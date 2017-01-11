import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import java.sql.*;

/**
 * Class that formats the table in the planning frame
 * @version 20030315
 * @author Oskar Nilsson
 */
public class RecordTableModel extends AbstractTableModel
{
    private AmbulanceRecord ar;

    private DB2Connect dbcon;
    private LogHandler lg;
    
    private Object[][] data;
    private final String[] columnNames = {"Journal", "Signerad"};
    private final boolean[] editable = {false, false};
    private int[] rowId;

    /**
     * Constructor, creates a table model object, and initializes it.
     * @param ar Reference to AmbulanceRecord.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.    
     */
    public RecordTableModel(AmbulanceRecord ar, DB2Connect dbcon, LogHandler lg)
    {
        this.ar = ar;
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
               "SELECT COUNT(*) FROM EPR.AMBULANCE_RECORD " +
               "WHERE CARER_ID = " + ar.getUserId()); 
         rs.next();
         numRows = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT RECORD_ID, DATE, ALARM_TIME, " +
               "SIGN_TIME FROM EPR.AMBULANCE_RECORD " +
               "WHERE CARER_ID = " + ar.getUserId() +
               " ORDER BY DATE, ALARM_TIME");
      } catch (java.sql.SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordTableModel/readData",
                  "Read records"));
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
            data[i][0] = rs.getString(2) +   // DATE
               " "+ rs.getString(3);         // ALARM_TIME
            data[i][1] = rs.getString(4);    // SIGN_TIME
            rowId[i] = rs.getInt(1);         // RECORD_ID
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "RecordTableModel/readData",
                     "Read resultset"));
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
        //No data can be changed
    }

} //Class
