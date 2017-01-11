import java.io.*;
import java.security.*;
import java.util.*;
import javax.net.*;
import javax.net.ssl.*;
import java.util.prefs.*;

/**
 * Class for handeling the ambulance client patient information requests.
 *
 * @version 030308
 * @author Kane Neman
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class AmbulanceClientPatient implements Runnable {

    //Ambulance client private key keystore
    private KeyStore clientKeyStore;

    //Server public key keystore
    private KeyStore serverKeyStore;

    //Used to generate a SocketFactory
    private SSLContext sslContext;

    //A source of secure random numbers
    private SecureRandom secureRandom;

    //A reference to the ambulance client's GUI. 
    private RecordInformationPane rip;

    //Socket to server
    private SSLSocket socket;

    //Connection to the server, used to write objects to.
    private OutputStream out;
    private ObjectOutputStream oos;

    //Connection to the server, used to read objects from
    private InputStream in;
    private ObjectInputStream ois;

    //Reference to LogHandler object
    private LogHandler lgHandler;

    //Server name in the network
    private String serverName;

    //Client name in the network
    private String clientName;

    //The port number the server listen for incoming requests
    private int port;

    //The key to server certificate
    private String pubKey;

    //Path to directory where certificates are stored
    private String certDir;

    //Variable used by GUI to signal the client to terminate this connection
    private boolean terminateStatus = false;

    //The person information packet
    private AmbulancePatientInformation api;

    ///Reference to this thread
    private Thread thread;

    /**
     * Constructor used to create the patient information packet,
     * and generate the random secure number for secure communication
     * @param lgHandler Reference to LogHandler object
     * @param personNumber The patient's person number
     * @param aip Refernece to the ambulance client GUI
     */
    public AmbulanceClientPatient(LogHandler lgHandler, String personNumber,
            RecordInformationPane rip) throws NullPointerException, 
    IllegalStateException, BackingStoreException {

        this.lgHandler = lgHandler;
        this.rip = rip;

        RegisterKey rk = new RegisterKey();
        clientName = rk.getClientName();
        serverName = rk.getServerName();
        port = rk.getAmbulancePatientPort();
        pubKey = rk.getPubKey();
        certDir = rk.getCertDirectory();

        secureRandom = new SecureRandom();
        secureRandom.nextInt();

        api = new AmbulancePatientInformation(personNumber);
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Method used to set up the server key store
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
     * is used by this object and the AmbulanceInformationPane.
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
                    "AmbulanceClientPatient/terminate",
                    "Falied to close socket/streams");
            lgHandler.addLog(log);

            rip.setMessage("Uppkopplingen kan inte stängas");
        }
    }

    /**
     * Method used to indecate the status of the terminateStatus
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
                    "AmbulanceClientPatient/run",
                    "Security problem");
            lgHandler.addLog(log);

            rip.setMessage("Certifikaten är ej giltiga");

            return;
        } catch (IOException ie) {
            log = new Log(ie.getMessage(),
                    "AmbulanceClientPatient/run",
                    "Failed to read certificates");
            lgHandler.addLog(log);

            rip.setMessage("Kunde inte läsa certifikaten");

            return;
        }

        SSLSocketFactory sf = sslContext.getSocketFactory();

        rip.setMessage("Ansluter till servern");

        //Try to connect to server
        while(!terminateStatus) {

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
            } catch(IOException ie) {
                log = new Log(ie.getMessage(),
                        "AmbulanceClientPatient/run",
                        "Failed to create socket/streams");
                lgHandler.addLog(log);

                rip.setMessage("Får ingen kontakt med servern");

                try {
                    sleep(10);
                } catch (InterruptedException IE) {
                    log = new Log(IE.getMessage(),
                            "AmbulanceClientPatient/run",
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
        
        rip.setMessage("Skickar förfråga till servern");

        // Try to send the packet to the server
        while(!terminateStatus) {
            try {
                oos.writeObject(api);
                oos.flush();
                break;
            } catch(IOException ie) {
                log = new Log(ie.getMessage(), 
                        "AmbulanceClientPatient/run",
                        "Failed to send the packet");
                lgHandler.addLog(log);

                rip.setMessage("Kunde inte skicka förfrågan till servern");
                try {
                    sleep(10);
                } catch (InterruptedException IE) {
                    log = new Log(IE.getMessage(),
                            "AmbulanceClientPatient/run",
                            "InterruptedException");
                    lgHandler.addLog(log);

                    terminate();
                    return;
                }
            }
        }

        if (!terminateStatus) {
            try {
                rip.setMessage("Väntar på svar från servern");
                //read the update packet from the stream
                api = (AmbulancePatientInformation)ois.readObject();

                //Check to see if server failed to fetch updates
                if (api.informationFailed()) {
                    //Inform the ambulance client gui that server has failed
                    rip.setMessage("Fel hos servern: " +
                            api.getMessage());
                } else
                    rip.setMessage(api.getMessage());

                //Pass the reference to the AmbulanceInformationPane
                rip.setPatientInformation(api);

                terminate();
            } catch (IOException ie) {
                log = new Log(ie.getMessage(),
                        "AmbulanceClientPatient/run",
                        "Failed to read the packet");
                lgHandler.addLog(log);

                rip.setMessage("Fel vid mottagning av data");

                terminate();
            } catch (ClassNotFoundException cnfe) {
                log = new Log(cnfe.getMessage(),
                        "AmbulanceClientPatient/run",
                        "Packet versions are not the same" +
                        "at server or client");
                lgHandler.addLog(log);

                rip.setMessage("Paket versionen i server och " +
                        "klienten är olika");
                terminate();
            }
        }
    }
}

