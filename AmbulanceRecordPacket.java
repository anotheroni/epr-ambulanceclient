import java.sql.*;
import java.io.*;

/**
 * AmbulanceRecordPacket represent the record information
 * that will be send from an ambulance client to the server
 *
 * @author Kane Neman
 * @version 20030904
 */
public class AmbulanceRecordPacket implements Serializable {

    private Date date = null;
    private int ambulanceNumber = 0;
    private int stationId = 0;
    private int prioOut = 0;
    private int prioIn = 0;
    private float drivedKm = 0;
    private int alarmCauseId = 0;
    private String alarmCause = null;
    private Time alarmTime = null;
    private Time leftStationTime = null;
    private String accidentScene = null;
    private Time arrivalAccidentTime = null;
    private Time leftAccidentTime = null;
    private String hospitalName = null;
    private Time handOverTime = null;
    private Time missionClosedTime = null;
    private int driverId = 0;
    private int carerId = 0;
    private Timestamp signTime = null;
    private int signId = 0;
    private String delegatingDoctor = null;
    private int diagnosisId = 0;
    private int accidentCityId = 0;
    private int accidentZoneId = 0;
    private int accidentPlaceId = 0;
    private int dropOffCityId = 0;
    private int dropOffPlaceId = 0;
    private int dropOffZoneId = 0;
    private Timestamp creationTime = null;
    private boolean recordExist;
    private boolean signTimeExist;

    /**
     * Constructor used by Record object to re-initialize this object
     */
    public AmbulanceRecordPacket() { }

    /**
     * Constructor used by ambulance client to create an AmbulanceRecordPacket
     * @param recordId is the record id for the patient record that 
     *                 will be send to the server
     * @param db2 is the database connection to the ambulance client database
     */ 
    public AmbulanceRecordPacket(int recordId, DB2Connect db2)
        throws SQLException {

            String query = "SELECT * FROM EPR.AMBULANCE_RECORD WHERE " +
                "RECORD_ID =" + recordId;

            ResultSet resultSet = db2.dbQuery(query);
            resultSet.next();

            date = resultSet.getDate("DATE");
            ambulanceNumber = resultSet.getInt("AMBULANCE_NR");
            stationId = resultSet.getInt("STATION_ID");
            prioOut = resultSet.getInt("PRIORITY_OUT");
            prioIn = resultSet.getInt("PRIORITY_IN");
            drivedKm = resultSet.getFloat("DRIVED_KM");
            alarmCauseId = resultSet.getInt("ALARM_CAUSE_ID");
            alarmCause = resultSet.getString("ALARM_CAUSE");
            alarmTime = resultSet.getTime("ALARM_TIME");
            leftStationTime = resultSet.getTime("LEFT_STATION_TIME");
            accidentScene = resultSet.getString("ACCIDENT_SCENE");
            arrivalAccidentTime = resultSet.getTime("ARRIVAL_ACCIDENT_TIME");
            leftAccidentTime = resultSet.getTime("LEFT_ACCIDENT_TIME");
            hospitalName = resultSet.getString("HOSPITAL_NAME");
            handOverTime = resultSet.getTime("HAND_OVER_TIME");
            missionClosedTime = resultSet.getTime("MISSION_CLOSED_TIME");
            driverId = resultSet.getInt("DRIVER_ID");
            carerId = resultSet.getInt("CARER_ID");
            signTime = resultSet.getTimestamp("SIGN_TIME");
            signId = resultSet.getInt("SIGN_ID");
            delegatingDoctor = resultSet.getString("DELEGATING_DOCTOR");
            diagnosisId = resultSet.getInt("DIAGNOSIS_ID");
            accidentCityId = resultSet.getInt("ACCIDENT_CITY");
            accidentZoneId = resultSet.getInt("ACCIDENT_ZONE");
            accidentPlaceId = resultSet.getInt("ACCIDENT_PLACE");
            dropOffCityId = resultSet.getInt("DROPOFF_CITY");
            dropOffPlaceId = resultSet.getInt("DROPOFF_PLACE");
            dropOffZoneId = resultSet.getInt("DROPOFF_ZONE");
            creationTime = resultSet.getTimestamp("CREATION_TIME");
        }

