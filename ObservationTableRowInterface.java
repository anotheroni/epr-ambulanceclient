import java.util.Vector;
import java.util.Date;
import javax.swing.table.TableCellEditor;

/**
  * Interafce for the rows used by the observations table.
  *
  * @version 20030626
  * @author Oskar Nilsson
  */
public interface ObservationTableRowInterface
{

   /**
     * Method used by the table to check if the row has any columns that 
     * doesn't exist in the table.
     * @return The new times in the row that doesn't exist in the table.
     */
   public Vector getNewTimes();

   /**
     * Method that returns the row header text.
     * @return The name of the row.
     */
   public String getRowName();

   /**
     * Method that adds a new column at the end of the row with value val.
     * @param val The value of the column.
     * @param col The column number to add.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String addColumn(String val, int col);
   
   /**
     * Method that returns the value in the specified column.
     * @param col The column number to get the value from.
     * @return The contents of col.
     */
   public String getValue(int col);

   /**
     * Method that updates the value of column.
     * @param val The new value of column.
     * @param col The column number to update.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String setValue(String val, int col);

   /**
     * Method that informs the row that the time for a column has been updated.
     * @param newTime The new time.
     * @param oldTime The old time.
     * @param col The old column number.
     * @param end The new column number.
     * @return null if all is ok, an error message otherwise.
     */
   public String changeTime(Date newTime, Date oldTime, int col, int end);

   /**
     * Method that save the contents of the row.
     * @param times Times for the columns.
     * @return null if ok, an error message of save failed.
     */ 
   public String saveRow(Vector times);

   /**
    * Method that returns the editor that should be used to edit the row.
    * @param col The column to get the editor for.
    * @return The TabelCellEditor that the row uses.
     */
   public TableCellEditor getCellEditor(int col);

   /**
     * Method that checks if there are any values in the row.
     * @return True id there are any values, false if the row is empty.
     */
   public boolean hasValues();

}
