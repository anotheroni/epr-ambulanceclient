import java.sql.*;
import java.io.*;
import java.util.Vector;
import java.lang.*;

/**
 * AmbulanceExaminationPacket is used to serialize the Examination part
 * of the patient record.
 *
 * @author Kane Neman
 * @version 20030904
 */ 
public class AmbulanceExaminationPacket implements Serializable {

    private Vector examinations;
    private Vector examinationDescriptions;

    /**
     * Constructor used by Record object to re-initialize this object
     */
    public AmbulanceExaminationPacket() {}

    /**
     * Constructor used by ambulance client to create this packet 
     * @param recordId is the record id for the patient record
     *                 will be send to the server
     * @param db2 is the database connection to the ambulance client database
     */ 
    public AmbulanceExaminationPacket(int recordId, DB2Connect db2) 
        throws SQLException {

            String query;
            ResultSet resultSet;
            ExaminationDescriptionPacket edp;
            
            examinations = new Vector();
            examinationDescriptions = new Vector();

            //Fetch the attributes that are examined for this record id 
            query = "SELECT EXAMINATION_ATTRIBUTE FROM EPR.EXAMINATION " +
                "WHERE RECORD_ID = " + recordId;

            resultSet = db2.dbQuery(query);

            //Save the attribute ids in examinations vector
            while (resultSet.next()) {
                examinations.add(new Integer
                        (resultSet.getInt("EXAMINATION_ATTRIBUTE")));
            }

            query = "SELECT * FROM EPR.EXAMINATION_DESCRIPTION " +
                "WHERE RECORD_ID = " + recordId;

            resultSet = db2.dbQuery(query);

            //Save the examination type information
            while (resultSet.next()) {
                edp = new ExaminationDescriptionPacket(
                        resultSet.getInt("EXAMINATION_TYPE_ID"),
                        resultSet.getString("DESCRIPTION_FIELD"),
                        resultSet.getInt("WITHOUT_REMARK"));
                examinationDescriptions.add(edp);
            }
        }

    /**
     * Method used to write the examination informations to the server
     * database
     * @param db2 is the connection to the server database 
     * @param recordId is the record id at the server side 
     */ 
    public void writeExaminationPacket(DB2Connect db2, int recordId)
        throws SQLException {

            String query;
            Integer examinationAttribute;
            ExaminationDescriptionPacket edp;

            //If there are examinations, insert them to the server database
            for (int i = 0; i < examinations.size(); i++) {
                examinationAttribute = (Integer) examinations.elementAt(i);

                query = "INSERT INTO EPR.EXAMINATION (RECORD_ID, " +
                    "EXAMINATION_ATTRIBUTE) VALUES (" + recordId +
                    ", " + examinationAttribute.intValue() + ")";
                db2.dbQueryUpdate (query);
            }

            //If there are examination descriptions, insert them
            for (int i = 0; i < examinationDescriptions.size(); i++) {
                edp = (ExaminationDescriptionPacket) 
                    examinationDescriptions.elementAt(i);

                query = "INSERT INTO EPR.EXAMINATION_DESCRIPTION " +
                    "(RECORD_ID, EXAMINATION_TYPE_ID, DESCRIPTION_FIELD, " 
                    + "WITHOUT_REMARK) VALUES (" + recordId + ", " +
                    edp.getExaminationTypeId() + ", '" + 
                    edp.getDescriptionField() + "', " + 
                    edp.getWithoutRemark() + ")";

                db2.dbQueryUpdate (query);
            }
        }
}
