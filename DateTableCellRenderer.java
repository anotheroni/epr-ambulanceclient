import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.awt.Color;
import java.text.DateFormat;
import java.util.Locale;
import javax.swing.border.Border;

/**
  * Class that implements a table cell renderer that displays a time in
  * HH:mm:ss format. The renderer extends a component. It is used each time a
  * cell must be displayed.
  *
  * @version 20030626
  * @author Oskar Nilsson
  */
public class DateTableCellRenderer extends JLabel implements TableCellRenderer
{
   private LogHandler lg;
   
   private DateFormat formatter;
   private Border focusBorder = null;
   private Border unselectedBorder = null;
   private Border selectedBorder = null;

   /**
     * Constructor, initializes the date formater.
     */   
   public DateTableCellRenderer(LogHandler lg)
   {
      this.lg = lg;
	   formatter = DateFormat.getTimeInstance(DateFormat.DEFAULT,
           new Locale("sv","SW"));
      setOpaque(true); 
   }
   
   /**
     * Method is called each time a cell in a column using this renderer
     * needs to be rendered.
     * @param table A reference to the table the cell exists in.
     * @param value The value contained in the cell.
     * @param isSelected True if the cell is selected.
     * @param hasFocus True if the cell has focus.
     * @param rowIndex The row index of the cell.
     * @param vColIndex The column index of the cell.
     */
   public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
   {
      setText (value.toString ());
      
      if (isSelected)
      {
         if (hasFocus) {
            setBackground(table.getBackground());
            if (focusBorder == null) {
               focusBorder = BorderFactory.createMatteBorder(1,1,1,1,
                                 table.getGridColor());
            }
            setBorder(focusBorder);
         } else {
            setBackground(table.getSelectionBackground());
            if (selectedBorder == null) {
               selectedBorder = BorderFactory.createMatteBorder(1,1,1,1,
                     table.getSelectionBackground());
            }
            setBorder(selectedBorder);
         }
      }
      else
      {
         setBackground(table.getBackground());
         if (unselectedBorder == null)
            unselectedBorder = BorderFactory.createMatteBorder(1,1,1,1,
                  table.getBackground());
         // An invisible border is needed else the text jumps when selected
         setBorder(unselectedBorder);      
      }

      // Since the renderer is a component, return itself
      return this;
   }

   // The following methods override the defaults for performance reasons
   public void validate() {}
   public void revalidate() {}
   protected void firePropertyChange(String propertyName, Object oldValue,
         Object newValue) {}
   public void firePropertyChange(String propertyName, boolean oldValue,
         boolean newValue) {}
}
