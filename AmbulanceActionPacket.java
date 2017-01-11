import java.sql.*;
import java.io.*;
import java.util.Vector;
import java.lang.*;

/**
 * AmbulanceActionPacket is used to serialize the action performed part
 * of the patient record
 *
 * @author Kane Neman
 * @version 20030904
 */
public class AmbulanceActionPacket implements Serializable {

    private Vector actions = null;
    private String actionDescription = null;

  /**
   * Constructor used by Record object to re-initialize this object
   */
  public AmbulanceActionPacket() { }  

  /**
   * Constructor used by ambulance client to create this packet
   * @param recordId The patient record id at the client side
   * @param db2 The client database connection
   */ 
  public AmbulanceActionPacket(int recordId, DB2Connect db2) 
      throws SQLException {
          
          String query;
          ResultSet resultSet;
          actions = new Vector();

          //Fetch the actions that are performed for this record id
          query = "SELECT ACTION_ATTRIBUTE_ID FROM EPR.ACTION_PERFORMED " +
              "WHERE RECORD_ID = " + recordId;

          resultSet = db2.dbQuery(query);

          //Save the action attribute ids in actions Vector
          while (resultSet.next()) {
            actions.add(new 
                    Integer(resultSet.getInt("ACTION_ATTRIBUTE_ID")));
          }

          //Check if there is a description for actions
          query = "SELECT DESCRIPTION FROM EPR.ACTION_DESCRIPTION " +
              "WHERE RECORD_ID = " + recordId;

          resultSet = db2.dbQuery(query);

          //Fetch the result
          if (resultSet.next())
              actionDescription = resultSet.getString("DESCRIPTION");
      }

  /**
   * Method used to write this packet to the server database
   * @param db2 is the server database connection
   * @param recordId The patient record id at the server side
   */ 
  public void writeActionPacket(DB2Connect db2, int recordId) 
      throws SQLException {

          String query;
          Integer actionAttributeId;

          //Insert the actions performed for this record id
          for (int i = 0; i < actions.size(); i++) {
              actionAttributeId = (Integer) actions.elementAt(i);

              query = "INSERT INTO EPR.ACTION_PERFORMED " +
                  "(RECORD_ID, ACTION_ATTRIBUTE_ID) VALUES (" +
                  recordId + ", " + actionAttributeId.intValue() + ")";
              
              db2.dbQueryUpdate (query);
          }

          //If there is a description, insert it
          if (actionDescription != null) {
              query = "INSERT INTO EPR.ACTION_DESCRIPTION " +
                  "(RECORD_ID, DESCRIPTION) VALUES (" + 
                  recordId + ",'" + actionDescription + "')";

              db2.dbQueryUpdate (query);
          }
      }
}
