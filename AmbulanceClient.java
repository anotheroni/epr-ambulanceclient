import java.io.*;
import java.security.*;
import java.util.*;
import javax.net.*;
import javax.net.ssl.*;
import java.sql.*;
import java.util.prefs.*;

/**
 * Class for sending a patient record to server
 *
 * @version 030305
 * @author Kane Neman
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class AmbulanceClient implements Runnable {

    private final int THRESHOLD = 5; 

    //KeyStore containing the client certificate
    private KeyStore clientKeyStore;

    //KeyStore containing the server certificate
    private KeyStore serverKeyStore;

    //Used to generate a SocketFactory
    private SSLContext sslContext;

    //Source of secure random numbers
    private SecureRandom secureRandom;

    //Reference to the ambulance client's GUI
    private AmbulanceRecord ar; 

    //Connection to server
    private SSLSocket socket;
    private OutputStream out;
    private ObjectOutputStream oos;
    private InputStream in;
    private ObjectInputStream ois;

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

    //The path to directory where ceretificates are stored
    private String certDir;

    //A vector of records that will be send to the server
    private Vector records;

    //Variable to signal the client to terminate this connection
    private boolean terminateStatus = true;

    /**
     * Constructor used to building record packets and generate the random
     * secure number for secure communication
     * @param lgHandler Reference to LogHandler object
     * @param ar Reference to AmbulanceRecord object
     * @param recordId An array containing the patient record ids that 
     * will be send to server
     */
    public AmbulanceClient(LogHandler lgHandler, AmbulanceRecord ar, 
            DB2Connect dbcon, int[] recordId) throws SQLException, 
    NullPointerException, IllegalStateException, BackingStoreException {

        this.lgHandler = lgHandler;
        this.ar = ar;

        RegisterKey rk = new RegisterKey();
        clientName = rk.getClientName();
        serverName = rk.getServerName();
        port = rk.getAmbulancePort();
        pubKey = rk.getPubKey();
        certDir = rk.getCertDirectory();

        records = new Vector();

        for(int i = 0; i < recordId.length; i++) {
            Record r = new Record(recordId[i], dbcon);
            if ((i + 1) == recordId.length)
                r.setLastPacket();
            records.add(r);
        }

        secureRandom = new SecureRandom();
        secureRandom.nextInt();

        new Thread(this).start();
    }

    /**
     * Method used to set up the server keystore
     */ 
    private void setupServerKeystore() throws GeneralSecurityException, 
    IOException {

        serverKeyStore = KeyStore.getInstance("JKS");
        serverKeyStore.load(new FileInputStream(certDir + "server.public"), 
                pubKey.toCharArray());
    }

    /**
     * Method used to set up the client keystore
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
     * Method used to write a Record object to the stream
     * @param Record The record that will be send to server
     */
    public void send(Record record) throws IOException {
        oos.writeObject(record);
        oos.flush();
    }

    /**
     * Method used to close the socket, streams and stop the thread
     */ 
    public void terminate() throws IOException {
        terminateStatus = false;
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
    }

    /**
     * Background thread, used to send the record packets to server
     */ 
    public void run() {

        Log log;

        try {
            setupServerKeystore();
            setupClientKeyStore();
            setupSSLContext();
        } catch (GeneralSecurityException gse) {
            ar.resultsFromSend(-1, -1,
                    "Certifikatet är ogiltigt!" +
                    " Kan ej ansluta till servern.");

            log = new Log(gse.getMessage(),"AmbulanceClient/run",
                    "Security problem");
            lgHandler.addLog(log);

            return;
        } catch (IOException ie) {
            ar.resultsFromSend(-1, -1,
                    "Kunde inte läsa certifikaten");

            log = new Log(ie.getMessage(), "AmbulanceClient/run",
                    "Failed to read certificates");
            lgHandler.addLog(log);

            return;
        }

        SSLSocketFactory sf = sslContext.getSocketFactory();

        while (terminateStatus) {

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
                AmbulanceReceiveThread art = 
                    new AmbulanceReceiveThread(lgHandler, this, ois, 
                            records.size(), ar);
                break;
            } catch(IOException ie) {
                ar.resultsFromSend(-2, -2, 
                        "Uppkopplingsfel. Servern svarar inte.");

                log = new Log(ie.getMessage(), "AmbulanceClient/run",
                        "Failed to create socket/streams");
                lgHandler.addLog(log);
                try {
                    terminate();
                } catch (IOException ie2) {
                    log = new Log(ie2.getMessage(), "AmbulanceClient/run",
                            "Failed to close socket/streams");
                }
            }
        }

        int attempts = 0;

        while(terminateStatus) {
            Enumeration eNum = records.elements();
            while(eNum.hasMoreElements()) {
                Record r = (Record) eNum.nextElement();
                try {
                    send(r);
                    records.remove(r);
                } catch(IOException ie) {
                    /*ar.resultsFromSend(-2, -2,
                      "Fel vid sändning av data." +
                      " Servern svarar inte");*/
                    if ((attempts + 1) == THRESHOLD) {
                        log = new Log(ie.getMessage(), "AmbulanceClient/run",
                            "Failed to send the record packet");
                        lgHandler.addLog(log);
                    }
                }
            }
            if(records.isEmpty())
                terminateStatus = false;
            else 
                attempts++;

            if (attempts == THRESHOLD) {
                ar.resultsFromSend(-2, -2, THRESHOLD + 
                        " försök gjordes för att" +
                        " skicka data till servern!\n" + 
                        " Sändning misslyckades");

                try {
                    terminate();
                } catch (IOException ie) {
                    log = new Log(ie.getMessage(), "AmbulanceClient/run",
                            "Failed to close socket/streams");
                }
            }
        }
    }
}
