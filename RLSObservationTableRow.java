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
public class RLSObservationTableRow implements ObservationTableRowInterface
{
   private DB2Connect dbcon;
   private LogHandler lg;

   private int recordId;
   private int paramId;

   private Vector values;
   // Vector containing booleans, true if the value is in the db.
   private Vector inDB;
   private Vector timeVec;

   private boolean datachanged = false;
   
   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   private String[] rlsValues = {
      "1 - Vaken orienterad",
      "2 - Vaken slö eller oklar",
      "3 - Vaken mycket slö eller oklar, avvärjer smärta",
      "4 - Medvetslös, lokaliserar smärta, men avvärjer ej smärta",
      "5 - Medvetslös, undandrar sig smörta",
      "6 - Medvetslös, böjrörelser vid smärta",
      "7 - Medvetslös, sträckrörelser vid smärta",
      "8 - Medvetslös, ingen smärtreaktion"
   };

   private BigComboBox combo;

   /**
     * Constructor, initialises the local variables.
     * @param recordId The curent record id.
     * @param paramId The id of the parameter in the database.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param times The rows that exists in the table.
     */
   public RLSObservationTableRow(int recordId, int paramId,
        DB2Connect dbcon, LogHandler lg, Vector times)
   {
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.paramId = paramId;

      values = new Vector();
      inDB = new Vector();
      timeVec = new Vector();

      combo = new BigComboBox(rlsValues);
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
          rs = dbcon.dbQuery("SELECT OBSERVATION_TIME, VALUE FROM " +
               "EPR.OBSERVATIONS_OF_PATIENT_PARAMETERS WHERE RECORD_ID = " +
               recordId + " AND OBSERVATION_PARAMETER_ID = " + paramId +
               " ORDER BY OBSERVATION_TIME");
          rsHasNext = rs.next();
          // Merge time vectors
          while (rsHasNext && tableTime != null)
          {
             // A new column that doesn't exist in the table
             // true if rs.getTime is before tableTime
             if (tableTime.compareTo(rs.getTime(1)) > 0)
             {
                values.add(rs.getString(2)); // VALUE
                inDB.add(TRUE);
                timeVec.add(rs.getTime(1));  // OBSERVATION_TIME
                rsHasNext = rs.next();
             }
             // A column that exists in both the db and the table
             else if (tableTime.compareTo(rs.getTime(1)) == 0)
             {
                values.add(rs.getString(2)); // VALUE
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
             values.add(rs.getString(2)); // VALUE
             inDB.add(TRUE);
             timeVec.add(rs.getTime(1));  // OBSERVATION_TIME
             rsHasNext = rs.next();
          }
          rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RLSObservationTableRow/readData",
                  "Read patient parameters"));
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
      return "RLS";
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
         return setValue(val, col);
      }
      else
      {
         try {
            values.add(col, null);
            inDB.add(FALSE);
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
      try {
         return (String)values.elementAt(col);
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
      int iVal;
      String numStr = null;
      if (val != null)
         numStr = val.substring(0, 1);
      
      try { // Check if the string contains only numbers
         iVal = Integer.parseInt(numStr, 10);
      } catch(NumberFormatException e) {
         return "RLS måste vara ett nummer";
      }

      if (iVal > 8 || iVal < 1) // Check that is a valid number
         return "RLS ska vara mellan 1 och 8";

      try {    // Insert the new value
         values.setElementAt(numStr, col);
      } catch (ArrayIndexOutOfBoundsException e) {
         // Column doesn't exists, create it
         for (int i = values.size() ; i < col ; i++)
            values.add(null);
         values.add(numStr);
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
                  "UPDATE EPR.OBSERVATIONS_OF_PATIENT_PARAMETERS " +
                  " SET OBSERVATION_TIME = ? WHERE RECORD_ID = ? " +
                  " AND OBSERVATION_PARAMETER_ID = ? AND OBSERVATION_TIME = ?");
            ps.setTime(1, new java.sql.Time(newTime.getTime()));
            ps.setInt(2, recordId);
            ps.setInt(3, paramId);
            ps.setTime(4, new java.sql.Time(oldTime.getTime()));
            ps.execute();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "RLSObservationTableRow/changeTime",
                     "Prepare update of patient parameters"));
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
            "INSERT INTO EPR.OBSERVATIONS_OF_PATIENT_PARAMETERS " +
            "(OBSERVATION_TIME, RECORD_ID, OBSERVATION_PARAMETER_ID, " +
            "VALUE) VALUES (?, ?, ?, ?)");
         psUpd = dbcon.prepareStatement(
            "UPDATE EPR.OBSERVATIONS_OF_PATIENT_PARAMETERS " +
            "SET VALUE = ? WHERE RECORD_ID = ? AND " +
            "OBSERVATION_PARAMETER_ID = ? AND OBSERVATION_TIME = ? "); 
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RLSObservationTableRow/saveRow",
                  "Prepare Insert/Update of patient parameters"));
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
               psUpd.setString(1, (String)values.elementAt(i));
               psUpd.setInt(2, recordId);
               psUpd.setInt(3, paramId);
               psUpd.setTime(4, new Time(((java.util.Date)times.elementAt(i)).
                        getTime()));
               psUpd.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "RLSObservationTableRow/saveRow",
                        "Update patient parameters"));
            }
         }
         // Not in db
         else
         {
            try {
               psIns.setTime(1, new Time(((java.util.Date)times.elementAt(i)).
                     getTime()));
               psIns.setInt(2, recordId);
               psIns.setInt(3, paramId);
               psIns.setString(4, (String)values.elementAt(i));
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
                        "RLSObservationTableRow/saveRow",
                        "Insert patient parameters"));
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
      String val;
      int iVal;
      try {
         val = (String)values.elementAt(col);
      } catch (ArrayIndexOutOfBoundsException e) {
         return new DefaultCellEditor(combo);
      }
      try { // Check if the string contains only numbers
         iVal = Integer.parseInt(val, 10);
      } catch (NumberFormatException e) {
         return new DefaultCellEditor(combo);
      }
      try {
        combo.setSelectedIndex(iVal-1);
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
    * looses focus. Saves the current value. NOT NULL.
    * @param val The text in the editor.
    * @return null if all is ok, else an error message.
    */
   public String saveCurrentCell (String val)
   {
      return null;
   }

}
