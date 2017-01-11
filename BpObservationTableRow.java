import java.util.*;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.sql.*;

/**
  * Class implementing the Blood Presure observation table row in the
  * observation table.
  *
  * @version 20030725
  * @author Oskar Nilsson
  */
public class BpObservationTableRow implements ObservationTableRowInterface,
   FocusEventReceiver
{
   private DB2Connect dbcon;
   private LogHandler lg;
   private ParameterTableModel paramTModel;
   private int recordId;
   private int paramId;

   private JTextField cellEditorFld;

   private Vector diastol;
   private Vector systol;
   // Vector containing booleans, true if the value is in the db.
   private Vector inDB;
   private Vector timeVec;

   private boolean datachanged = false;
   private int lastCell = 0;
   private int nextActiveCell = 0;
   
   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   /**
     * Constructor, initialises the local variables.
     * @param recordId The curent record id.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param times The rows that exists in the table.
     * @param paramTModel A reference to the table model that the row is a
     * part of.
     */
   public BpObservationTableRow(int recordId, DB2Connect dbcon, LogHandler lg,
         Vector times, ParameterTableModel paramTModel)
   {
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.paramTModel = paramTModel;

      systol = new Vector();
      diastol = new Vector();
      inDB = new Vector();
      timeVec = new Vector();

      cellEditorFld = new JTextField();
      ComponentFocusListener f_listener =
         new ComponentFocusListener (this, cellEditorFld);
      cellEditorFld.addFocusListener (f_listener);

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
         rs = dbcon.dbQuery("SELECT OBSERVATION_TIME, DIASTOL, SYSTOL FROM " +
               "EPR.BLOOD_PRESSURE WHERE RECORD_ID = " + recordId +
               " ORDER BY OBSERVATION_TIME");
         rsHasNext = rs.next();
         // Merge time vectors
         while (rsHasNext && tableTime != null)
         {
            // A new column that doesn't exist in the table
             // true if rs.getTime is before tableTime
             if (tableTime.compareTo(rs.getTime(1)) > 0)
             {
                diastol.add(new Integer(rs.getInt(2))); // DIASTOL
                systol.add(new Integer(rs.getInt(3)));  // SYSTOL
                inDB.add(TRUE);
                timeVec.add(rs.getTime(1));  // OBSERVATION_TIME
                rsHasNext = rs.next();
             }
             // A column that exists in both the db and the table
             else if (tableTime.compareTo(rs.getTime(1)) == 0)
             {
                diastol.add(new Integer(rs.getInt(2))); // DIASTOL
                systol.add(new Integer(rs.getInt(3)));  // SYSTOL
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
                diastol.add(null);
                systol.add(null);
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
             diastol.add(null);
             systol.add(null);
             inDB.add(FALSE);
             if (iterator.hasNext())
               tableTime = (java.util.Date)iterator.next();
             else
                tableTime = null;
          }
          while (rsHasNext)
          {
             diastol.add(new Integer(rs.getInt(2))); // DIASTOL
             systol.add(new Integer(rs.getInt(3)));  // SYSTOL
             inDB.add(TRUE);
             timeVec.add(rs.getTime(1));  // OBSERVATION_TIME
             rsHasNext = rs.next();
          }
          rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "BpObservationTableRow/readData",
                  "Read bp"));
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
      return "Blodtryck";
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
      if (val != null)
      {
         return setValue(val, col);
      }
      else
      {
         try {
            systol.add(col, null);
            diastol.add(col, null);
            inDB.add(col, FALSE);
         } catch (ArrayIndexOutOfBoundsException e) {
            for (int i = systol.size() ; i <= col ; i++)
            {
               systol.add(null);
               diastol.add(null);
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
         if (systol.elementAt(col) == null && diastol.elementAt(col) == null)
            return null;
         else if (systol.elementAt(col) == null)
            return "/" + diastol.elementAt(col);
         else if (diastol.elementAt(col) == null)
            return systol.elementAt(col) + "/";
         else
            return systol.elementAt(col) + "/" + diastol.elementAt(col);
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
      Integer iSystol = null;
      String systolStr = null;
      Integer iDiastol = null;
      String diastolStr = null;
      int idx;

      if (val != null)
      {
         if((idx = val.indexOf("/")) == -1)
            return "Blodtrycket måste skrivas på formen systol/diastol";
         
         try {
            if (idx > 0)
               systolStr = val.substring(0, idx);
            if (idx + 1 < val.length())
               diastolStr = val.substring(idx+1, val.length());
         } catch (IndexOutOfBoundsException e) {
            return "Blodtrycket måste skrivas på formen systol/diastol";
         }
         try { // Check if the string contains only numbers
            if (systolStr != null)
               iSystol = Integer.valueOf(systolStr, 10);
            if (diastolStr != null)
               iDiastol = Integer.valueOf(diastolStr, 10);
         } catch(NumberFormatException e) {
            return "Blodtrycket måste skrivas på formen systol/diastol";
         }
         try {
            systol.setElementAt(iSystol, col);
            diastol.setElementAt(iDiastol, col);
         } catch (ArrayIndexOutOfBoundsException e) {
            // Column doesn't exists, create it
            for (int i = systol.size() ; i < col ; i++)
            {
               systol.add(null);
               diastol.add(null);
               inDB.add(FALSE);
            }            
            systol.add(iSystol);
            diastol.add(iDiastol);
            inDB.add(FALSE);
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
                  "UPDATE EPR.BLOOD_PRESSURE SET OBSERVATION_TIME = ? " +
                  " WHERE RECORD_ID = ? AND OBSERVATION_TIME = ?");
            ps.setTime(1, new java.sql.Time(newTime.getTime()));
            ps.setInt(2, recordId);
            ps.setTime(3, new java.sql.Time(oldTime.getTime()));
            ps.execute();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "BpObservationTableRow/changeTime",
                     "Prepare update bp"));
         }
      }
      // The column has changed place in the table
      if (col != end)
      {
         Object systolObj = systol.elementAt(col);
         Object diastolObj = diastol.elementAt(col);
         Object inDBobj = inDB.elementAt(col);
         // Shift columns
         for ( ; col < end ; col++)
         {
            systol.setElementAt(systol.elementAt(col+1), col);
            diastol.setElementAt(diastol.elementAt(col+1), col);
            inDB.setElementAt(inDB.elementAt(col+1), col);
         }
         for ( ; col > end ; col--)
         {
            systol.setElementAt(systol.elementAt(col-1), col);
            diastol.setElementAt(diastol.elementAt(col-1), col);
            inDB.setElementAt(inDB.elementAt(col-1), col);
         }
         systol.setElementAt(systolObj, end);
         diastol.setElementAt(diastolObj, end);
         inDB.setElementAt(inDBobj, end);
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
            "INSERT INTO EPR.BLOOD_PRESSURE " +
            "(OBSERVATION_TIME, RECORD_ID, SYSTOL, DIASTOL) " +
            "VALUES (?, ?, ?, ?)");
         psUpd = dbcon.prepareStatement(
            "UPDATE EPR.BLOOD_PRESSURE SET SYSTOL = ?, DIASTOL = ? " +
            " WHERE RECORD_ID = ? AND OBSERVATION_TIME = ?");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "BpObservationTableRow/saveRow",
                  "Prepare Insert/Update of bp"));
         return "Kunde inte spara observationerna i databasen";
      }
      for (int i=0 ; i < systol.size() ; i++)
      {
         // Don't save empty values
         if (systol.elementAt(i) == null && diastol.elementAt(i) == null)
            continue;

         b =  (Boolean)inDB.elementAt(i);
         // Already in the db
         if (b != null && b.booleanValue())
         {
            try {
               if (systol.elementAt(i) != null)
                  psUpd.setInt(1, ((Integer)systol.elementAt(i)).intValue());
               else
                  psUpd.setNull(1, java.sql.Types.INTEGER);
               if (diastol.elementAt(i) != null)
                  psUpd.setInt(2, ((Integer)diastol.elementAt(i)).intValue());
               else
                  psUpd.setNull(2, java.sql.Types.INTEGER);
               psUpd.setInt(3, recordId);
               psUpd.setTime(4, new Time(((java.util.Date)times.elementAt(i)).
                        getTime()));
               psUpd.execute();
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "BpObservationTableRow/saveRow",
                        "Update bp"));
            }
         }
         // Not in db
         else
         {
            try {
               psIns.setTime(1, new Time(((java.util.Date)times.elementAt(i)).
                     getTime()));
               psIns.setInt(2, recordId);
               if (systol.elementAt(i) != null)
                  psIns.setInt(3, ((Integer)systol.elementAt(i)).intValue());
               else
                  psIns.setNull(3, java.sql.Types.INTEGER);
               if (diastol.elementAt(i) != null)
                  psIns.setInt(4, ((Integer)diastol.elementAt(i)).intValue());
               else
                  psIns.setNull(4, java.sql.Types.INTEGER);
               psIns.execute();
               try {
                  inDB.setElementAt(TRUE, i);
               } catch (ArrayIndexOutOfBoundsException e) {
                  for (int j = systol.size() ; j < i ; j++)
                     inDB.add(FALSE);
                  inDB.add(TRUE);
               }
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "BpObservationTableRow/saveRow",
                        "Insert bp"));
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
      for (int i=0 ; i < systol.size() ; i++)
         if (systol.elementAt(i) != null || diastol.elementAt(i) != null)
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
