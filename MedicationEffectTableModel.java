import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.sql.*;

/**
 * Class that formats the medication effects table in the medication pane
 *
 * @version 20030904
 * @author Oskar Nilsson
 */
public class MedicationEffectTableModel extends AbstractTableModel
{
   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;

   private String[] columnNames = {"Tid", "Effekt"};
   private boolean[] editable = {true, true};
   private Object[][] data;

   private int given_medicine_id;
   //private SimpleDateFormat timeFormat; 

   /** 
     * Creates a table model object, and initializes it
     * @param pr Reference to the Patient Record.
     * @param dbcon A reference to the database
     * @param lg The log habdler to report errors to.
     */
   public MedicationEffectTableModel(PatientRecord pr, DB2Connect dbcon,
         LogHandler lg)
   {
      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;
      //timeFormat = new SimpleDateFormat("HH:mm:ss");

      readData(0);
   }

   /**
    * Method that fills the data array with data from the database.
    * @param medicineId Id number of the medication event that the effect
    * belongs to.
    */
   private void readData(int medicineId)
   {
      ResultSet rs = null;
      int numRows = 0;
      given_medicine_id = medicineId;

      try {
         rs = dbcon.dbQuery(
               "SELECT COUNT(*) FROM EPR.MEDICINE_EFFECTS " +
               "WHERE GIVEN_MEDICINE_ID = " + medicineId);
         rs.next();
         numRows = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT EFFECT_TIME, EFFECT " +
               "FROM EPR.MEDICINE_EFFECTS " +
               "WHERE GIVEN_MEDICINE_ID = " + medicineId +
               " ORDER BY EFFECT_TIME");
      } catch (java.sql.SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "medicationeffecttablemodel/readdata",
                  "Reading medicine effects"));
      }
 
      if (numRows == 0)
         data = null;
      else
         data = new Object[numRows][columnNames.length];

      for(int i=0 ; i < numRows ; i++)
      {
         try {
            rs.next();
            data[i][0] = rs.getTime("EFFECT_TIME");
            data[i][1] = rs.getString("EFFECT");
         } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "medicationeffecttablemodel/readdata",
                  "Reading result set"));
         }
      }
    }

    /**
      * Method that updates the table by reading data from the database.
      * @param medicineId Id number of the medication event that the effect
      * belongs to.
      */
    public void updateTable(int medicineId)
    {
       readData(medicineId);
       fireTableDataChanged();
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
       String query = null;
 
       switch (col) {
          case 0:
             query = new String("UPDATE EPR.MEDICINE_EFFECTS " +
                  "SET EFFECT_TIME = '" + value.toString () + 
                  "' WHERE GIVEN_MEDICINE_ID = " + given_medicine_id +
                  " AND EFFECT_TIME = '" + data[row][0] + //timeFormat.format(data[row][0]) +
                  "'");
             break;
          case 1:
             query = new String("UPDATE EPR.MEDICINE_EFFECTS " +
                  "SET EFFECT = '" + value +
                  "' WHERE GIVEN_MEDICINE_ID = " + given_medicine_id +
                  " AND EFFECT_TIME = '" + data[row][0] + //timeFormat.format(data[row][0]) +
                  "'");
                  break;
       }
       if (query != null)
       {
          try {
             dbcon.dbQueryUpdate (query);
             data[row][col] = value;
          } catch (SQLException e) {
             // Don't log error if the time is incorect, only give an error msg
             if (e.getSQLState().equals("22007"))
                pr.setMessage ("Värdet är felaktigt");
             else // Not a time error, log it
                lg.addLog(new Log(e.getMessage(),
                         "medicationeffecttablemodel/setValueAt",
                         "Update medicine effects"));
          }
          fireTableCellUpdated(row, col);
       }
    }

} //Class
