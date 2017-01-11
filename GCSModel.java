import java.util.*;
import java.sql.*;

/**
  * Class implementing a model for the GCS rows in the observation table.
  *
  * @version 20030313
  * @author Oskar Nilsson
  */
public class GCSModel
{
   private int recordId;
   private DB2Connect dbcon;
   private LogHandler lg;

   private GCSObservationTableRow rows[];
   private Vector values[];
   private Vector inDB;
   private Vector timeVec;

   /// Vector that keeps track of calls to changeTime
   private Vector changeTimeVec;

   private boolean datachanged = false;

   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   private final String[] eyeValues = {
      "4 - Normal/Spontan",
      "3 - Öppnas vid tilltal",
      "2 - Öppnas vid smärta",
      "1 - Ingen reaktion"
   };

   private final String[] speechValues = {
      "5 - Orienterad",
      "4 - Förvirrad",
      "3 - Osammanhängande begripbara ord",
      "2 - Grymtningar",
      "1 - Igen reaktion"
   };

   private final String[] moveValues = {
      "6 - Lyder uppmaningar",
      "5 - Lokaliserar smärta",
      "4 - Drar undan",
      "3 - Böjer",
      "2 - Sträcker",
      "1 - Ingen reaktion"
   };

   /**
     * Construcor, initzialises the local variables and creates the three
     * GCSObservationTableRow objects.
     * @param recordId The current record id.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param times The rows that exists in the table.
     */
   public GCSModel(int recordId, DB2Connect dbcon, LogHandler lg, Vector times)
   {
      this.recordId = recordId;
      this.dbcon = dbcon;
      this.lg = lg;

      inDB = new Vector();
      changeTimeVec = new Vector();
      timeVec = new Vector();

      rows = new GCSObservationTableRow[3];

      rows[0] = new GCSObservationTableRow(this, 0, eyeValues,
           "GCS ögon");
      rows[1] = new GCSObservationTableRow(this, 1, speechValues,
            "GCS tilltal");
      rows[2] = new GCSObservationTableRow(this, 2, moveValues,
            "GCS rörelser");

      values = new Vector[3];
      values[0] = new Vector();
      values[1] = new Vector();
      values[2] = new Vector();

      readData(times);
   }

