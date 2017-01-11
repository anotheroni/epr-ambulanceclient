import javax.swing.*;
import java.awt.Component;

/**
  * Class implementing a renderer for a Combo box.
  *
  * @version 021118
  * @author Oskar Nilsson
  */
class ListRenderer extends JLabel implements ListCellRenderer
{

   /**
     * Constructor, configures the renderer.
     */
   public ListRenderer()
   {
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
   }

   /**
     * Method called by the GUI to paint a component.
     * @param list The list displayed by the combo box.
     * @param value The object to render.
     * @param index Index in the combo box for the object.
     * @param isSelected True if the object is selected.
     * @param cellHasFocus True if the object has focus.
     * @return The object to render.
     */
   public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
   {
      if (isSelected) {
         setBackground(list.getSelectionBackground());
         setForeground(list.getSelectionForeground());
      } else {
         setBackground(list.getBackground());
         setForeground(list.getForeground());
      }

      ImageIcon icon = (ImageIcon)value;
      setText(icon.getDescription());
      setIcon(icon);
      return this;
   }
}