    /**
     * Method used by the Record object to tell this object to input record
     * information to the server database
     * @param db2 is the database connection at the server side
     */
    public int writeRecordPacket(DB2Connect db2) throws SQLException {

        String query;
        int recordId;
        ResultSet resultSet;

        query = "SELECT RECORD_ID FROM EPR.AMBULANCE_RECORD WHERE DATE = '" +
            date + "'AND ALARM_TIME = '" + alarmTime + "' AND " +
            "CREATION_TIME = '" + creationTime + "' AND AMBULANCE_NR = " + 
            ambulanceNumber;

        resultSet = db2.dbQuery(query);

        /*
         * If the record has been inserted to the central database
         * before do not insert it again, return only the record's 
         * record id 
         */
        if(resultSet.next()) {
            recordId = resultSet.getInt("RECORD_ID");
            recordExist = true;
            signTimeExist = true;

            //Otherwise if the record does not exist, insert it
        } else {
            query = "INSERT INTO EPR.AMBULANCE_RECORD " +
                "(DATE, AMBULANCE_NR, STATION_ID, " +
                "PRIORITY_OUT, PRIORITY_IN, DRIVED_KM, ALARM_CAUSE, " +
                "ALARM_TIME, LEFT_STATION_TIME, ACCIDENT_SCENE,  " +
                "ARRIVAL_ACCIDENT_TIME, LEFT_ACCIDENT_TIME, HOSPITAL_NAME, " +
                "HAND_OVER_TIME, MISSION_CLOSED_TIME, DRIVER_ID, CARER_ID, " +
                "SIGN_TIME, SIGN_ID, DELEGATING_DOCTOR, CREATION_TIME) " +
                "VALUES ('" + date + "'," + ambulanceNumber + "," + 
                stationId +"," + prioOut + "," + prioIn + "," + drivedKm + 
                ",'" + alarmCause + "','" + alarmTime +"'," ;

            if (leftStationTime == null) {
                query = query + null + ",'" + accidentScene + "',";
            } else {
                query = query + "'" + leftStationTime + "','" +
                    accidentScene + "',";
            }

            if (arrivalAccidentTime == null) {
                query = query + null + "," ;
            } else {
                query = query + "'" + arrivalAccidentTime + "',";
            }

            if (leftAccidentTime == null) {
                query = query + null + ",'" + hospitalName + "',";
            } else {
                query = query + "'" + leftAccidentTime + "','" + 
                    hospitalName + "',";
            }

            if (handOverTime == null) {
                query = query + null + ",";
            } else {
                query = query + "'" + handOverTime + "',";
            }

            if (missionClosedTime == null) {
                query = query + null + "," + driverId + "," + carerId + ",";
            } else {
                query = query + "'" + missionClosedTime + "'," +
                    driverId + "," + carerId + ",";
            }

            if (signTime == null) {
                query = query + null + "," + signId + ",'" + 
                    delegatingDoctor + "','" + creationTime + "')";
                signTimeExist = false;
            } else {
                query = query + "'" + signTime + "'," + signId + ",'" +
                    delegatingDoctor + "','" + creationTime + "')";
                signTimeExist = true;
            }

            //Insert the record into the database
            db2.dbQueryUpdate (query);

            //Fetch the server record id
            query = "SELECT RECORD_ID FROM EPR.AMBULANCE_RECORD " +
                "WHERE DATE = '" + date + "' AND ALARM_TIME = '" +
                alarmTime + "' AND CREATION_TIME = '" + creationTime + 
                "' AND AMBULANCE_NR = " + ambulanceNumber;

            resultSet = db2.dbQuery(query);
            resultSet.next();
            recordId = resultSet.getInt(1);
            recordExist = false;

            //Check diagnosis, zone, place and uppdate the patient record
            query = "UPDATE EPR.AMBULANCE_RECORD SET ";
            
            if (diagnosisId != 0)
                query = query + "DIAGNOSIS_ID = " + diagnosisId + ", ";
            
            if (alarmCauseId != 0)
                query = query + "ALARM_CAUSE_ID = " + alarmCauseId + ", ";
            
            if (accidentCityId != 0)
                query = query + "ACCIDENT_CITY = " + accidentCityId + ", ";
            
            if (accidentZoneId != 0)
                query = query + "ACCIDENT_ZONE = " + accidentZoneId + ", ";
            
            if (accidentPlaceId != 0)
                query = query + "ACCIDENT_PLACE = " + accidentPlaceId + ", ";

            if (dropOffCityId != 0)
                query = query + "DROPOFF_CITY = " + dropOffCityId + ", " ;
            
            if (dropOffPlaceId != 0)
                query = query + "DROPOFF_PLACE = " + dropOffPlaceId + ", ";
            
            if(dropOffZoneId != 0)
                query = query + "DROPOFF_ZONE = " + dropOffZoneId;
            /*Dummy update of DOCTOR_ID parameter if some or none of 
              the previous parameters has 0 value*/
            else
                query = query + "DELEGATING_DOCTOR = '" + 
                    delegatingDoctor + "'";

            query = query + " WHERE RECORD_ID = " + recordId;
            
            db2.dbQueryUpdate (query);
        }
        return recordId;
    }

    /**
     * Method used to indicate if a record does already exists in the
     * server database. This method is used by Record object
     * @return true if a record does exist in the database, otherwise false
     */ 
    public boolean doesRecordExist() {
        return recordExist;
    }

    /**
     * Method used to indicate if a record is signed. This method is used
     * by Record object.
     * @return true if the record is signed, otherwise false
     */ 
    public boolean doesSignTimeExist() {
        return signTimeExist;
    }
}
