import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import java.text.*;
import java.util.*;
import java.sql.*;

/**
 * Class that formats the medication table in the verify signature dialog. 
 *
 * @version 20030408
 * @author Oskar Nilsson
 */
public class VerifySignatureTableModel extends AbstractTableModel
{
   private int recordId;
   private LogHandler lg;
   private DB2Connect dbcon;

   private String[] columnNames = {"Tid", "Medicin", "Sign"};
   private boolean[] editable = {false, false, true};
   private Object[][] data;
   private int[] rowId;

   /**
     * Creates a table model object, and initializes it
     * @param recordId The current record id.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     */
   public VerifySignatureTableModel(int recordId, DB2Connect dbcon,
         LogHandler lg)
   {
      this.recordId = recordId;
      this.dbcon = dbcon;
      this.lg = lg;

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
               "SELECT COUNT(*) FROM EPR.GIVEN_MEDICATIONS AS G " +
               "WHERE RECORD_ID = " + recordId + " AND GIVEN_BY = 0 " +
               "AND NOT EXISTS (SELECT * FROM EPR.MEDICATION_GIVEN_BY AS I " +
               "WHERE I.GIVEN_MEDICATION_ID = G.GIVEN_MEDICATION_ID)");
         rs.next();
         numRows = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT GM.GIVEN_MEDICATION_ID, M.MEDICINE_NAME, " +
               "GM.GIVEN_TIME " +
               "FROM EPR.GIVEN_MEDICATIONS AS GM, EPR.MEDICINE AS M " +
               "WHERE GM.RECORD_ID = " + recordId +
               " AND GM.MEDICINE_ID = M.MEDICINE_ID " +
               " AND GM.GIVEN_BY = 0 AND " +
               "NOT EXISTS (SELECT * FROM EPR.MEDICATION_GIVEN_BY AS I " +
               "WHERE I.GIVEN_MEDICATION_ID = GM.GIVEN_MEDICATION_ID) " +
               "ORDER BY GIVEN_TIME");
      } catch (java.sql.SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "VerifySignatureTableModel/readData",
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
            data[i][0] = rs.getTime(3);   // GIVEN_TIME
            data[i][1] = rs.getString(2); // MEDICINE_NAME
            data[i][2] = new Boolean(false);
            rowId[i] = rs.getInt(1);   // GIVEN_MEDICATION_ID
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "VerifySignatureTableModel/readData",
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
       data[row][col] = value;
       fireTableCellUpdated(row, col);
    }

    /**
      * Method that returns the record ids of the rows where sign is true.
      * @return A vector containing the ids of the selected rows.
      */
    public Vector getSelectedRows()
    {
       Vector res = new Vector();
       
       if (data == null)
          return res;
       
       for (int i=0 ; i < data.length ; i++)
       {
          if (((Boolean)data[i][2]).booleanValue())
             res.addElement(new Integer(rowId[i]));
       }
       return res;
    }
    
} //Class
