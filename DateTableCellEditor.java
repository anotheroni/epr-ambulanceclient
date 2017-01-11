import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import java.awt.Component;
import java.text.SimpleDateFormat;

/**
  * Class implementing a cell editor that uses a time limited textfield to
  * edit a time in the format HH:mm:ss.
  *
  * @version 20030705
  * @author Oskar Nilsson
  */
public class DateTableCellEditor extends AbstractCellEditor
            implements TableCellEditor
{
   final JTextField cellEditorFld = new JTextField ();

   private MedicationPane f = null;

   /**
     * Constructor, initializes the textfield.
     */
   public DateTableCellEditor()
   {
      ((AbstractDocument)cellEditorFld.getDocument()).
         setDocumentFilter(new LimitedTimeFilter(8, cellEditorFld));
   }

   /**
     * Constructor, initializes the textfield and adds a ComponentFocusListener
     * to it.
     * @param f The object that should receive focus events.
     */
   public DateTableCellEditor (MedicationPane f)
   {
      this.f = f;
      ComponentFocusListener f_listener =
         new ComponentFocusListener (f, cellEditorFld);
      cellEditorFld.addFocusListener (f_listener);
      ((AbstractDocument)cellEditorFld.getDocument()).
         setDocumentFilter(new LimitedTimeFilter(8, cellEditorFld));
   }

   /**
    * Prepares the textfield component and returns it.
    * @param table The table that the cell is in.
    * @param value The value to edit.
    * @param isSelected True if the cell is selected.
    * @param row The row the cell is in.
    * @param column The column the cell is in.
    * @return A JSpinner displaying value.
    */
   public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column)
   {
      cellEditorFld.setText (value.toString ());
      if (f != null)
         f.setLastCell (row);
      return cellEditorFld;
   }

   /**
     * Method that returns the textfields current value.
     * @return The current textfiled text.
     */
   public Object getCellEditorValue()
   {
      return cellEditorFld.getText ();
   }
}
