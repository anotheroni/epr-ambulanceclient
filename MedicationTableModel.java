import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import java.text.*;
import java.util.*;
import java.sql.*;

/**
 * Class that formats the medication table in the medication pane
 *
 * @version 20030904
 * @author Oskar Nilsson
 */
public class MedicationTableModel extends AbstractTableModel
{
   private PatientRecord pr;
   private LogHandler lg;
   private DB2Connect dbcon;

   private DecimalFormat df;

   private String[] columnNames = {"Tid", "Medicin", "Mängd", "Sign"};
   private boolean[] editable = {true, false, false, true};
   private Object[][] data;
   private int[] rowId;

   /** Creates a table model object, and initializes it
     * @param pr Reference to the Patient Record.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param isEditable True if the table should be editable.
     */
   public MedicationTableModel(PatientRecord pr, DB2Connect dbcon,
         LogHandler lg, boolean isEditable)
   {
      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;

      if (!isEditable)
      {
         editable[0] = false;
         editable[3] = false;
      }

      DecimalFormatSymbols dfs = 
         new DecimalFormatSymbols(new Locale("sv", "sw"));
      dfs.setDecimalSeparator('.');
      df = new DecimalFormat("0.00", dfs);

      readData(); 
   }

    /**
      * Method that fills the data array with data from the database.
      */
    private void readData()
    {
       ResultSet rs = null;
       int numRows = 0;

      try {
         rs = dbcon.dbQuery(
               "SELECT COUNT(*) FROM EPR.GIVEN_MEDICATIONS " +
               "WHERE RECORD_ID = " + pr.getRecordId());
         rs.next();
         numRows = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT GM.GIVEN_MEDICATION_ID, " +
               "M.MEDICINE_NAME, GM.DOZAGE, M.MEDICINE_CONCENTRATION, " +
               "M.MEDICINE_CONC_UNIT, GM.GIVEN_TIME, S.USER_NAME " +
               "FROM EPR.GIVEN_MEDICATIONS AS GM, EPR.MEDICINE AS M, " +
               "EPR.STAFF AS S WHERE GM.RECORD_ID = " + pr.getRecordId() +
               " AND GM.MEDICINE_ID = M.MEDICINE_ID " +
               " AND S.STAFF_ID = GM.GIVEN_BY ORDER BY GIVEN_TIME");
      } catch (java.sql.SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MedicationTableModel/readData",
                  "Read given medication"));
      }

      if (numRows == 0)
         data = null;
      else
         data = new Object[numRows][columnNames.length];

      rowId = new int[numRows];

      for(int i=0 ; i < numRows ; i++)
      {
         try {
            rs.next();
            data[i][0] = rs.getTime(6);   // GIVEN_TIME
            data[i][1] = rs.getString(2); // MEDICINE_NAME
            data[i][2] = df.format(rs.getDouble(4) * // MEDICINE_CONCENTRATION
                rs.getInt(3)) +  // DOZAGE
                " " + rs.getString(5);  // MEDICINE_CONC_UNIT
            data[i][3] = rs.getString(7); // USER_NAME
            rowId[i] = rs.getInt(1);   // GIVEN_MEDICATION_ID
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "MedicationTableModel/readData",
                     "Read result set"));
         }
      }
    }

    /**
      * Method that updates the table by reading data from the database.
      */
    public void updateTable()
    {
       readData();
       fireTableDataChanged();
    }

    /**
      * Method used to get the record id for a row in the table.
      * @param rowNum The row number.
      * @return The id number of the record on row, -1 if the row don't exist.
      */
    public int getRowId(int rowNum)
    {
       try {
         return rowId[rowNum];
       } catch (ArrayIndexOutOfBoundsException e) {
          return -1;
       }
    }

    /**
     * Method to get the number of columns in the table.
     */
    public int getColumnCount()
    {
        return columnNames.length;
    }

    /**
     * Method to get the number of rows in the table.
     */ 
    public int getRowCount()
    {
       if (data == null)
          return 0;
       else
          return data.length;
    }

    /**
     * Method to get a column name.
     */
    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    /**
     * Method to get the value in a cell.
     * @param row The row number.
     * @param col The column number.
     * @return The object in the specified cell.
     */
    public Object getValueAt(int row, int col)
    {
        return data[row][col];
    }

    /**
     * JTable uses this method to determine the default renderer/
     * editor for each cell. If we didn't implement this method,
     * the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c)
    {
        Object obj = null;
        if(data != null)
            obj = getValueAt(0, c);
        if(obj != null)
            return obj.getClass();
        else //Table is empty, use generic class
            return (new Object()).getClass();
    }

    /**
     * Method used to se if a cell is editable
     * @param row the row to check
     * @param col the column to check
     */
    public boolean isCellEditable(int row, int col)
    {
        return editable[col];
    }

    /**
     * Method to change data in a cell
     * @param value the new value
     * @param row the row to change
     * @param col the colom to change
     */
    public void setValueAt(Object value, int row, int col)
    {
       StringBuffer query = null;
       switch (col) {
          case 0:
             query = new StringBuffer(200);
             query.append("UPDATE EPR.GIVEN_MEDICATIONS SET GIVEN_TIME = '");
             query.append(value.toString ());
             query.append("' WHERE RECORD_ID = ");
             query.append(pr.getRecordId());
             query.append(" AND GIVEN_MEDICATION_ID = ");
             query.append(rowId[row]);
             break;
          case 3:
             query = new StringBuffer(100);
             query.append("UPDATE EPR.GIVEN_MEDICATIONS SET GIVEN_BY = ");
             query.append(((ListEntry)value).getNumber());
             query.append(" WHERE GIVEN_MEDICATION_ID = ");
             query.append(rowId[row]);
             break;
       }
       if (query != null)
       {
          try {
             dbcon.dbQueryUpdate (query.toString());
             data[row][col] = value;
          } catch (SQLException e) {
             // Don't log error if the time is incorect, only give an error msg
             if (e.getSQLState().equals("22007"))
                pr.setMessage ("Värdet är felaktigt");
             else // Not a time error, log it
                lg.addLog(new Log(e.getMessage(),
                         "MedicationTableModel/setValueAt",
                         "Update given medications"));
          }
          fireTableCellUpdated(row, col);
       }
    }

} //Class
