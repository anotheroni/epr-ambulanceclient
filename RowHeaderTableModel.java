import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
  * Class that formats the row header table for the patient parameter table.
  *
  * @version 20030218
  * @author Oskar Nilsson
  */
public class RowHeaderTableModel extends AbstractTableModel
{

   private ParameterTableModel tm;

   /**
     * Constructor initzialices the local variables.
     * @param tm A reference to the main tablemodel.
     */
   public RowHeaderTableModel(ParameterTableModel tm)
   {
      this.tm = tm;
   }

    /**
      * Method that updates the table.
      */
    public void updateTable()
    {
       fireTableDataChanged();
    }

    /**
     * Method to get the number of columns in the table.
     */
    public int getColumnCount()
    {
        return 1;
    }

    /**
     * Method to get the number of rows in the table.
     */ 
    public int getRowCount()
    {
        return tm.getRowCount();
    }

    /**
     * Method to get a column name.
     */
    public String getColumnName(int col)
    {
        return null;
    }

    /**
     * Method to get the value in a cell.
     * @param row The row number.
     * @param col The column number.
     * @return The object in the specified cell.
     */
    public Object getValueAt(int row, int col)
    {
        return tm.getRowName(row);
    }

    /**
     * JTable uses this method to determine the default renderer/
     * editor for each cell. If we didn't implement this method,
     * the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c)
    {
       return Object.class;
    }

    /**
     * Method used to se if a cell is editable
     * @param row the row to check
     * @param col the column to check
     */
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    /**
     * Method to change data in a cell
     * @param value the new value
     * @param row the row to change
     * @param col the colom to change
     */
    public void setValueAt(Object value, int row, int col)
    {
       return; // Not editable
    }
       
} //Class
