import java.io.*;
import java.sql.*;

/**
 * This object is used for serialization of patient information.
 *
 * @author Kane Neman
 * @version 20030904
 */ 
public class AmbulancePatientPacket implements Serializable { 

    private String personNumber = null;
    private String firstName = null;
    private String lastName = null;
    private String address = null;
    private String relative = null;

    /**
     * Empty contructor used by Record object to re-initialize this object
     */  
    public AmbulancePatientPacket() {}

    /**
     * This constructor is used by Ambulance client to create
     * the serialized object
     * @param recordId is the record id of the patient record
     *                 that will be send to the server
     * @param db2 is the database connection to the ambulance 
     *            client database   
     */ 
    public AmbulancePatientPacket(int recordId, DB2Connect db2) 
        throws SQLException {

            String query = "SELECT * FROM EPR.PATIENT WHERE RECORD_ID = " + 
                recordId;

            ResultSet resultSet = db2.dbQuery(query);
            boolean patientExists = resultSet.next();

            if (patientExists)
            {
               personNumber = resultSet.getString("PERSON_ID");
               firstName = resultSet.getString("FIRST_NAME");
               lastName = resultSet.getString("LAST_NAME");
               address = resultSet.getString("ADDRESS");
               relative = resultSet.getString("RELATIVE_INFORMATION");
            }
        }

    /** 
     * Method called by Record object, it will write the patient information
     * into the database on the server side 
     * @param db2 is the connection to server database
     * @param recordId is the record id for the patient record at
     *                 server side
     */ 
    public void writePatientPacket(DB2Connect db2, int recordId) 
        throws SQLException {

        String query;
        String[] personInformation;
        
        /**
         * If there is no person number information or the
         * information is not complete, Insert the available
         * information
         */
        if ((personNumber == null) || (personNumber.length() != 12)) {
            query = "INSERT INTO EPR.PATIENT " +
                "(RECORD_ID, PERSON_ID, FIRST_NAME, LAST_NAME, ADDRESS, " +
                "RELATIVE_INFORMATION, INCOMPLETE) VALUES (" + recordId +
                ",'" + personNumber +"','" + firstName + "','" + lastName +
                "','" + address + "','" + relative + "', 1)";

            db2.dbQueryUpdate (query);
            /**
             * If a person number is given, search after the person
             * in Person Number Register
             */ 
        } else {
            //Create a reference to PersonInformation object
            PersonInformation pI = new PersonInformation();

            try {
                pI.personSearch(personNumber);
                personInformation = pI.getInformation();
                query = "INSERT INTO EPR.PATIENT " +
                    "(RECORD_ID, PERSON_ID, FIRST_NAME, LAST_NAME, " +
                    "ADDRESS, ZIPCODE, CITY, COUNTY, RELATIVE_INFORMATION, " +
                    "INCOMPLETE) VALUES (" + recordId + ",'" + personNumber + 
                    "','" + personInformation[0] + "','" + 
                    personInformation[1] + "','" + personInformation[2] + 
                    "','" + personInformation[3] + "','" + 
                    personInformation[4] + "','" + personInformation[5] + 
                    "','" + relative + "', 0)";

                db2.dbQueryUpdate (query);
                /**
                 * If the given person number does not exist ,
                 * insert the known inforamtion to the database anyway
                 */     
            } catch(IllegalArgumentException g) {
                query = "INSERT INTO EPR.PATIENT " +
                    "(RECORD_ID, PERSON_ID, FIRST_NAME, LAST_NAME, ADDRESS, " +
                    "RELATIVE_INFORMATION, INCOMPLETE) VALUES (" + 
                    recordId + ",'" + personNumber +"','" + firstName + 
                    "','" + lastName + "','" + address + "','" + 
                    relative + "', 1)";

                db2.dbQueryUpdate (query);
            }
        }
    }
}

