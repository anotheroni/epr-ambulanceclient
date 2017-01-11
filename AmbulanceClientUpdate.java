import java.io.*;
import java.security.*;
import java.util.*;
import javax.net.*;
import javax.net.ssl.*;
import java.sql.*;
import java.util.prefs.*;

/**
 * Class for handeling the ambulance client updates.
 *
 * @version 20030329
 * @author Kane Neman
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class AmbulanceClientUpdate implements Runnable {

    //KeyStore containing client certificate
    private KeyStore clientKeyStore;

    //KeyStore containing server certificate
    private KeyStore serverKeyStore;

    //Used to generate a SocketFactory
    private SSLContext sslContext;

    //A source of secure random numbers
    private SecureRandom secureRandom;

    //Reference to ambulance GUI.
    private SynchronizeDialog sd;

    //Socket to server
    private SSLSocket socket;

    //Connection to the server, used to write objects to.
    private ObjectOutputStream oos;
    private OutputStream out;

    //Connection to the server, used to read objects from
    private ObjectInputStream ois;
    private InputStream in;

    //Reference to LogHandler object
    private LogHandler lgHandler;

    //Server name in the network
    private String serverName;

    //Ambulance client name in the network
    private String clientName;

    //The port number the server listens to
    private int port;

    //The key to server certificate
    private String pubKey;

    //Path to directory where certificates are stored
    private String certDir;

    //Variable used by GUI to signal the client to terminate this connection
    private boolean terminateStatus = false;

    //Reference to ambulance client database connection
    private DB2Connect dbcon;

    //The update packet that will be send to the server
    private UpdatePacket updatePacket;

    //Reference to this thread
    private Thread thread;

    /**
     * Constructor used to initialize the lokal variables,
     * and generate the random secure number for secure communication
     * @param lgHandler Reference to LogHandler object
     * @param sd Refernece to the ambulance client GUI
     * @param dbcon Refernece to the database
     */
    public AmbulanceClientUpdate(LogHandler lgHandler, SynchronizeDialog sd, 
            DB2Connect dbcon) throws SQLException, NullPointerException, 
    IllegalStateException, BackingStoreException {

        this.lgHandler = lgHandler;
        this.sd = sd;
        this.dbcon = dbcon;

        RegisterKey rk = new RegisterKey();
        clientName = rk.getClientName();
        serverName = rk.getServerName();
        port = rk.getAmbulanceUpdatePort();
        pubKey = rk.getPubKey();
        certDir = rk.getCertDirectory();

        updatePacket = new UpdatePacket(dbcon);

        secureRandom = new SecureRandom();
        secureRandom.nextInt();

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Method used to set up the server key store, extracting 
     * the server public key
     */
    private void setupServerKeystore() throws GeneralSecurityException, 
    IOException {

        serverKeyStore = KeyStore.getInstance("JKS");
        serverKeyStore.load(new FileInputStream(certDir + "server.public"), 
                pubKey.toCharArray());
    }

    /**
     * Method used to set up the ambulance client key store
     */ 
    private void setupClientKeyStore() throws GeneralSecurityException, 
    IOException {

        clientKeyStore = KeyStore.getInstance("JKS");
        clientKeyStore.load(
                new FileInputStream(certDir + clientName + ".private"),
                clientName.toCharArray());
    }

    /**
     * Method used to initialize the SSL parameters
     */ 
    private void setupSSLContext() throws GeneralSecurityException, 
    IOException {

        TrustManagerFactory tmf = 
            TrustManagerFactory.getInstance("SunX509");
        tmf.init(serverKeyStore);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(clientKeyStore, clientName.toCharArray());

        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
                secureRandom);
    }

    /**
     * Method used to terminate the background thread. This method 
     * is used by AmbulanceClientUpdate and the AmbulanceRecord.
     */ 
    public void terminate() {
        terminateStatus = true;
        try {
            if (socket != null)
                if (!socket.isClosed())
                    socket.close();
            if (out != null)
                out.close();
            if (oos != null)
                oos.close();
            if (in != null)
                in.close();
            if (ois != null)
                ois.close();
        } catch (IOException ie) {
            Log log = new Log(ie.getMessage(),
                    "AmbulanceClientUpdate/terminate",
                    "Falied to close socket/streams");
            lgHandler.addLog(log);

            sd.setMessage("Uppkopplingen kan inte stängas");
        }
    }

    /**
     * Method used to indicate the status of the terminateStatus
     * @return true if the the thread is termianted otherwise false
     */ 
    public boolean getTerminateStatus() {
        return terminateStatus;
    }

    /**
     * Method used to sleep this thread
     * @param sec The amount of time this thread will sleep in seconds
     */
    public void sleep(int sec) throws InterruptedException {
        thread.sleep(sec*1000);
    }

    /**
     * Method used to run the background thread. This method handle the
     * connect, send, read procedures
     */ 
    public void run() {

        Log log;

        try {
            setupServerKeystore();
            setupClientKeyStore();
            setupSSLContext();
        } catch (GeneralSecurityException gse) {
            log = new Log(gse.getMessage(), 
                    "AmbulanceClientUpdate/run",
                    "Security problem");
            lgHandler.addLog(log);

            sd.setMessage("Certifikaten är ej giltiga");

            return;
        } catch (IOException ie) {
            log = new Log(ie.getMessage(),
                    "AmbulanceClientUpdate/run",
                    "Failed to read certificates");
            lgHandler.addLog(log);

            sd.setMessage("Kunde inte läsa certifikaten");

            return;
        }

        SSLSocketFactory sf = sslContext.getSocketFactory();

        sd.setMessage("Ansluter till servern");

        //Try to connect to server
        while (!terminateStatus) {

            socket = null;
            out = null;
            oos = null;
            in = null;
            ois = null;

            try {
                socket = (SSLSocket)sf.createSocket(serverName, port);
                out = socket.getOutputStream();
                oos = new ObjectOutputStream(out);
                in = socket.getInputStream();
                ois = new ObjectInputStream(in);
                break;
            } catch (IOException ie) {
                log = new Log(ie.getMessage(),
                        "AmbulanceClientUpdate/run",
                        "Failed to create socket/streams");
                lgHandler.addLog(log);

                sd.setMessage("Får ingen kontakt med servern");

                try {
                    sleep(10);
                } catch (InterruptedException IE) {
                    log = new Log(IE.getMessage(),
                            "AmbulanceClientUpdate/run",
                            "InterruptedException");
                    lgHandler.addLog(log);

                    terminate();
                    return;
                }
            }
        }

        //If user cancel the communication
        if (terminateStatus)
            return;
        
        sd.setMessage("Skickar förfråga till servern");

        // Try to send the packet to the server
        while(!terminateStatus) {
            try {
                oos.writeObject(updatePacket);
                oos.flush();
                break;
            } catch(IOException ie) {
                log = new Log(ie.getMessage(), 
                        "AmbulanceClientUpdate/run",
                        "Failed to send the update packet");
                lgHandler.addLog(log);

                sd.setMessage("Kunde inte skicka förfrågan till servern");
                try {
                    sleep(10);
                } catch (InterruptedException IE) {
                    log = new Log(IE.getMessage(),
                            "AmbulanceClientUpdate/run",
                            "InterruptedException");
                    lgHandler.addLog(log);

                    terminate();
                    return;
                }
            }
        }

        //If user cancel the communication
        if (terminateStatus)
            return;

        if (!terminateStatus) {
            try {
                sd.setMessage("Väntar på svar från servern");
                //read the update packet from the stream
                updatePacket = (UpdatePacket)ois.readObject();

                //Check to see if server failed to fetch updates
                if (updatePacket.doesUpdateFailed()) {
                    //Inform the ambulance client gui that server has failed
                    sd.setMessage("Fel hos servern: " +
                            updatePacket.getMessage());
                    terminate();
                    return;
                }
            } catch (IOException ie) {
                log = new Log(ie.getMessage(),
                        "AmbulanceClientUpdate/run",
                        "Failed to read the packet");
                lgHandler.addLog(log);

                sd.setMessage("Fel vid mottagning av data");

                terminate();
                return;
            } catch (ClassNotFoundException cnfe) {
                log = new Log(cnfe.getMessage(),
                        "AmbulanceClientUpdate/run",
                        "Packet versions are not the same" +
                        "at server or client");
                lgHandler.addLog(log);

                sd.setMessage("Paket versionen i server och " +
                        "klienten är olika");
                terminate();
                return;
            }

            sd.setMessage("Skriver uppdateringar till den lokala databasen");

            //write the updates to the client database
            updatePacket.writeUpdates(dbcon, lgHandler);

            /*Inform the client about the update status*/
            sd.setMessage(updatePacket.getMessage());
        }
        //Send the update version to server
        while (!terminateStatus) {
            try {
                oos.writeObject(updatePacket.getClientUpdateTime());
                oos.flush();

                //Inform the client that the update went well
                sd.setMessage("Synkroniseringen utförd");
                sd.syncComplete();

                terminate();
            } catch (IOException ie) {
                log = new Log(ie.getMessage(),
                        "AmbulanceClientUpdate/run",
                        "Failed to send the version");
                lgHandler.addLog(log);

                /*Inform the client gui that the version can not be send 
                  to the server*/
                sd.setMessage("Fel vid sändning av svar till servern");
                try {
                    sleep(10);
                } catch (InterruptedException IE) {
                    log = new Log(IE.getMessage(),
                            "AmbulanceClientUpdate/run",
                            "InterruptedException");
                    lgHandler.addLog(log);

                    terminate();
                }
            }
        }
    }
}
