import java.sql.*;
import java.io.*;
import java.util.Vector;
import java.lang.*;

/**
 * AmbulanceParameterPacket is used to serialize the observed patient  
 * parameters 
 *
 * @author Kane Neman
 * @version 20030904
 */
public class AmbulanceParameterPacket implements Serializable {

    //Regular parameters
    private Vector parameters = null;

    //Blood pressure 
    private Vector bloodPressure = null;

    //GCS
    private Vector gcs = null;

    //Pupil observations
    private Vector eye = null;

    //Body position
    private Vector body = null;

    //Fracture areas
    private byte[] fracture = null;

    //Luxation areas
    private byte[] luxation = null;

    private final int FRACTURE_AREAS = 23;
    private final int LUXATION_AREAS = 10;

    //Miscellaneous comments
    private String miscComments = null;

    //ParalysisPacket
    private ParalysisPacket pp = null;

    /**
     * Constructor used by Record object to re-initialize this object
     */
    public AmbulanceParameterPacket() { }

    /**
     * Constructor used by ambulance client to create this packet
     * @param recordId The patient record id at the client side
     * @param db2 The client database connection
     */ 
    public AmbulanceParameterPacket(int recordId, DB2Connect db2) 
        throws SQLException {

            String query;
            ResultSet resultSet;

            parameters = new Vector();
            bloodPressure = new Vector();
            gcs = new Vector();
            eye = new Vector();
            body = new Vector();
            
            ParameterPacket p;
            BloodPressurePacket b;
            GCSPacket g;
            EyePacket e;
            BodyPositionPacket bpp;

            //Fetch observed paramters for this record id
            query = "SELECT * FROM EPR.OBSERVATIONS_OF_PATIENT_PARAMETERS " +
                "WHERE RECORD_ID = " + recordId;

            resultSet = db2.dbQuery(query);

            //Store regular parameters in parameters Vector
            while (resultSet.next()) {
                p = new ParameterPacket(resultSet.getTime("OBSERVATION_TIME"),
                        resultSet.getInt("OBSERVATION_PARAMETER_ID"),
                        resultSet.getString("VALUE"));
                parameters.add(p);
            }

            //System.out.println("OBSERVATIONS_OF_PATIENT_PARAMETERS pass");

            //Fetch fracture areas
            query = "SELECT * FROM EPR.FRACTURE WHERE RECORD_ID = " +
                recordId;

            resultSet = db2.dbQuery(query);

            //Store areas in the fracture
            if (resultSet.next()) {
                fracture = new byte[FRACTURE_AREAS];
                for (int i = 0; i < FRACTURE_AREAS; i++)
                    fracture[i] = resultSet.getByte(i + 2);
            }

            //System.out.println("FRACTURE pass");

            //Fetch luxation areas
            query = "SELECT * FROM EPR.LUXATION WHERE RECORD_ID = " +
                recordId;

            resultSet = db2.dbQuery(query);

            //Store areas in the luxation
            if (resultSet.next()) {
                luxation = new byte[LUXATION_AREAS];
                for (int i = 0; i < LUXATION_AREAS; i++)
                    luxation[i] = resultSet.getByte(i +2);
            }

            //System.out.println("LUXATION pass");

            //Fetch blood pressure observations
            query = "SELECT * FROM EPR.BLOOD_PRESSURE WHERE RECORD_ID = " +
                recordId;

            resultSet = db2.dbQuery(query);

            //Store blood pressure observations in bloodPressure
            while (resultSet.next()) {
                b = new BloodPressurePacket(
                        resultSet.getTime("OBSERVATION_TIME"),
                        resultSet.getInt("DIASTOL"),
                        resultSet.getInt("SYSTOL"));
                bloodPressure.add(b);
            }

            //System.out.println("BLOOD_PRESSURE pass");

            //Fetch GCS observations
            query = "SELECT * FROM EPR.GCS WHERE RECORD_ID = " +
                recordId;

            resultSet = db2.dbQuery(query);

            //Store GCS observations in gcs
            while (resultSet.next()) {
                g = new GCSPacket(resultSet.getTime("OBSERVATION_TIME"),
                        resultSet.getInt("EYES"),
                        resultSet.getInt("MOTOR_ACTIVITY"),
                        resultSet.getInt("VERBAL"));
                gcs.add(g);
            }

            //System.out.println("GCS pass");

            //Fetch Eye observations
            query = "SELECT * FROM EPR.EYE WHERE RECORD_ID = " +
                recordId;

            resultSet = db2.dbQuery(query);

            //Store Eye observations in eye
            while (resultSet.next()) {
                e = new EyePacket(resultSet.getTime("OBSERVATION_TIME"),
                        resultSet.getString("LEFT"),
                        resultSet.getString("RIGHT"));
                eye.add(e);
            }

            //System.out.println("EYE pass");
            
            //Fetch body positions
            query = "SELECT * FROM EPR.BODY_POSITION_OBSERVATION " +
                "WHERE RECORD_ID = " + recordId;

            resultSet = db2.dbQuery(query);

            //Store body positions in body vector
            while (resultSet.next()) {
                bpp = new BodyPositionPacket(
                        resultSet.getTime("OBSERVATION_TIME"),
                        resultSet.getInt("POSITION"));
                body.add(bpp);
            }

            //System.out.println("BODY_POSITION_OBSERVATION pass");
            
            //Fetch miscellaneous comments
            query = "SELECT * FROM EPR.MISC WHERE RECORD_ID = " +
                recordId;
            
            resultSet = db2.dbQuery(query);
            
            //Store the comments 
            if (resultSet.next())
                miscComments = resultSet.getString("MISCTEXT");

            //System.out.println("MISC pass");
            
            //Fetch paralysis observation 
            query = "SELECT * FROM EPR.PARALYSIS WHERE RECORD_ID = " +
                recordId;
            
            resultSet = db2.dbQuery(query);
            
            if (resultSet.next())
                pp = new ParalysisPacket(
                        resultSet.getInt("RIGHT_SIDE"),
                        resultSet.getInt("LEFT_SIDE"));

            //System.out.println("PARALYSIS pass");
        }

