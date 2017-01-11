import java.util.*;
import java.sql.*;

/**
  * Class implementing a model for the Eye rows in the observation table.
  *
  * @version 20030313
  * @author Oskar Nilsson
  */
public class EyeModel
{
   private int recordId;
   private DB2Connect dbcon;
   private LogHandler lg;

   private EyeObservationTableRow rows[];
   private Vector values[];
   private Vector inDB;
   private Vector timeVec;

   /// Vector that keeps track of calls to changeTime
   private Vector changeTimeVec;

   private boolean datachanged = false;

   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   private final String[] cboxValues = {
      "Liten",
      "Normal",
      "Stor"
   };

   /**
     * Construcor, initializes the local variables and creates the two
     * EyeObservationTableRow objects.
     * @param recordId The current record id.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param times The rows that exists in the table.
     */
   public EyeModel(int recordId, DB2Connect dbcon, LogHandler lg, Vector times)
   {
      this.recordId = recordId;
      this.dbcon = dbcon;
      this.lg = lg;

      inDB = new Vector();
      changeTimeVec = new Vector();
      timeVec = new Vector();

      rows = new EyeObservationTableRow[2];

      rows[0] = new EyeObservationTableRow(this, 0, cboxValues,
           "Vänster puppil");
      rows[1] = new EyeObservationTableRow(this, 1, cboxValues,
            "Höger puppil");

      values = new Vector[2];
      values[0] = new Vector();
      values[1] = new Vector();

      readData(times);
   }

   /**
     * Method that reads eye data from the database.
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
               "SELECT OBSERVATION_TIME, LEFT, RIGHT " +
               "FROM EPR.EYE WHERE RECORD_ID = " + recordId +
               " ORDER BY OBSERVATION_TIME");
         rsHasNext = rs.next();
         // Merge time vectors
         while (rsHasNext && tableTime != null)
         {
            // A new column that doesn't exist in the table
            // true if rs.getTime is before tableTime
            if (tableTime.compareTo(rs.getTime(1)) > 0)
            {
               values[0].add(rs.getString(2)); // LEFT 
               values[1].add(rs.getString(3)); // RIGHT
               inDB.add(TRUE);
               timeVec.add(rs.getTime(1));   // OBSERVATION_TIME
               rsHasNext = rs.next(); 
            }
            // A column that exists in both db and the table
            else if (tableTime.compareTo(rs.getTime(1)) == 0)
            {
               values[0].add(rs.getString(2)); // LEFT 
               values[1].add(rs.getString(3)); // RIGHT
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
            inDB.add(FALSE);
            if (iter.hasNext())
               tableTime = (java.util.Date)iter.next();
            else
               tableTime = null;
         }
         while (rsHasNext)
         {
            values[0].add(rs.getString(2)); // LEFT 
            values[1].add(rs.getString(3)); // RIGHT
            inDB.add(TRUE);
            timeVec.add(rs.getTime(1));   // OBSERVATION_TIME
            rsHasNext = rs.next(); 
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "EyeModel/readData",
                  "Read patient parameters"));
      }
   }

   /**
     * Method that returns the specified eye row model.
     * @param row Index of the row to get.
     * @return The requested row model.
     */
   public EyeObservationTableRow getRowModel(int row)
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
     * @param paramId The eye observation type index (row nr).
     * @param val The value of the column. (not used)
     * @param col The column number to add.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String addColumn(int paramId, String val, int col)
   {
      // Bad, only request from row 0 are processed.
      if (paramId != 0)
         return null;

      try {
         values[0].add(col, null);
         values[1].add(col, null);
         inDB.add(FALSE);
      } catch (ArrayIndexOutOfBoundsException e) {
         for (int i = values[0].size() ; i <= col ; i++)
         {
            values[0].add(null);
            values[1].add(null);
            inDB.add(FALSE);
         }
      }
      return null;
   }

    /**
     * Method that returns the value in the specified column.
     * @param paramId The eye observation type index (row nr).
     * @param col The column number to get the value from.
     * @return The contents of col.
     */
  public String getValue(int paramId, int col)
   {
      try {
         return (String)values[paramId].elementAt(col);
      } catch (ArrayIndexOutOfBoundsException e) { return null; }
   }

   /**
     * Method that updates the value of a column.
     * @param paramId The eye observation type index (row nr).
     * @param val The new value of column.
     * @param col The column number to update.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String setValue(int paramId, String val, int col)
   {
      Vector vec = null;
      try {
         vec = values[paramId];
      } catch (ArrayIndexOutOfBoundsException e) {
         return "Ogiltig puppil observationstyp";
      }

      try {    // Insert the new value
         vec.setElementAt(val, col);
      } catch (ArrayIndexOutOfBoundsException e) {
         // Column doesn't exists, create it
         for (int i = vec.size() ; i < col ; i++)
            vec.add(null);
         vec.add(val);
      }

      datachanged = true;
      return null;
   }

   /**
     * Method that informs the row that the time for a column has been updated.
     * @param paramId The eye observation type index (row nr).
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
               // Both updates remove entry from the vector
               if (newEntry.num == 2)
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
                     "UPDATE EPR.EYE SET OBSERVATION_TIME = ? " +
                     " WHERE RECORD_ID = ? AND OBSERVATION_TIME = ?");
               ps.setTime(1, new java.sql.Time(newTime.getTime()));
               ps.setInt(2, recordId);
               ps.setTime(3, new java.sql.Time(oldTime.getTime()));
               ps.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "EyeModel/changeTime",
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
    * @param paramId The eye observation type index (row nr).
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
               "INSERT INTO EPR.EYE (OBSERVATION_TIME, RECORD_ID, LEFT, " +
               "RIGHT) VALUES (?, ?, ?, ?)");
         psUpd = dbcon.prepareStatement(
               "UPDATE EPR.EYE SET LEFT = ?, RIGHT = ? " +
               "WHERE RECORD_ID = ? AND OBSERVATION_TIME = ?");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "EyeModel/saveRow",
                  "Prepare Insert/Update of patient parameters"));
         return "Kunde inte spara observationerna i databasen";
      }
      for (int i=0 ; i < values[paramId].size() ; i++)
      {
         // Don't save empty values
         if (values[0].elementAt(i) == null ||
               values[1].elementAt(i) == null)
         {
            msg = "Inkompletta puppil observationer sparas inte i databasen";
            continue;
         }

         b =  (Boolean)inDB.elementAt(i);
         // Already in the db
         if (b != null && b.booleanValue())
         {
            try {
               psUpd.setString(1, (String)values[0].elementAt(i));
               psUpd.setString(2, (String)values[1].elementAt(i));
               psUpd.setInt(3, recordId);
               psUpd.setTime(4, new Time(((java.util.Date)times.
                           elementAt(i)).getTime()));
               psUpd.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "EyeModel/saveRow",
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
               psIns.setString(3, (String)values[0].elementAt(i));
               psIns.setString(4, (String)values[1].elementAt(i));
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
                        "EyeModel/saveRow",
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
         if (values[0].elementAt(i) != null || values[1].elementAt(i) != null)
            return true;
      return false;
   }

}
