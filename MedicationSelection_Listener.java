import javax.swing.JTable;
import javax.swing.event.*;

/**
  * Class implementing a change listener for the medication table.
  *
  * @version 021113
  * @author Oskar Nilsson
  */
public class MedicationSelection_Listener implements ListSelectionListener
{
   private JTable table;
   private MedicationPane mp;

   /**
     * Constructor, sets the references.
     * @param mp A reference to the panel containing the table.
     * @param table A reference to the table the listener is listening to.
     */
   public MedicationSelection_Listener(MedicationPane mp, JTable table)
   {
      this.mp = mp;
      this.table = table;
   }

   /**
     * Method called by the system when table selction is changed.
     * @param e A reference to the event information.
     */
   public void valueChanged(ListSelectionEvent e)
   {
      // Check that it is the correct table and that the mouse button is
      // released else mouse selection will result in two events.
      if (e.getSource() == table.getSelectionModel() &&
          !e.getValueIsAdjusting())
      {
         mp.rowSelectionChanged();
      }
   }
}
