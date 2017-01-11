import java.util.*;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
  * Class implementing the GCS observation table rows in the observation table.
  *
  * @version 20030626
  * @author Oskar Nilsson
  */
public class GCSObservationTableRow implements ObservationTableRowInterface
{
   private GCSModel gcsModel;
   private DB2Connect dbcon;
   private int recordId;
   private int paramId;

   private String rowName;
   private BigComboBox combo;

   private int maxValue;
   private Boolean TRUE = new Boolean(true);
   private Boolean FALSE = new Boolean(false);

   /**
     * Constructor, initzialises the local variables.
     * @param gcsModel A reference to the GCS model.
     * @param paramId The id of the row in the GCS model.
     * @param cboxValues The options that should be available for the row.
     * @param rowName The name of the row.
     */
   public GCSObservationTableRow(GCSModel gcsModel, int paramId,
        String[] cboxValues, String rowName)
   {
      this.gcsModel = gcsModel;
      this.dbcon = dbcon;
      this.recordId = recordId;
      this.paramId = paramId;
      this.rowName = rowName;

      maxValue = cboxValues.length + 1;
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
      return gcsModel.getNewTimes();
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
      return gcsModel.addColumn(paramId, val, col);
   }
   
   /**
     * Method that returns the value in the specified column.
     * @param col The column number to get the value from.
     * @return The contents of col.
     */
   public String getValue(int col)
   {
      return gcsModel.getValue(paramId, col);
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
         return "GCS måste vara ett nummer";
      }

      if (iVal > 8 || iVal < 1) // Check that is a valid number
         return rowName + " måste vara mellan 1 och " + maxValue;

      return gcsModel.setValue(paramId, iVal, col);
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
      return gcsModel.changeTime(paramId, newTime, oldTime, col, end);
   }

   /**
     * Method that save the contents of the row.
     * @param times Times for the columns.
     * @return null if ok, an error message of save failed.
     */ 
   public String saveRow(Vector times)
   {
      return gcsModel.saveRow(paramId, times);
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
         val = gcsModel.getValue(paramId, col);
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
     * Calls the GCSModel to check if there are any values in the model.
     * @return True id there are any values, false if the row is empty.
     */
   public boolean hasValues()
   {
      return gcsModel.hasValues();
   }

}
