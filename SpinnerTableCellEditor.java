import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import java.awt.Component;
import java.text.SimpleDateFormat;

/**
  * Cell editor that uses a JSpinner to edit table cells.
  * A SpinnerNumberModel is used to format the spinner.
  *
  * @version 021130
  * @author Oskar Nilsson
  */
public class SpinnerTableCellEditor extends AbstractCellEditor
            implements TableCellEditor
{
   final JSpinner spinner;
   private boolean hel;
 
   /**
    * Constructor, initializes the spinner with a float increment.
    * @param low The lower limit for the number spinner.
    * @param high The upper limit for the number spinner.
    * @param inc The amount to change the value for each click.
    */
   public SpinnerTableCellEditor(int low, int high, double inc) {
      spinner = new JSpinner(new SpinnerNumberModel(low, low, high, inc));
      hel = false;
   }

   /**
     * Constructor, initializes the spinner with an integer increment.
     * @param low The lower limit for the number spinner.
     * @param high The upper limit for the number spinner.
     * @param inc The amount to change the value for each click.
     */
   public SpinnerTableCellEditor(int low, int high, int inc) {
      spinner = new JSpinner(new SpinnerNumberModel(low, low, high, inc));
      hel = true;
   }
   
   /**
    * Prepares the spinner component and returns it.
    * Row one will return a date spinner, row two a number spinner.
    */
   public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column) {
      if (hel)
         spinner.setValue(new Integer(value.toString()));
      else
         spinner.setValue(new Double(value.toString()));
      return spinner;
   }

   /**
    * Returns the spinners current value.
    */
   public Object getCellEditorValue() {
      return spinner.getValue();
   }
}
