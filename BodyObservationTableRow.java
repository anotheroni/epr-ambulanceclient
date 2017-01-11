import java.util.*;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.sql.*;

/**
  * Class implementing the RLS observation table row in the observation table.
  *
  * @version 20030716
  * @author Oskar Nilsson
  */
public class BodyObservationTableRow implements ObservationTableRowInterface
{
   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;

   private Vector values;
   // Vector containing booleans, true if the value is in the db.
   private Vector inDB;
   private Vector timeVec;

   private boolean datachanged = false;
   
   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   private Vector posValues;

   private BigComboBox combo;

   /**
     * Constructor, initialises the local variables.
     * @param recordId The curent record id.
     * @param paramId The id of the parameter in the database.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param times The rows that exists in the table.
     */
   public BodyObservationTableRow(int recordId, DB2Connect dbcon, LogHandler lg,
         Vector times)
   {
      this.dbcon = dbcon;
      this.recordId = recordId;
      this.lg = lg;

      values = new Vector();
      inDB = new Vector();
      timeVec = new Vector();
      posValues = new Vector();

      // Read the position names from the database
      ResultSet rs;
      try {
         rs = dbcon.dbQuery("SELECT POSITION_ID, POSITION_NAME FROM " +
              "EPR.BODY_POSITION WHERE DISABLE = 0 ORDER BY POSITION_ID");
         while (rs.next())
            posValues.add(new ListEntry(rs.getString(2), rs.getInt(1)));
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "BodyObservationTableRow/BodyObservationTableRow",
                  "Read body positions"));
      }

      combo = new BigComboBox(posValues);
      Dimension d = combo.getPreferredSize();
      combo.setPopupWidth(d.width);
 
      readData(times);
   }
   
   /**
     * Method that reads the contents of the row from the database.
     * @param times The times of the columns that already exists.
     */
   private void readData(Vector times)
   {
      ResultSet rs;
      Iterator iterator = times.iterator();
      java.util.Date tableTime;
      boolean rsHasNext;
      try {
         tableTime = (java.util.Date) iterator.next();
      } catch (NoSuchElementException e) { tableTime = null; }
      
      try {
          rs = dbcon.dbQuery("SELECT BPO.OBSERVATION_TIME, BPO.POSITION, " +
               "BP.POSITION_NAME FROM EPR.BODY_POSITION_OBSERVATION AS BPO, " +
               "EPR.BODY_POSITION AS BP WHERE BPO.RECORD_ID = " + recordId +
               " AND BPO.POSITION = BP.POSITION_ID ORDER BY OBSERVATION_TIME");
          rsHasNext = rs.next();
          // Merge time vectors
          while (rsHasNext && tableTime != null)
          {
             // A new column that doesn't exist in the table
             // true if rs.getTime is before tableTime
             if (tableTime.compareTo(rs.getTime(1)) > 0)
             {
                values.add(new ListEntry(rs.getString(3), rs.getInt(2)));
                inDB.add(TRUE);
                timeVec.add(rs.getTime(1));  // OBSERVATION_TIME
                rsHasNext = rs.next();
             }
             // A column that exists in both the db and the table
             else if (tableTime.compareTo(rs.getTime(1)) == 0)
             {
                values.add(new ListEntry(rs.getString(3), rs.getInt(2)));
                inDB.add(TRUE);
                if (iterator.hasNext())
                   tableTime = (java.util.Date)iterator.next();
                else
                   tableTime = null;
                rsHasNext = rs.next();
             }
             // A row that only exists in the table
             else
             {
                values.add(null);
                inDB.add(FALSE);
                if (iterator.hasNext())
                   tableTime = (java.util.Date)iterator.next();
                else
                   tableTime = null;
             }
          }
          // Add columns left from the merge loop
          while (tableTime != null)
          {
             values.add(null);
             inDB.add(FALSE);
             if (iterator.hasNext())
               tableTime = (java.util.Date)iterator.next();
             else
                tableTime = null;
          }
          while (rsHasNext)
          {
             values.add(new ListEntry(rs.getString(3), rs.getInt(2)));
             inDB.add(TRUE);
             timeVec.add(rs.getTime(1));  // OBSERVATION_TIME
             rsHasNext = rs.next();
          }
          rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "BodyObservationTableRow/readData",
                  "Read body positions observations"));
      }
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
     * Method that returns the row header text.
     * @return The name of the row.
     */
   public String getRowName()
   {
      return "Kroppsposition";
   }

   /**
     * Method that adds a new column at the end of the row with value val.
     * @param val The value of the column.
     * @param col The column number to add.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String addColumn(String val, int col)
   {
      int iVal;

      if (val != null)
      {
         setValue(val, col);
      }
      else
      {
         try {
            values.add(col, null);
            inDB.add(col, FALSE);
         } catch (ArrayIndexOutOfBoundsException e) {
            for (int i = values.size() ; i <= col ; i++)
            {
               values.add(null);
               inDB.add(FALSE);
            }
         }
      }

      return null;
   }
   
   /**
     * Method that returns the value in the specified column.
     * @param col The column number to get the value from.
     * @return The contents of col.
     */
   public String getValue(int col)
   {
      ListEntry obj;
      try {
         obj = (ListEntry)values.elementAt(col);
         if (obj != null)
            return obj.toString();
         else
            return null;
      } catch (ArrayIndexOutOfBoundsException e) {
         // Asking for a column that doesn't exist
         return null;
      }
   }

   /**
     * Method that updates the value of column.
     * @param val The new value of column.
     * @param col The column number to update.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String setValue(String val, int col)
   {
      int i;
      String numStr = null;
      ListEntry obj = null;
      if (val != null)
      {
         for (i=0 ; i < posValues.size() ; i++)
         {
            obj = (ListEntry)posValues.elementAt(i);
            if (val.equals(obj.toString()))
               break;
         }
         if (i == posValues.size())
            return "Kroppspositionen finns ej definierad";

         try {    // Insert the new value
            values.setElementAt(obj, col);
         } catch (ArrayIndexOutOfBoundsException e) {
            // Column doesn't exists, create it
            for (i = values.size() ; i < col ; i++)
               values.add(null);
            values.add(obj);
         }
      }
      datachanged = true;
      return null;
   }

   /**
     * Method that informs the row that the time for a column has been updated.
     * @param newTime The new time.
     * @param oldTime The old time.
     * @param col The old column number.
     * @param end The new column number.
     * @return null if all is ok, an error message otherwise.
     */
   public String changeTime(java.util.Date newTime, java.util.Date oldTime,
         int col, int end)
   {
      Boolean b = (Boolean)inDB.elementAt(col);
      
      // If the value is in the DB update the DB.
      if (b != null && b.booleanValue())
      {
         try {
            PreparedStatement ps = dbcon.prepareStatement(
                  "UPDATE EPR.BODY_POSITION_OBSERVATION " +
                  " SET OBSERVATION_TIME = ? WHERE RECORD_ID = ? " +
                  " AND OBSERVATION_TIME = ?");
            ps.setTime(1, new java.sql.Time(newTime.getTime()));
            ps.setInt(2, recordId);
            ps.setTime(3, new java.sql.Time(oldTime.getTime()));
            ps.execute();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "BodyObservationTableRow/changeTime",
                     "Prepare update of body positions"));
         }
      }
      // The column has changed place in the table
      if (col != end)
      {
         Object val = values.elementAt(col);
         Object inDBval = inDB.elementAt(col);
         // Shift columns
         for ( ; col < end ; col++)
         {
            values.setElementAt(values.elementAt(col+1), col);
            inDB.setElementAt(inDB.elementAt(col+1), col);
         }
         for ( ; col > end ; col--)
         {
            values.setElementAt(values.elementAt(col-1), col);
            inDB.setElementAt(inDB.elementAt(col-1), col);
         }
         values.setElementAt(val, end);
         inDB.setElementAt(inDBval, end);
      }
      return null;
   }

   /**
     * Method that save the contents of the row.
     * @param times Times for the columns.
     * @return null if ok, an error message of save failed.
     */ 
   public String saveRow(Vector times)
   {
      if (!datachanged)
         return null;

      Boolean b;
      PreparedStatement psIns = null;
      PreparedStatement psUpd = null;
      try {
         psIns = dbcon.prepareStatement(
            "INSERT INTO EPR.BODY_POSITION_OBSERVATION " +
            "(OBSERVATION_TIME, RECORD_ID, POSITION) VALUES (?, ?, ?)");
         psUpd = dbcon.prepareStatement(
            "UPDATE EPR.BODY_POSITION_OBSERVATION " +
            "SET POSITION = ? WHERE RECORD_ID = ? AND OBSERVATION_TIME = ? "); 
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "BodyObservationTableRow/saveRow",
                  "Prepare Insert/Update of body positions"));
         return "Kunde inte spara observationerna i databasen";
      }
      for (int i=0 ; i < values.size() ; i++)
      {
         if (values.elementAt(i) == null)    // Don't save empty values
            continue;

         b =  (Boolean)inDB.elementAt(i);
         // Already in the db
         if (b != null && b.booleanValue())
         {
            try {
               psUpd.setInt(1, ((ListEntry)values.elementAt(i)).getNumber());
               psUpd.setInt(2, recordId);
               psUpd.setTime(3, new Time(((java.util.Date)times.elementAt(i)).
                        getTime()));
               psUpd.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "BodyObservationTableRow/saveRow",
                        "Update body positions"));
            }
         }
         // Not in db
         else
         {
            try {
               psIns.setTime(1, new Time(((java.util.Date)times.elementAt(i)).
                     getTime()));
               psIns.setInt(2, recordId);
               psIns.setInt(3, ((ListEntry)values.elementAt(i)).getNumber());
               psIns.execute();
               try {
                  inDB.setElementAt(TRUE, i);
               } catch (ArrayIndexOutOfBoundsException e) {
                  for (int j = values.size() ; j < i ; j++)
                     inDB.add(FALSE);
                inDB.add(TRUE);
               }
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "BodyObservationTableRow/saveRow",
                        "Insert body positions"));
            }
         }
      }
      datachanged = false;
      return null;
   }

   /**
     * Method that returns the editor that should be used to edit the row.
     * @param col The column to get the editor for.
     * @return The TabelCellEditor that the row uses.
     */
   public TableCellEditor getCellEditor(int col)
   {
      ListEntry val = null;
      int iVal;
      try {
         val = (ListEntry)values.elementAt(col);
      } catch (ArrayIndexOutOfBoundsException e) {
         return new DefaultCellEditor(combo);
      }
      try {
        combo.setSelectedItem(val);
      } catch (IllegalArgumentException e) { }
 
      return new DefaultCellEditor(combo);
   }

   /**
     * Method that checks if there are any values in the row.
     * @return True id there are any values, false if the row is empty.
     */
   public boolean hasValues()
   {
      for (int i=0 ; i < values.size() ; i++)
         if (values.elementAt(i) != null)
            return true;
      return false;
   }

   /**
    * Method called by the cell editor focus listener when the editor
    * looses focus. Saves the current value. NOT USED.
    * @param val The text in the editor.
    * @return null if all is ok, else an error message.
    */
   public String saveCurrentCell (String val)
   {
      return null;
   }

}
