import java.sql.*;
import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * Class that handles connections to the database.
 *
 * @author Kane Neman
 * @version 20030904
 */
public class DB2Connect implements Runnable {

    //Connection handle to the database
    private Connection con;

    //Process for executing db2jstrt command
    private Process p = null;

    //Number of attempts to start the JDBC applet server
    private final int TRY_COUNT = 20;

    /*Background thread, used to sleep this object 2 ms before 
      trying for starting JDBC applet server upon failure*/
    private Thread processThread = null;

    //Database address
    private String dbaddress;

    //The user id of the database
    private String userId;

    //The password of the user
    private String pwd;

    /*Command used to start the JDBC applet server on the specified
      port number. The JDBC applet server is a server daemon that 
      enables SQLJ and JDBC applets to connect to the DB2 server.*/
    private final String JDBC_COMMAND = "db2jstrt 50000";

    //Reference to LogHandler object
    private LogHandler lgHandler;

    //Error message to caller
    private String message = null;

    private boolean remote;

    static {//load the driver
        try {
            Class.forName("COM.ibm.db2.jdbc.net.DB2Driver");
        } catch (Exception e) {
            /*System.err.println("Could not load database driver\n " + 
              "Error:" + e);*/
        }
    }

    /**
     * Constructor, starts a new thread that opens the connection.
     * @param dbadress Address to the database.
     * @param lgHandler Reference to the LogHandler object
     * @param remote Indicates the access to a local or remote database.
     * True indicates a remote database and false a local database
     */
    public DB2Connect(String dbaddress, LogHandler lgHandler, boolean remote) {

        this.dbaddress = dbaddress;
        this.lgHandler = lgHandler;
        this.remote = remote;
        processThread = new Thread(this);
    }

    /**
     * Background thread, used to sleep this process for each
     * failure attempt to run the db2jstrt command and connect to
     * database
     */
    public void run() {

        int state = 1;

        for (int i = 0; i < TRY_COUNT; i++) {
            try {
                con = DriverManager.getConnection(dbaddress, userId, pwd);
                break;
            } catch (SQLException sql) {
                if ((i + 1) ==  TRY_COUNT) {
                    while (sql != null) {
                        lgHandler.addLog(
                                new Log(sql.getMessage(), 
                                    "SQLState: " + sql.getSQLState(),
                                    "ERRORCode: " + sql.getErrorCode()));

                        sql = sql.getNextException();
                    }//while
                    message = "Kan inte få kontakt med databasen";
                }//if

                if (!remote) {
                    if ((i + 1) < TRY_COUNT) {
                        try {
                            if (state > 0) { 
                                p = Runtime.getRuntime().exec(JDBC_COMMAND);
                                state = p.waitFor();
                            }
                        } catch (Exception e) {
                            lgHandler.addLog(new Log(e.getMessage(),
                                        "DB2Connect/run", 
                                        "Failed to execute db2jstrt command"));
                        }
                    }
                }

                if ((i + 1) < TRY_COUNT) {
                    try { //Sleep TRY_COUNT ms before next attempt
                        processThread.sleep(TRY_COUNT);
                    } catch (InterruptedException ie) {
                        lgHandler.addLog( new Log(ie.getMessage(),
                                    "DB2Connect/run", "InterruptedException"));
                        break;
                    }
                }//if
            }//catch
        }//for
    }

    /**
     * Method that connects to the database.
     * @param userId The user name to connect with.
     * @param pwd The password to connect with.
     * @return Null if ok else an error message
     */
    public String connect(String userId, String pwd) {

        this.userId = userId;
        this.pwd = pwd;

        try {
            con = DriverManager.getConnection(dbaddress, userId, pwd);
        } catch (SQLException sql) {
            processThread.start();

            try {
                processThread.join();
            } catch (InterruptedException ie) {
                lgHandler.addLog(new Log(ie.getMessage(),
                            "DB2Connect/connect",
                            "join"));
            }
        }

        if (con == null)
            throw new NullPointerException("Failed to connect to DB");
        return message;
    }

    /**
     * Method that disconnects from the database.
     */
    public void disconnect() {

        try {
            con.close();
        } catch(SQLException sql) {
            while (sql != null) {
                lgHandler.addLog(
                        new Log(sql.getMessage(),
                            "SQLState: " + sql.getSQLState(),
                            "ERRORCode: " + sql.getErrorCode()));

                sql = sql.getNextException();
            }//while
        }
    }

    /**
     * Method that executes a database query.
     * @throws java.sql.SQLException Thrown when the query failed.
     * @param query The SQL query
     * @return The result from the query
     */
    public ResultSet dbQuery(String query) throws java.sql.SQLException
    {
        Statement stmt = con.createStatement();
        return stmt.executeQuery(query);
    }

    /**
      * Method that executes a database update query.
      * @throws java.sql.SQLException Thrown when the query failed.
      * @param query The SQL query.
      * @return The result from the query.
      */
    public int dbQueryUpdate (String query) throws java.sql.SQLException
    {
       Statement stmt = con.createStatement ();
       return stmt.executeUpdate (query);
    }

    /**
     * Method that setsup a prepared statement.
     * @param query The SQL query
     * @return A preparedStatement.
     * @throws java.sql.SQLException Thrown when preparing the statment failed.
     */
    public PreparedStatement prepareStatement(String query) throws
        java.sql.SQLException
        {
            return con.prepareStatement(query);
        }

    /**
     * This method is used to turn on/off
     * the auto commit behaivior of the database
     * @param state true turn on the auto commit otherwise false turn it off
     */
    public void setAutoCommit(boolean state) throws SQLException {
        con.setAutoCommit(state);
    }

    /**
     * This method require java 1.4
     * Method used to execute a (Insert, Delete, Update) query
     * and returning a ResultSet that containing the Primary key
     * @param query is the query that will be executed
     * @param autoGeneratedKeys define if this mode should be activated or not
     * options for this parameter are: NO_GENERATED_KEYS, RETURN_GENERATED_KEYS
     * @return Containing the Primary key
     */
    public ResultSet dbQuery(String query, int autoGeneratedKeys) throws 
        SQLException {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(query, autoGeneratedKeys);
            return stmt.getGeneratedKeys();
        }
}
