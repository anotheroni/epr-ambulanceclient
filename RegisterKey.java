import java.util.prefs.*;

/**
 * Class for handling the windows registry
 *
 * Exception defenition for methods in this object:
 * NullPointerException: If Key is null
 * IllegalArgumentException: If key length exceeds MAX_KEY_LENGTH 
 * IllegalStateException: If this node has been removed
 * SecurityException: If a security manager is present and it denies
 *                    runtime permission
 * BackingStoreException: If an operation cannot be completed due to a 
 *                        failure in the backing store, or inability to 
 *                        communicate with it
 *
 * @author Kane Neman
 * @version 030219
 * 
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class RegisterKey {

    //Root node pref/Epr/Config
    private final String ROOT = "Epr/Client";
    //Name of the key that contains the client name
    private final String CLIENT_KEY_NAME = "clientName";
    //Name of the key that contains the server name
    private final String SERVER_KEY_NAME = "serverName";
    //Name of the key that contains the database user name 
    private final String DB_KEY_USER = "dbUser";
    //Name of the key that contains the database password
    private final String DB_KEY_PASS = "dbPass";
    //Name of the key that contains the portnumber to the database
    private final String DB_KEY_PORT = "dbPort";
    //Name of the key that contains the name of the database
    private final String DB_KEY_NAME = "dbName";
    //Name of the key that contains the path where keystores are stored
    private final String CERT_KEY_DIRECTORY ="certDir";
    //Name of the key that contains the port number for Ambulance.java
    private final String AMB_KEY_PORT = "ambPort";
    //Name of the key that contains the port number for AmbulancePatient.java
    private final String AMB_PAT_KEY_PORT = "ambPatPort";
    //Name of the key that contains the port number for AmbulanceUpdate.java
    private final String AMB_UP_KEY_PORT = "ambUpPort";
    //Name of the key that contains the port number for ER.java
    private final String ER_KEY_PORT = "erPort";
    //Name of the key that contains the port number fort SOS.java
    private final String SOS_KEY_PORT = "sosPort";
    //Name of the key that contains the public key
    private final String PUB_KEY = "pubKey";
    //Default value to pass to get methods upon retreiving string key values
    private final String DEFAULT_STRING = null;
    //Default value to pass to get methods upon retreiving string key values
    private final int DEFAULT_INT = -1;
    //Exception message if the key value is null
    private final String EXCEPTION_MESSAGE = "Key value is null";

    //Reference to the prefs in windows registry
    private Preferences preference = null;

    /**
     * Constructor for creating the node for first time if the node does 
     * not exist, otherwise if the node exists a reference to the node 
     * will be retrieved
     */  
    public RegisterKey() throws IllegalArgumentException,
    NullPointerException, IllegalStateException, SecurityException,
    BackingStoreException {
        preference = Preferences.systemRoot().node(ROOT);
        preference.flush();
    }

    /**
     * Method used to set the client name in the registry
     * @param clientName is the name of the client in the network
     */
    public void setClientName(String clientName) throws NullPointerException, 
    IllegalStateException, IllegalArgumentException {
        preference.put(CLIENT_KEY_NAME, clientName);
    }

    /**
     * Method used to set the server name in the registry
     * @param serverName is the name of the server in the network
     */
    public void setServerName(String serverName) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        preference.put(SERVER_KEY_NAME, serverName);
    }

    /**
     * Method used to set the user name of the database in the registry
     * @param userName The user name of the database
     */
    public void setDBUser(String userName) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        preference.put(DB_KEY_USER, userName);
    }

    /**
     * Method used to set the password to the database in the registry
     * @param pass The password to the database
     * @return -1 if the operation failes otherwise 0
     */ 
    public int setDBPass(String pass) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        
        String c = null;
        
        c = getClientName();
        
        //If client name could not be read
        if (c == null)
            return DEFAULT_INT; 
        
        byte [] data = c.getBytes();
        byte [] source = pass.getBytes();

        process(source, source.length, data, data.length);
        preference.put(DB_KEY_PASS, new String(source));
        
        return 0;
    }

    /**
     * Method used to set the database port number in the registry
     * @param portNumber The database port number
     */
    public void setDBPort(String portNumber) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        preference.put(DB_KEY_PORT, portNumber);
    }

    /**
     * Method used to set the database name in the registry
     * @param dbName The name of the database
     */
    public void setDBName(String dbName) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        preference.put(DB_KEY_NAME, dbName);
    }

    /**
     * Method used to set the directory path to certificates
     * @param path The path to the directory where certificates are stored
     */ 
    public void setCertDirectory(String path) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        preference.put(CERT_KEY_DIRECTORY, path);
    }

    /**
     * Method used to set the port number for the thread which handles
     * the transmission of patient record from a client to the server
     * @param portNumber The port that server listens to
     */ 
    public void setAmbulancePort(int portNumber) throws NullPointerException,
    IllegalStateException, IllegalArgumentException {
        preference.putInt(AMB_KEY_PORT, portNumber);
    }

    /**
     * Method used to set the port number for the thread which handles
     * the patient information requests  
     * @param portNumber The port that server listens to
     */
    public void setAmbulancePatientPort(int portNumber) throws 
        NullPointerException, IllegalStateException, IllegalArgumentException {
            preference.putInt(AMB_PAT_KEY_PORT, portNumber);

    }

    /**
     * Method used to set the port number for the thread which handles
     * the synchronization requests
     * @param portNumber The port that server listens to
     */ 
    public void setAmbulanceUpdatePort(int portNumber) throws
        NullPointerException, IllegalStateException, IllegalArgumentException {
            preference.putInt(AMB_UP_KEY_PORT, portNumber);
     }

    /**
     * Method used to set the port number for the thread which handles
     * the connection requests from ER clients
     * @param portNumber The port that server listens to
     */
    public void setERPort(int portNumber) throws
        NullPointerException, IllegalStateException, IllegalArgumentException {
            preference.putInt(ER_KEY_PORT, portNumber);
    }

    /**
     * Method used to set the port number for the thread which handles
     * the connection requests from SOS clients
     * @param portNumber The port that server listens to
     */
    public void setSOSPort(int portNumber) throws
        NullPointerException, IllegalStateException, IllegalArgumentException {
            preference.putInt(SOS_KEY_PORT, portNumber);
    }

    /**
     * Method used to set the public key
     * @param pubKey The public key
     * @return -1 if the operation failes otherwise 0
     */ 
    public int setPubKey(String pubKey) throws
        NullPointerException, IllegalStateException, IllegalArgumentException {
            
        String c = null;
        
        c = getClientName();

        //If client name could not be read
        if (c == null)
            return DEFAULT_INT;

        byte [] data = c.getBytes();
        byte [] source = pubKey.getBytes();

        process(source, source.length, data, data.length);
        preference.put(PUB_KEY, new String(source));
        return 0;
    }

    /**
     * Method used to get the name of the client stored in the registry
     * @return The client name 
     */ 
    public String getClientName() throws NullPointerException, 
    IllegalStateException {
        String retValue = preference.get(CLIENT_KEY_NAME, DEFAULT_STRING);
        if (retValue == null) 
           throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the name of the server stored in the registry
     * @return The server name
     */ 
    public String getServerName() throws NullPointerException,
    IllegalStateException {
       String retValue =  preference.get(SERVER_KEY_NAME, DEFAULT_STRING);
       if (retValue == null)
           throw new NullPointerException(EXCEPTION_MESSAGE);
       return retValue;
    }
    
    /**
     * Method used to get the database user name 
     * @return The user name of the database
     */
    public String getDBUser() throws NullPointerException,
    IllegalStateException {
        String retValue = preference.get(DB_KEY_USER, DEFAULT_STRING);
        if (retValue == null) 
           throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the database password
     * @return The database password
     */ 
    public String getDBPass() throws NullPointerException,
    IllegalStateException {
        
        String p = null;
        String c = null;
        
        p = preference.get(DB_KEY_PASS, DEFAULT_STRING);
        c = getClientName();
        
        //IF data could not be read
        if (p == null || c == null)
           throw new NullPointerException(EXCEPTION_MESSAGE);

        byte[] source = p.getBytes();
        byte[] data = c.getBytes();
        
        process(source, source.length, data, data.length);

        return new String(source);
    }

    /**
     * Method used to get the database port number
     * @return The database port number
     */ 
    public String getDBPort() throws NullPointerException,
    IllegalStateException {
        String retValue = preference.get(DB_KEY_PORT, DEFAULT_STRING);
        if (retValue == null)
           throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the databse name
     * @return The database name
     */
    public String getDBName() throws NullPointerException,
    IllegalStateException {
        String retValue = preference.get(DB_KEY_NAME, DEFAULT_STRING);
        if (retValue == null) 
           throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the directory where the certificates are
     * stored
     * @return The path to the certificate directory
     */ 
    public String getCertDirectory() throws NullPointerException,
    IllegalStateException {
        String retValue = preference.get(CERT_KEY_DIRECTORY, DEFAULT_STRING);
        if (retValue == null) 
           throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the server port number to Ambulance thread
     * @return The port number
     */
    public int getAmbulancePort() throws NullPointerException,
    IllegalStateException {
        int retValue = preference.getInt(AMB_KEY_PORT, DEFAULT_INT);
        if (retValue == DEFAULT_INT)
            throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the server port number to AmbulancePatient thread
     * @return The port number
     */
    public int getAmbulancePatientPort() throws NullPointerException,
    IllegalStateException {
        int retValue = preference.getInt(AMB_PAT_KEY_PORT, DEFAULT_INT);
        if (retValue == DEFAULT_INT)
            throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }
    
    /**
     * Method used to get the server port number to AmbulanceUpdate thread
     * @return The port number
     */
    public int getAmbulanceUpdatePort() throws NullPointerException,
    IllegalStateException {
        int retValue = preference.getInt(AMB_UP_KEY_PORT, DEFAULT_INT);
        if (retValue == DEFAULT_INT)
            throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

    /**
     * Method used to get the server port number to ER thread
     * @return The port number
     */ 
    public int getERPort() throws NullPointerException,
    IllegalStateException {
        int retValue = preference.getInt(ER_KEY_PORT, DEFAULT_INT);
        if (retValue == DEFAULT_INT)
            throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }
    
    /**
     * Method used to get the server port number to SOS thread
     * @return The port number
     */ 
    public int getSOSPort() throws NullPointerException,
    IllegalStateException {
        int retValue = preference.getInt(SOS_KEY_PORT, DEFAULT_INT);
        if (retValue == DEFAULT_INT)
            throw new NullPointerException(EXCEPTION_MESSAGE);
        return retValue;
    }

     /**
     * Method used to get the public key
     * @return The public key
     */
    public String getPubKey() throws NullPointerException,
    IllegalStateException {
        
        String p = null;
        String c = null;
        
        p = preference.get(PUB_KEY, DEFAULT_STRING);
        c = getClientName();
        
        //IF data could not be read
        if (p == null || c == null)
           throw new NullPointerException(EXCEPTION_MESSAGE);

        byte[] source = p.getBytes();
        byte[] data = c.getBytes();
        
        process(source, source.length, data, data.length);

        return new String(source);
    }
    
    /**
     * Method used to get all the keys in this node in the registry
     * @return An array with key names
     */ 
    public String[] getKeys() throws BackingStoreException, 
    IllegalStateException {
        return preference.keys();
    }

    /**
     * Method used to remove this node from the registry
     */ 
    public void removeNode() throws BackingStoreException, 
    IllegalStateException, UnsupportedOperationException {
        preference.removeNode();
    }

    /**
     * Method used to get this nodes absolute path
     * @return The absolute path of this node
     */ 
    public String getNodeAbsolutePath() {
        return preference.absolutePath();
    }

    /**
     * Method used to get the database address, port number and
     * name in a string
     * @return The database address 
     */ 
    public String getDBPath() throws NullPointerException, 
    IllegalStateException {
        return "jdbc:db2://" + getClientName() + ":" + getDBPort() +
            "/" + getDBName();
    }
    
    private static void process(byte[] source, int sourceLength, 
            byte[] data, int dataLength) {
        for (int i = 0; i < sourceLength; i ++)
            source[i] ^= data[i%dataLength];
    }
    
    public static void main (String [] args) {
        String [] keys = null;

        try {
             //Set the registry values
             RegisterKey rk = new RegisterKey();
             //rk.removeNode();
             rk.setClientName("u07w195");
             rk.setServerName("u07w255");
             rk.setDBUser("dbepr");
             rk.setDBPass("x9Aw37b");
             rk.setDBPort("50000");
             rk.setDBName("AMBULANS");
             rk.setCertDirectory("cert/");
             rk.setAmbulancePort(1070);
             rk.setAmbulancePatientPort(1090);
             rk.setAmbulanceUpdatePort(1080);
             //rk.setERPort(1060);
             //rk.setSOSPort(1050);
             rk.setPubKey("public");
             
             //Get the registry values
             System.out.println("Client name: " + rk.getClientName());
             System.out.println("Server name: " + rk.getServerName());
             System.out.println("DB User: " + rk.getDBUser());
             System.out.println("DB Pass: " + rk.getDBPass());
             System.out.println("DB Port: " + rk.getDBPort());
             System.out.println("DB Name: " + rk.getDBName());
             System.out.println("Cert dir: " + rk.getCertDirectory());
             System.out.println("Amb port: " + rk.getAmbulancePort());
             System.out.println("AmbP port: " + rk.getAmbulancePatientPort());
             System.out.println("AmbUP port: " + rk.getAmbulanceUpdatePort());
             //System.out.println("ER port: " + rk.getERPort());
             //System.out.println("SOS port: " + rk.getSOSPort());
             System.out.println("Public key: " + rk.getPubKey());
             System.out.println("DB Path: " + rk.getDBPath());
             System.out.println("ABS Path: " + rk.getNodeAbsolutePath());
             
             keys = rk.getKeys();
             System.out.println("Keys: ");

            if (keys != null)
                for (int i = 0; i < keys.length; i++)
                    System.out.println(keys[i]);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }
}