    /**
     * Method used to write this packet to the server database
     * @param db2 is the server database connection
     * @param recordId The patient record id at the server side
     */ 
    public void writeParameterPacket(DB2Connect db2, int recordId) 
        throws SQLException {

            String query1;
            String query2;
            ParameterPacket p;
            BloodPressurePacket b;
            GCSPacket g;
            EyePacket e;
            BodyPositionPacket bpp;

            if (parameters.size() > 0) {
                query1 = 
                    "INSERT INTO EPR.OBSERVATIONS_OF_PATIENT_PARAMETERS " +
                    "(OBSERVATION_TIME, RECORD_ID, " +
                    "OBSERVATION_PARAMETER_ID, VALUE) VALUES ('";

                //Insert the parameter packet for this record id
                for (int i = 0; i < parameters.size(); i++) {
                    p = (ParameterPacket) parameters.elementAt(i);

                    query2 = query1 +  p.getObsTime() + "', " + recordId + 
                        ", " +p.getParamId() + ", '" + 
                        p.getParamValue() + "')";

                    db2.dbQueryUpdate (query2);
                }
            }

            //System.out.println("OBSERVATIONS_OF_PATIENT_PARAMETERS pass");
            
            if (fracture != null) {
                //Insert the fracture areas
                query1 = "INSERT INTO EPR.FRACTURE VALUES (";
                query2 = " " + recordId;

                for (int i = 0; i < FRACTURE_AREAS; i++) { 
                    if (fracture[i] == 0)
                        query2 = query2 + "," + null;
                    else
                        query2 = query2 + ",'" + (char)fracture[i] + "'";
                }
                //Build the final query
                query1 = query1 + query2 + ")";

                //System.out.println(query1);

                db2.dbQueryUpdate (query1);
            }

            //System.out.println("FRACTURE pass");

            if (luxation != null) {
                //Insert the luxation areas
                query1 = "INSERT INTO EPR.LUXATION VALUES (";
                query2 = " " + recordId;

                for (int i = 0; i < LUXATION_AREAS; i++)
                    query2 = query2 + ",'" + (char)luxation[i] + "'";

                //Build the final query
                query1 = query1 + query2 + ")";

                //System.out.println(query1);

                db2.dbQueryUpdate (query1);
            }

            //System.out.println("LUXATION pass");

            if (bloodPressure.size() > 0) {
                //Insert the blood pressure observations
                query1 = "INSERT INTO EPR.BLOOD_PRESSURE ( " +
                    "OBSERVATION_TIME, RECORD_ID, DIASTOL, SYSTOL ) VALUES" +
                    "('";

                //Insert the blood pressure observations
                for (int i = 0; i < bloodPressure.size(); i++) {
                    b = (BloodPressurePacket) bloodPressure.elementAt(i);

                    query2 = query1 + b.getObsTime() + "'," + recordId + 
                        ", " + b.getDiastol() + ", " + b.getSystol() + ")";

                    db2.dbQueryUpdate (query2);
                }
            }

            //System.out.println("BLOOD_PRESSURE pass");

            if (gcs.size() > 0) {
                //Insert GCS observations
                query1 = "INSERT INTO EPR.GCS (" +
                    "OBSERVATION_TIME, RECORD_ID, EYES, MOTOR_ACTIVITY, " +
                    "VERBAL) VALUES ('";

                for (int i = 0; i < gcs.size(); i++) {
                    g = (GCSPacket) gcs.elementAt(i);

                    query2 = query1 + g.getObsTime() + "'," + recordId + "," +
                        g.getEyeValue() + "," + g.getMotorValue() + "," +
                        g.getVerbalValue() + ")";

                    db2.dbQueryUpdate (query2);
                }
            }

            //System.out.println("GSC pass");
            
            if (eye.size() > 0) {
                //Insert the pupil observations
                query1 = "INSERT INTO EPR.EYE ( " +
                    "OBSERVATION_TIME, RECORD_ID, LEFT, RIGHT) " +
                    "VALUES ('";

                for (int i = 0; i < eye.size(); i++) {
                    e = (EyePacket) eye.elementAt(i);
                    query2 = query1 + e.getObsTime() + "'," + recordId + ",'" +
                        e.getLeft() + "','" + e.getRight() + "')";

                    db2.dbQueryUpdate (query2);
                }
            }

            //System.out.println("EYE pass");
            
            if (body.size() > 0) {
                //Insert the body position observations
                query1 = "INSERT INTO EPR.BODY_POSITION_OBSERVATION " +
                    "(OBSERVATION_TIME, RECORD_ID, POSITION) VALUES ('";

                for (int i = 0; i < body.size(); i++) {
                    bpp = (BodyPositionPacket) body.elementAt(i);

                    query2 = query1 + bpp.getObsTime() + "'," + recordId +
                        "," + bpp.getPosition() + ")";

                    db2.dbQueryUpdate (query2);
                }
            }

            //System.out.println("BODY_POSITION_OBSERVATION pass");
            
            if (miscComments != null) {
                //Insert miscellaneous comments
                query1 = "INSERT INTO EPR.MISC (RECORD_ID, MISCTEXT) "+
                    "VALUES (" + recordId + ",'" + miscComments + "')";

                db2.dbQueryUpdate (query1);
            }

            //System.out.println("MISC pass");

            if (pp != null) {
                //Insert paralysis observations
                query1 = "INSERT INTO EPR.PARALYSIS " +
                    "(RECORD_ID, RIGHT_SIDE, LEFT_SIDE) VALUES (" +
                    recordId + "," + pp.getRightSide() + "," +
                    pp.getLeftSide() + ")";

                db2.dbQueryUpdate (query1);
            }

            //System.out.println("PARALYSIS pass");
        }
}