   /**
     * Method that reads GCS data from the database.
     * @param times The times of the columns that already exists.
     */
   private void readData(Vector times)
   {
      ResultSet rs;
      Iterator iter = times.iterator();
      java.util.Date tableTime;
      boolean rsHasNext;
      try {
         tableTime = (java.util.Date) iter.next();
      } catch (NoSuchElementException e) { tableTime = null; }
      
      try {
         rs = dbcon.dbQuery(
               "SELECT OBSERVATION_TIME, EYES, MOTOR_ACTIVITY, VERBAL " +
               "FROM EPR.GCS WHERE RECORD_ID = " + recordId +
               " ORDER BY OBSERVATION_TIME");
         rsHasNext = rs.next();
         // Merge time vectors
         while (rsHasNext && tableTime != null)
         {
            // A new column that doesn't exist in the table
            // true if rs.getTime is before tableTime
            if (tableTime.compareTo(rs.getTime(1)) > 0)
            {
               values[0].add(new Integer(rs.getInt(2))); // EYES
               values[1].add(new Integer(rs.getInt(3))); // MOTOR_ACTIVITY
               values[2].add(new Integer(rs.getInt(4))); // VERBAL
               inDB.add(TRUE);
               timeVec.add(rs.getTime(1));   // OBSERVATION_TIME
               rsHasNext = rs.next(); 
            }
            // A column that exists in both db and the table
            else if (tableTime.compareTo(rs.getTime(1)) == 0)
            {
               values[0].add(new Integer(rs.getInt(2))); // EYES
               values[1].add(new Integer(rs.getInt(3))); // MOTOR_ACTIVITY
               values[2].add(new Integer(rs.getInt(4))); // VERBAL
               inDB.add(TRUE);
               if (iter.hasNext())
                  tableTime = (java.util.Date)iter.next();
               else
                  tableTime = null;
               rsHasNext = rs.next(); 
            }
            // A row that only exists in the table
            else
            {
               values[0].add(null);
               values[1].add(null);
               values[2].add(null);
               inDB.add(FALSE);
               if (iter.hasNext())
                  tableTime = (java.util.Date)iter.next();
               else
                  tableTime = null;
            }
         }
         // Add columns left from the merge loop
         while (tableTime != null)
         {
            values[0].add(null);
            values[1].add(null);
            values[2].add(null);
            inDB.add(FALSE);
            if (iter.hasNext())
               tableTime = (java.util.Date)iter.next();
            else
               tableTime = null;
         }
         while (rsHasNext)
         {
            values[0].add(new Integer(rs.getInt(2))); // EYES
            values[1].add(new Integer(rs.getInt(3))); // MOTOR_ACTIVITY
            values[2].add(new Integer(rs.getInt(4))); // VERBAL
            inDB.add(TRUE);
            timeVec.add(rs.getTime(1));   // OBSERVATION_TIME
            rsHasNext = rs.next(); 
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "GCSModel/readData",
                  "Read patient parameters"));
      }
   }

   /**
     * Method that returns the specified GCS row model.
     * @param row Index of the row to get.
     * @return The requested row model.
     */
   public GCSObservationTableRow getRowModel(int row)
   {
      try {
         return rows[row];
      } catch (ArrayIndexOutOfBoundsException e) { return null; }
   }

   /**
     * Method used by the table to check if the row has any columns that 
     * doesn't exist in the table.
     * @return The new times in the row that doesn't exist in the table.
     */
   public Vector getNewTimes()
   {
      return timeVec;
   }

   /**
     * Method that adds a new column at the end of the row with value val.
     * @param paramId The GCS observation type index (row nr).
     * @param val The value of the column. (not used)
     * @param col The column number to add.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String addColumn(int paramId, String val, int col)
   {
      // Only request from row 0 are processed. (This is a bad solution)
      if (paramId != 0)
         return null;

      try {
         values[0].add(col, null);
         values[1].add(col, null);
         values[2].add(col, null);
         inDB.add(col, FALSE);
      } catch (ArrayIndexOutOfBoundsException e) {
         for (int i = values[0].size() ; i <= col ; i++)
         {
            values[0].add(null);
            values[1].add(null);
            values[2].add(null);
            inDB.add(FALSE);
         }
      }
      return null;
   }

    /**
     * Method that returns the value in the specified column.
     * @param paramId The GCS observation type index (row nr).
     * @param col The column number to get the value from.
     * @return The contents of col.
     */
  public String getValue(int paramId, int col)
   {
      Object obj;
      try {
         obj = values[paramId].elementAt(col);
         if (obj == null)
            return null;
         else
            return obj.toString();
      } catch (ArrayIndexOutOfBoundsException e) { return null; }
   }

   /**
     * Method that updates the value of a column.
     * @param paramId The GCS observation type index (row nr).
     * @param val The new value of column.
     * @param col The column number to update.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String setValue(int paramId, int val, int col)
   {
      Vector vec = null;
      try {
         vec = values[paramId];
      } catch (ArrayIndexOutOfBoundsException e) {
         return "Ogiltig GCS observationstyp";
      }

      try {    // Insert the new value
         vec.setElementAt(new Integer(val), col);
      } catch (ArrayIndexOutOfBoundsException e) {
         // Column doesn't exists, create it
         for (int i = vec.size() ; i < col ; i++)
            vec.add(null);
         vec.add(new Integer(val));
      }

      datachanged = true;
      return null;
   }

   /**
     * Method that informs the row that the time for a column has been updated.
     * @param paramId The GCS observation type index (row nr).
     * @param newTime The new time.
     * @param oldTime The old time.
     * @param col The old column number.
     * @param end The new column number.
     * @return null if all is ok, an error message otherwise.
     */
   public String changeTime(int paramId, java.util.Date newTime,
        java.util.Date oldTime, int col, int end)
   {
      Boolean b = (Boolean)inDB.elementAt(col);
 
      // If the value is in the DB update the DB.
      if (b != null && b.booleanValue())
      {
         int i;
         UpdateEntry newEntry = new UpdateEntry(newTime, oldTime);
         // Look for equal previous updates
         for (i=0 ; i < changeTimeVec.size() ; i++)
         {
            if (newEntry.equals((UpdateEntry)changeTimeVec.elementAt(i)))
            {
               newEntry = (UpdateEntry)changeTimeVec.elementAt(i);
               newEntry.num++;
               // All three updates remove entry from the vector
               if (newEntry.num == 3)
               {
                  changeTimeVec.remove(newEntry);
                  i = -1;
               }
               break;
            }
         }
         // if it doesn't exist
         if (i == changeTimeVec.size())
         {
            changeTimeVec.add(newEntry);
            try {
               PreparedStatement ps = dbcon.prepareStatement(
                     "UPDATE EPR.GCS SET OBSERVATION_TIME = ? " +
                     " WHERE RECORD_ID = ? AND OBSERVATION_TIME = ?");
               ps.setTime(1, new java.sql.Time(newTime.getTime()));
               ps.setInt(2, recordId);
               ps.setTime(3, new java.sql.Time(oldTime.getTime()));
               ps.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "GCSModel/changeTime",
                        "Prepare update of patient parameters"));
            }
         }
      }
      // The column has changed place in the table
      if (col != end)
      {
         Object obj = values[paramId].elementAt(col);
         Object inDBobj = inDB.elementAt(col);
         // Shift columns
         for ( ; col < end ; col++)
         {
            values[paramId].setElementAt(values[paramId].elementAt(col+1), col);
            inDB.setElementAt(inDB.elementAt(col+1), col);
         }
         for ( ; col > end ; col--)
         {
            values[paramId].setElementAt(values[paramId].elementAt(col-1), col);
            inDB.setElementAt(inDB.elementAt(col-1), col);
         }   
         values[paramId].setElementAt(obj, end);
         inDB.setElementAt(inDBobj, end);
      }
 
      return null;
   }

   /**
     * Method that save the contents of the row.
     * @param paramId The GCS observation type index (row nr).
     * @param times Times for the columns.
     * @return null if ok, an error message of save failed.
    */
   public String saveRow(int paramId, Vector times)
   {
      if (!datachanged)
         return null;

      String msg = null;
      Boolean b;
      PreparedStatement psIns = null;
      PreparedStatement psUpd = null;
      try {
         psIns = dbcon.prepareStatement(
               "INSERT INTO EPR.GCS (OBSERVATION_TIME, RECORD_ID, EYES, " +
               "MOTOR_ACTIVITY, VERBAL) " +
               "VALUES (?, ?, ?, ?, ?)");
         psUpd = dbcon.prepareStatement(
               "UPDATE EPR.GCS SET EYES = ?, MOTOR_ACTIVITY = ?, " +
               " VERBAL = ? WHERE RECORD_ID = ? AND OBSERVATION_TIME = ?");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "GCSModel/saveRow",
                  "Prepare Insert/Update of patient parameters"));
         return "Kunde inte spara observationerna i databasen";
      }
      for (int i=0 ; i < values[paramId].size() ; i++)
      {
         // Don't save empty values
         if (values[0].elementAt(i) == null ||
               values[1].elementAt(i) == null ||
               values[2].elementAt(i) == null)
         {
            msg = "Inkompletta GCS observationer sparas inte i databasen";
            continue;
         }

         try {
            b =  (Boolean)inDB.elementAt(i);
         } catch (ArrayIndexOutOfBoundsException e) {
            lg.addLog(new Log(e.getMessage(),
                     "GCSModel/saveRow",
                     "Element in db check"));
            b = null;
         }
         // Already in the db
         if (b != null && b.booleanValue())
         {
            try {
               psUpd.setInt(1, ((Integer)values[0].elementAt(i)).intValue());
               psUpd.setInt(2, ((Integer)values[1].elementAt(i)).intValue());
               psUpd.setInt(3, ((Integer)values[2].elementAt(i)).intValue());
               psUpd.setInt(4, recordId);
               psUpd.setTime(5, new Time(((java.util.Date)times.
                           elementAt(i)).getTime()));
               psUpd.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "GCSModel/saveRow",
                        "Update patient parameters"));
            }
         }
         // Not in db
         else
         {
            try {
               psIns.setTime(1, new Time(((java.util.Date)times.
                           elementAt(i)).getTime()));
               psIns.setInt(2, recordId);
               psIns.setInt(3, ((Integer)values[0].elementAt(i)).intValue());
               psIns.setInt(4, ((Integer)values[1].elementAt(i)).intValue());
               psIns.setInt(5, ((Integer)values[2].elementAt(i)).intValue());
               psIns.execute();
               try {
                  inDB.setElementAt(TRUE, i);
               } catch (ArrayIndexOutOfBoundsException e) {
                  for (int j = values[0].size() ; j < i ; j++)
                     inDB.add(FALSE);
                  inDB.add(TRUE);
               }
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "GCSModel/saveRow",
                        "Insert patient parameters"));
            }
         }
      }

      datachanged = false;
      return msg;
   }

   /**
     * Method that checks if there are any values in the rows.
     * @return True id there are any values, false if the row is empty.
     */
   public boolean hasValues()
   {
      for (int i=0 ; i < values[0].size() ; i++)
         if (values[0].elementAt(i) != null ||
             values[1].elementAt(i) != null ||
             values[2].elementAt(i) != null)
            return true;
      return false;
   }

}
