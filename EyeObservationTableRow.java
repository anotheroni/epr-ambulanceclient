import java.util.*;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
  * Class implementing the Eye observation table rows in the observation table.
  *
  * @version 20030626
  * @author Oskar Nilsson
  */
public class EyeObservationTableRow implements ObservationTableRowInterface
{
   private EyeModel eyeModel;
   private DB2Connect dbcon;
   private int recordId;
   private int paramId;

   private String rowName;
   private BigComboBox combo;

   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   /**
     * Constructor, initzialises the local variables.
     * @param eyeModel A reference to the Eye model.
     * @param paramId The id of the row in the Eye model.
     * @param cboxValues The options that should be available for the row.
     * @param rowName The name of the row.
     */
   public EyeObservationTableRow(EyeModel eyeModel, int paramId,
        String[] cboxValues, String rowName)
   {
      this.eyeModel = eyeModel;
      this.dbcon = dbcon;
      this.recordId = recordId;
      this.paramId = paramId;
      this.rowName = rowName;

      combo = new BigComboBox(cboxValues);
      Dimension d = combo.getPreferredSize();
      combo.setPopupWidth(d.width);
   }
   
   /**
     * Method used by the table to check if the row has any columns that 
     * doesn't exist in the table.
     * @return The new times in the row that doesn't exist in the table.
     */
   public Vector getNewTimes()
   {
      return eyeModel.getNewTimes();
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
     * Method that adds a new column at the end of the row with value val.
     * @param val The value of the column.
     * @param col The column number to add.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String addColumn(String val, int col)
   {
      return eyeModel.addColumn(paramId, val, col);
   }
   
   /**
     * Method that returns the value in the specified column.
     * @param col The column number to get the value from.
     * @return The contents of col.
     */
   public String getValue(int col)
   {
      return eyeModel.getValue(paramId, col);
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
      return eyeModel.setValue(paramId, val, col);
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
      return eyeModel.changeTime(paramId, newTime, oldTime, col, end);
   }

   /**
     * Method that save the contents of the row.
     * @param times Times for the columns.
     * @return null if ok, an error message of save failed.
     */ 
   public String saveRow(Vector times)
   {
      return eyeModel.saveRow(paramId, times);
   }

   /**
     * Method that returns the editor that should be used to edit the row.
     * @param col The column to get the editor for.
     * @return The TabelCellEditor that the row uses.
     */
   public TableCellEditor getCellEditor(int col)
   {
      String val;
      try {
         val = eyeModel.getValue(paramId, col);
      } catch (ArrayIndexOutOfBoundsException e) {
         return new DefaultCellEditor(combo);
      }
      combo.setSelectedItem(val);

      return new DefaultCellEditor(combo);
   }

   /**
     * Method that checks if there are any values in the row.
     * Calls EyeModel to check if there are any values in the model.
     * @return True id there are any values, false if the row is empty.
     */
   public boolean hasValues()
   {
      return eyeModel.hasValues();
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
