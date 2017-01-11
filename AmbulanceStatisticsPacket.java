import java.sql.*;
import java.io.*;

/**
 * AmbulanceStatisticsPacket contain the statistcal observations done by 
 * ambulance staff at accident place
 *
 * @author Kane Neman
 * @version 20030904
 */ 
public class AmbulanceStatisticsPacket implements Serializable {

    private int policeConnected;
    private int helmet;
    private int seatBelt;
    private int neckSupport;
    private int airbag;
    private boolean statisticalObservationExist;

    /**
     * Constructor used by Record object to re-initialize this object
     * at server side
     */ 
    public AmbulanceStatisticsPacket() { }
    
    /**
     * Constructor used by ambulance client to read the statistical
     * information from the client database
     * @param recordId is the patient record id at the client side
     * @param db2 is the database connection at the client side
     */
    public AmbulanceStatisticsPacket(int recordId, DB2Connect db2)
        throws  SQLException {
            
            /*Check if there are any statistical obsservations for this
              patient record*/
            String query = "SELECT * FROM EPR.STATISTICS WHERE RECORD_ID = " +
                recordId;
            ResultSet resultSet = db2.dbQuery(query);
            statisticalObservationExist = resultSet.next();
            
            //IF there are any information, store it
            if (statisticalObservationExist) {
                policeConnected = resultSet.getInt("POLICE_CONNECTED");
                helmet = resultSet.getInt("HELMET");
                seatBelt = resultSet.getInt("SEAT_BELT");
                neckSupport = resultSet.getInt("NECK_SUPPORT");
                airbag = resultSet.getInt("AIRBAG");
            }
        }

    /**
     * Method used by Record object to write the Statistics infomation
     * to the server database
     * @param db2 is the connection to the server database
     * @param recordId is the record id at the server side
     */ 
    public void writeStatisticPacket(DB2Connect db2, int recordId)
        throws SQLException {

            String query;

            /*If there are statistical information for this 
              patient record write it to the server database*/
            if (statisticalObservationExist) {
                query = "INSERT INTO EPR.STATISTICS " +
                    "(RECORD_ID, POLICE_CONNECTED, HELMET, SEAT_BELT, " +
                    "NECK_SUPPORT, AIRBAG) VALUES (" + recordId + "," +
                    policeConnected + "," + helmet + "," + seatBelt +
                    "," + neckSupport + "," + airbag + ")";
                
                db2.dbQueryUpdate (query);
            }
        }
}
