import java.io.*;

/**
 * This object represent the acknowledgement the 
 * server sends to a mabulance client
 */  
public class ServerAcknowledgement implements Serializable {

    private int clientRecordId;
    private int serverRecordId;
    private String serverMessage;

    public ServerAcknowledgement() { }

    public ServerAcknowledgement(int c, int s, String sm) { 
        clientRecordId = c;
        serverRecordId = s;
        serverMessage = sm;
    }


    public int getClientRecordId() {
        return clientRecordId;
    }

    public int getServerRecordId() {
        return serverRecordId;
    }

    public String getServerMessage() {
        return serverMessage;
    }
}
         
