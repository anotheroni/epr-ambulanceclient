import java.sql.*;
import java.io.*;

/**
 * AmbulanceMatasPacket contain the patient medicine, last meal, 
 * anamnesis and diagnosis
 *
 * @author Kane Neman
 * @version 20030904
 */ 
public class AmbulanceMatasPacket implements Serializable {

    //MATAS parameters
    private String medicine;
    private String allergy;
    private String anamnesis;
    private String diagnosis;
    private String lastMeal;

    /**
     * Constructor used by Record object to re-initialize this object 
     * at server side
     */ 
    public AmbulanceMatasPacket() { }

    /**
     * Constructor used by ambulance client to read the MATAS information
     * @param recordId is the patient record id at the ambulance client
     * @param db2 is the database connection to the ambulance client database
     */
    public AmbulanceMatasPacket(int recordId, DB2Connect db2) 
        throws SQLException {

            String query = "SELECT * FROM EPR.MATAS WHERE RECORD_ID = " +
                recordId;
            ResultSet resultSet = db2.dbQuery(query);

            //IF there are information store it
            if (resultSet.next()) {
                medicine = resultSet.getString("MEDICINE");
                allergy = resultSet.getString("ALLERGY");
                anamnesis = resultSet.getString("ANAMNESIS");
                diagnosis = resultSet.getString("DIAGNOSIS");
                lastMeal = resultSet.getString("LAST_MEAL");
            }
        }

        /**
         * Method used by Record object to write the MATAS information
         * to the server database 
         * @param db2 is the connection to the server database
         * @param recordId is the record id at the server side
         */    
        public void writeMatasPacket(DB2Connect db2, int recordId) 
            throws  SQLException {

                String query;

                query = "INSERT INTO EPR.MATAS " + 
                    "(RECORD_ID, MEDICINE, ALLERGY, " +
                    "ANAMNESIS, DIAGNOSIS, LAST_MEAL) VALUES ( " + 
                    recordId + ", '" + medicine + "','" + allergy + "','" + 
                    anamnesis + "','" +  diagnosis + "', '" + lastMeal + "')";

                db2.dbQueryUpdate (query);
            }
        }
