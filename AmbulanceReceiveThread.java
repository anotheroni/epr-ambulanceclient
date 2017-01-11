import java.io.*;
import java.util.*;

/**
 * Class for handling the server responses and inform the client
 * with the received responses
 *
 * @author Kane Neman
 * @version 030305
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class AmbulanceReceiveThread implements Runnable {

    private LogHandler lgHandler;
    private AmbulanceClient ambClient;
    private ObjectInputStream ois;
    private int recordCount;
    private int receivedPacket;
    private  AmbulanceRecord ar;

    /**
     * Constructor, used to initialize this thread
     * @param lgHandler Reference to LogHandler object
     * @param ambClient Reference to AmbulanceClient object
     * @param ois The stream this thread will read objects from
     * @param recordCount The number of acknowleges expected from server
     * @param ar Reference to AmbulanceRecord object
     */
    public AmbulanceReceiveThread(LogHandler lgHandler, 
            AmbulanceClient ambClient, ObjectInputStream ois, int recordCount, 
            AmbulanceRecord ar) {

        this.lgHandler = lgHandler;
        this.ambClient = ambClient;
        this.ois = ois;
        this.recordCount = recordCount;
        this.ar = ar;

        receivedPacket = 0;

        new Thread(this).start();
    }

    /**
     * Background thread, used to read packets that server sends
     */
    public void run() {

        Log log;

        try {
            while(receivedPacket != recordCount) {
                ServerAcknowledgement sa = 
                    (ServerAcknowledgement) ois.readObject();

                ar.resultsFromSend(sa.getClientRecordId(),
                        sa.getServerRecordId(), sa.getServerMessage());

                receivedPacket++;
            }
        } catch(IOException ie) {
            log = new Log(ie.getMessage(), "AmbulanceReceiveThread/run",
                    "Failed to read packets from server");
            lgHandler.addLog(log);

            ar.resultsFromSend(-2, -2, "Fel vid mottagning av data.");

        } catch(ClassNotFoundException cnfe) {
            log = new Log(cnfe.getMessage(), "AmbulanceReceiveThread/run",
                    "Class versions is not the same at client and server");
            lgHandler.addLog(log);

            ar.resultsFromSend(-2, -2, "Paketversion konflikt");
        }

        try {
            ambClient.terminate();
        } catch (IOException ie) {
            log = new Log(ie.getMessage(), "AmbulanceReceiveThread/run",
                    "Failed to close socke/streams");
            lgHandler.addLog(log);
        }
    }
}
