import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.*;

/**
  * Class representing a standard row in the observations table that
  * contains integer values.
  * 
  * @version 20030716
  * @author Oskar Nilsson
  */
public class StdObservationTableRow implements ObservationTableRowInterface,
   FocusEventReceiver
{
 
   protected DB2Connect dbcon;
   protected LogHandler lg;
   protected ParameterTableModel paramTModel;

   protected int recordId;
   protected int paramId;
   protected int minVal;
   protected int maxVal;
   protected String rowName;

   protected Vector values;
   // Vector containing booleans, true if the value is in the db.
   protected Vector inDB;
   protected Vector timeVec;

   protected boolean datachanged = false;
   protected int lastCell = 0;
   protected int nextActiveCell = 0;

   protected JTextField cellEditorFld;

   protected Boolean TRUE = new Boolean(true);
   protected Boolean FALSE = new Boolean(false);
   
   /**
     * Constructor, initialises the local variables.
     * @param recordId The curent record id.
     * @param paramId The id of the parameter in the database.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param rowName The name of the row.
     * @param minVal The minimum value allowed.
     * @param maxVal The maximum value allowed.
     * @param times The rows that exists in the table.
     * @param paramTModel A reference to the table model that the row is a
     * part of.
     */
   public StdObservationTableRow(int recordId, int paramId,
        DB2Connect dbcon, LogHandler lg, String rowName, int minVal, int maxVal,
        Vector times, ParameterTableModel paramTModel)
   {
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.paramId = paramId;
      this.rowName = rowName;
      this.minVal = minVal;
      this.maxVal = maxVal;
      this.paramTModel = paramTModel;

      values = new Vector();
      inDB = new Vector();
      timeVec = new Vector();

      cellEditorFld = new JTextField();
      ComponentFocusListener f_listener =
         new ComponentFocusListener (this, cellEditorFld);
      cellEditorFld.addFocusListener (f_listener);
      ((AbstractDocument)cellEditorFld.getDocument()).
         setDocumentFilter(new LimitedNumberFilter(3));

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
                  "StdObservationTableRow/readData",
                  "Read patient parameters " + paramId));
      }
   }

   /**
     * Method used by tha table to check if the row has any columns that 
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
      return rowName;
   }

   /**
     * Method that adds a new column at position row in the row with value val.
     * @param val The value of the row.
     * @param col The column number to insert val at.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
    */
   public String addColumn(String val, int col)
   {
      if (val != null)
      {
         return setValue(val, col);
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
     * Method that returns the value in column.
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
     * Method that updates the value of a column.
     * @param val The new value of col.
     * @param col The column number to update.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String setValue(String val, int col)
   {
      int iVal;
      try { // Check if the string contains only numbers
         iVal = Integer.parseInt(val, 10);
      } catch(NumberFormatException e) {
         return "Värdet måste vara ett tal";
      }

      if (iVal > maxVal || iVal < minVal) // Check that is a valid number
         return "Värdet måste vara mellan " + minVal + " och " + maxVal;

      try {    // Insert the new value
         values.setElementAt(val, col);
      } catch (ArrayIndexOutOfBoundsException e) {
         // Column doesn't exists, create it
         for (int i = values.size() ; i < col ; i++)
         {
            values.add(null);
            inDB.add(FALSE);
         }
         values.add(val);
         inDB.add(FALSE);
      }
      datachanged = true;

      return null;
   }

   /**
     * Method that informs the row that the time for a column has been updated.
     * @param newTime The new time.
     * @param oldTime The old time.
     * @param col The new column number.
     * @param end The old column number.
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
                     "StdObservationTableRow/changeTime",
                     "Update observation time " + paramId));
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
                  "StdObservationTableRow/saveRow",
                  "Preprare Insert/Update patient parameters " + paramId));
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
                        "StdObservationTableRow/saveRow",
                        "Update patient parameters " + paramId));
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
                        "StdObservationTableRow/saveRow",
                        "Insert patient parameters " + paramId));
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
      nextActiveCell = col;
      return new DefaultCellEditor(cellEditorFld);
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
    * looses focus. Saves the current value.
    * @param val The text in the editor.
    * @return null if all is ok, else an error message.
    */
   public String saveCurrentCell (String val)
   {
      datachanged = true;
      String res = setValue(val, lastCell);
      // Needed to remove the editor text field
      paramTModel.fireTableStructureChanged ();
      return res;
   }

   /**
     * Method that sets the current active cell.
     */
   public void setCurrentCell ()
   {
      lastCell = nextActiveCell;
   }
}
