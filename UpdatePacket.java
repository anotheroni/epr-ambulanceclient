import java.io.*;
import java.sql.*;
import java.util.Vector;

/**
 * This class represent the packet that is send for
 * synchronization between server and client
 *
 * @author Kane Neman
 * @version 20030904
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class UpdatePacket implements Serializable {

    private String clientId;
    private String serverMessage = "";
    private Timestamp clientUpdateTime = null;
    private Vector updates;
    private boolean regularUpdate = true;
    private String message;
    private boolean updateFailed = false;
    private Vector blockedUsers;

    /**
     * Constructor used for serialization
     */ 
    public UpdatePacket() { }

    /**
     * Constructor used for creating an update packet.
     * @param dbcon is the reference to the ambulance client database 
     * connection
     */
    public UpdatePacket(DB2Connect dbcon) throws SQLException {

        int userId;
        //Fetch the client id and client version
        String query = "SELECT * FROM EPR.SETTINGS";
        ResultSet resultSet = dbcon.dbQuery(query);
        resultSet.next();

        clientUpdateTime = resultSet.getTimestamp("UPDATE_VERSION");
        clientId = resultSet.getString("CLIENT_ID");

        /*Check to see if this is a first time synchronization
          or a regular update*/
        if (clientUpdateTime == null)
            regularUpdate = false;

        blockedUsers = new Vector();

        //Get the blocked users which have more than 3 login attempts
        query ="SELECT USER_ID FROM EPR.BLOCKED_USERS WHERE " +
            "FAILED_ATTEMPTS >= 3";
        resultSet = dbcon.dbQuery(query);

        //Store blocked users 
        while (resultSet.next()) {
            userId = resultSet.getInt("USER_ID");
            blockedUsers.add(new Integer(userId));
        }

        query = "DELETE FROM EPR.BLOCKED_USERS WHERE " +
            "FAILED_ATTEMPTS >= 3";
        dbcon.dbQueryUpdate (query);
    }

    /** 
     * Method used to read updates from server database
     * @param dbcon is the reference to the server database
     */
    public void readUpdates(DB2Connect dbcon, LogHandler lgHandler) {

        updates = new Vector();
        Integer userId;
        ResultSet resultSet;
        String query;
        Timestamp ts;
        Log log;

        //If there are any blocked users
        for (int i = 0; i < blockedUsers.size(); i++) {

            userId = (Integer) blockedUsers.elementAt(i);

            try {

                //Check to see if the user is already blocked
                query = "SELECT * FROM EPR.STAFF WHERE " +
                    "STAFF_ID = " + userId.intValue() + " AND " +
                    "DISABLE = 1";

                resultSet = dbcon.dbQuery(query);

                if (resultSet.next()) {
                    serverMessage = "Användaren " + 
                        resultSet.getString("FIRST_NAME") + " " + 
                        resultSet.getString("LAST_NAME") + 
                        " är redan blockerad\n";
                    continue;
                }
            } catch (SQLException sql) {
                serverMessage = serverMessage + "Fel hos server. " +
                    "Kunde inte slå upp blockerade användare.\n" +
                    "Användare ID = " + userId.intValue() + " " +
                    sql.getMessage() + "\n";

                log = new Log(sql.getMessage(), 
                        "UpdatePacket/readUpdates",
                        "Failed to block user-id = " + userId.intValue());
                lgHandler.addLog(log);

                continue;
            }

            try {

                query = "SELECT * FROM EPR.STAFF WHERE " +
                    "STAFF_ID = " + userId.intValue();

                resultSet = dbcon.dbQuery(query);
                resultSet.next();

                //Block the user at server     
                query = "UPDATE EPR.STAFF SET DISABLE = 1 " +
                    "WHERE STAFF_ID = " + userId.intValue();

                dbcon.dbQueryUpdate (query);
            } catch (SQLException sql) {
                try {
                    serverMessage = serverMessage + "Användaren " + 
                        resultSet.getString("FIRST_NAME") + " " + 
                        resultSet.getString("LAST_NAME") + 
                        " kunde inte blockeras.\n" + sql.getMessage() +
                        "\n";

                    log = new Log(sql.getMessage(),
                            "UpdatePacket/readUpdates",
                            "Failed to block " + 
                            resultSet.getString("FIRST_NAME") +
                            resultSet.getString("LAST_NAME"));
                    lgHandler.addLog(log);

                    continue;
                } catch (SQLException sql2) {
                    serverMessage = serverMessage + sql2.getMessage() + "\n";

                    log = new Log(sql2.getMessage(),
                            "UpdatePacket/readUpdates",
                            "Failed to read first or last name");
                    lgHandler.addLog(log);

                    continue;
                }
            }

            try {

                //Insert this query into the CURRENT_UPDATES 
                query = "INSERT INTO EPR.CURRENT_UPDATES (INSERT_TIME, " +
                    "QUERY) VALUES (CURRENT TIMESTAMP, '" +
                    "UPDATE EPR.STAFF SET DISABLE = 1 " +
                    "WHERE STAFF_ID = " + userId.intValue() + "')";

                dbcon.dbQueryUpdate (query);
            } catch (SQLException sql) {
                try {
                    serverMessage = serverMessage + "Server Fel.\n" +
                        "Andra klienter kommer ej kunna få denna " +
                        "updatering. Kontakta administratören!\n" +
                        "Ärendet gäller användare \n" + 
                        resultSet.getString("FIRST_NAME") + " " + 
                        resultSet.getString("LAST_NAME") + 
                        "med användar Id = " + userId.intValue() + "\n" +
                        sql.getMessage() + "\n";

                    log = new Log(sql.getMessage(),
                            "UpdatePacket/readUpdates",
                            "Failed to insert the blocked user into "+
                            "CURRENT_UPDATES table, user: " +
                            resultSet.getString("FIRST_NAME") + " " +
                            resultSet.getString("LAST_NAME"));
                    lgHandler.addLog(log);

                    continue;
                } catch (SQLException sql2) {
                    serverMessage = serverMessage + sql2.getMessage() + "\n";

                    log = new Log(sql2.getMessage(),
                            "UpdatePacket/readUpdates",
                            "Failed to read first or last name");
                    lgHandler.addLog(log);

                    continue;
                }
            }

            try {
                serverMessage = serverMessage + "Användaren " + 
                    resultSet.getString("FIRST_NAME") + " " + 
                    resultSet.getString("LAST_NAME") + 
                    " har blockerats.\n";
            } catch (SQLException sql) {
                serverMessage = serverMessage + sql.getMessage() + "\n";

                log = new Log(sql.getMessage(), 
                        "UpdatePacket/readUpdates",
                        "Failed to read first or last name");
                lgHandler.addLog(log);

                continue;
            }
        }

        try {
            /*check to see if the client name exist*/
            query = "SELECT * FROM EPR.AMBULANCE_LAST_UPDATE " +
                "WHERE CLIENT_ID = '" + clientId + "'";

            resultSet = dbcon.dbQuery(query);

            if (!resultSet.next()) {
                message =  "Klienten " + clientId + " är ej valid.";
                updateFailed = true;
                return;
            }
            /*If the execution goes wrong, save a log to inform the client*/
        } catch (SQLException sql) {
            message = "Servern misslyckades med att slå upp klient ide.";

            log = new Log(sql.getMessage(),
                    "UpdatePacket/readUpdates",
                    "Failed to read client id = " + clientId);
            lgHandler.addLog(log);

            updateFailed = true;
            return;
        }


        //If it is a first time synchronization
        if (!regularUpdate) {
            try {
                //Check in the HISTORY table to see if there are any updates
                query = "SELECT * FROM EPR.HISTORY ORDER BY INSERT_TIME";

                resultSet = dbcon.dbQuery(query);

                //If there are any updates in the HISTORY ,take them first
                while (resultSet.next()) {
                    ts = resultSet.getTimestamp("INSERT_TIME");
                    query = resultSet.getString("QUERY");
                    updates.add(new Update(ts, query));
                }

                /*Check in the CURRENT_UPDATES table to see if there are
                  any updates to fetch*/
                query = "SELECT * FROM EPR.CURRENT_UPDATES ORDER BY " +
                    "INSERT_TIME";

                resultSet = dbcon.dbQuery(query);

                //If there are not any updates to fetch
                if (!resultSet.next()) {
                    message = "Det fanns inga uppdateringar att hämta.";
                    return;
                }

                //If there are any updates in the CURRENT_UPDATE, take them
                do {
                    ts = resultSet.getTimestamp("INSERT_TIME");
                    query = resultSet.getString("QUERY");
                    updates.add(new Update(ts, query));
                }  while (resultSet.next());

                /*If queries failed, save a log to inform the client 
                  that updates could not be fetched*/
            } catch (SQLException sql) {
                message = "Servern misslyckades med att hämta uppdateringar.";

                log = new Log(sql.getMessage(),
                        "UpdatePacket/readUpdates",
                        "Failed to read the updates");
                lgHandler.addLog(log);

                updateFailed = true;
                return;
            }
            //Otherwise if a regular update is requested
        } else {
            try {
                /*See if the server version is the same as the client
                  version.*/
                query = "SELECT * FROM EPR.AMBULANCE_LAST_UPDATE " +
                    "WHERE LAST_UPDATE < '" + clientUpdateTime + "' AND " +
                    "CLIENT_ID = '" + clientId + "'";

                resultSet = dbcon.dbQuery(query);

                /*If the server version is older than the client version, 
                  update the server version.
                  This means an acknowledgement from last time has been
                  lost*/
                if(resultSet.next()) {
                    query = "UPDATE EPR.AMBULANCE_LAST_UPDATE SET " +
                        "LAST_UPDATE = '" + clientUpdateTime + "' WHERE " +
                        "CLIENT_ID = '" + clientId + "'";

                    dbcon.dbQueryUpdate (query);
                }
                //If the query failed, save a log to inform the client 
            } catch (SQLException sql) {
                message = "Servern kunde inte updatera " + 
                    "sin uppdateringsversion.";

                log = new Log(sql.getMessage(),
                        "UpdatePacket/readUpdates",
                        "Failed to update client update version");
                lgHandler.addLog(log);

                updateFailed = true;
                return;
            }

            try {
                //Get the appropriate updates for this client
                query = "SELECT * FROM EPR.CURRENT_UPDATES WHERE " +
                    "INSERT_TIME > '" + clientUpdateTime + "'";

                resultSet = dbcon.dbQuery(query);

                //If there are not any updates to fetch
                if (!resultSet.next()) {
                    message = "Det fanns inga uppdateringar att hämta.";
                    return;
                }

                //If there are any updates fetch them
                do {
                    ts = resultSet.getTimestamp("INSERT_TIME");
                    query = resultSet.getString("QUERY");
                    updates.add(new Update(ts, query));
                } while (resultSet.next());

                /*If the query failed, save a log to inform the client*/
            } catch (SQLException sql) {
                message = "Servern misslyckades med att hämta uppdateringar.";

                log = new Log(sql.getMessage(), 
                        "UpdatePacket/readUpdates",
                        "Failed to read updates");
                lgHandler.addLog(log);

                updateFailed = true;
            }
        }
    }

    /**
     * Method used to get the status message, this method is used by 
     * AmbulanceUpdateClient
     * @return Message to informing the user about the update status
     */
    public String getMessage() {
        return serverMessage + message;
    }

    /**
     * Method used to inform the client the update status
     * @return true if the update has failed otherwise false
     */   
    public boolean doesUpdateFailed() {
        return updateFailed;
    }

    /**
     * Method used to get the client id for the client that 
     * request an update
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Method used to inform if this update is an regular update
     * @return true if this update is a regular update otherwise if 
     * this update is a first time update false
     */
    public boolean isRegularUpdate() {
        return regularUpdate;
    }

    /**
     * Method used to get the ambulance client update version 
     * @return a timestamp for the last succeded update that has
     * been performed
     */ 
    public Timestamp getClientUpdateTime() {
        return clientUpdateTime;
    }

    /**
     * Method used to execute the received updates  at the client side
     * @param dbcon is the reference to the client database connection
     */
    public void writeUpdates(DB2Connect dbcon, LogHandler lgHandler) {
        Update u;
        //used to count the number of succeded inserts 
        int queryCount = 0;
        String query;
        Log log;

        /*If there are no updates, Update SETTINGS table with 
          servet contact time and server message*/
        if (updates.size() == 0) {
            query = "UPDATE EPR.SETTINGS SET DATABASE_MESSAGE = '" + 
                message + "', SERVER_CONTACT_TIME = CURRENT TIMESTAMP";
            try {
                dbcon.dbQueryUpdate (query);
                return;
            } catch (SQLException sql) {
                message = "Kunde inte uppdatera " +
                    "DATABASE_MESSAGE/SERVER_CONTACT_TIME. " +
                    "Kontakta administratören!";

                log = new Log(sql.getMessage(),
                        "UpdatePacket/writeUpdates",
                        "Failed to update SETTINGS table");
                lgHandler.addLog(log);

                updateFailed = true;
                return;
            }
        }

        //begin execute the received queries
        for (queryCount = 0; queryCount < updates.size(); queryCount++) {
            try {
                u = (Update)updates.elementAt(queryCount);
                dbcon.dbQueryUpdate (u.getQuery());
                //If an update fails, don't execute the rest
            } catch (SQLException sql) {
                /*If the update does already exist, ignore it and
                  continue with others*/
                if(sql.getSQLState().equals("23505")) {
                    //System.out.println("Uppdateringen fanns redan");
                    continue;
                    /*Otherwise if there are other problems,  do not
                      run the rest of the update sequence*/
                } else {
                    log = new Log(sql.getMessage(),
                            "UpdatePacket/writeUpdates",
                            "Failed to execute the " + queryCount + 
                            ":th query");
                    lgHandler.addLog(log);

                    break;
                }
            }
        }

        message = queryCount + " av " + updates.size() + 
            " uppdateringar lyckades.";

        /*Check to get the right update version for latest succeded version.
          This time will be used to update the server version and the client 
          version*/
        if ((queryCount <= updates.size()) && (queryCount != 0)) {
            u = (Update) updates.elementAt(queryCount -1);
            clientUpdateTime = u.getInsertTime();
        }

        /*Update the SETTINGS table with version, message and server
          contact time*/

        //First time synchronization and client has no version available
        if (clientUpdateTime == null) {
            query = "UPDATE EPR.SETTINGS SET DATABASE_MESSAGE = '" + message + 
                "', SERVER_CONTACT_TIME = CURRENT TIMESTAMP";
        } else {
            query = "UPDATE EPR.SETTINGS SET UPDATE_VERSION = '" +
                clientUpdateTime + "'" + ", DATABASE_MESSAGE = '" + message +
                "', SERVER_CONTACT_TIME = CURRENT TIMESTAMP";
        }

        try {
            dbcon.dbQueryUpdate (query);
            /*If the the client doing a first time synchronization
              and it goes wrong */
            if ((queryCount == 0) && (!regularUpdate)) {
                message = "Klienten kunde inte konfigureras. " +
                    "Kontakta administratören.";
                updateFailed = true;
                return;
            }

            /*If the client doing a regular update and it goes wrong*/
            if((queryCount == 0) && (regularUpdate)) {
                message = "Kunde inte utföra uppdateringarna. Försök igen.";
                updateFailed = true;
                return;
            }

            /*Otherwise the client has been synchronized before and
              has failed this time with all or part of updates or
              some of the updates for the first time synchronization
              has been performed*/
            updateFailed = false;

        } catch (SQLException sql) {
            //System.err.println("SQLEXCEPtion: " + sql.getMessage());

            message = "Klientens konfigurationstabell kunde inte uppdateras." +
                "Kontakta administratören";

            log = new Log(sql.getMessage(),
                    "UpdatePacket/writeUpdates",
                    "Failed to update SETTINGS table");
            lgHandler.addLog(log);

            updateFailed = true;
        }
    }
}
