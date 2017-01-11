import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
  * Class implementing a change listener for the table in the Import time frame.
  *
  * @version 20030523
  * @author Oskar Nilsson
  */
public class ImportTimeDialog_SelectionListener implements ListSelectionListener
{

   private ImportTimeDialog parent;

   /**
     * Constructor, initializes the local variables.
     * @param parent A reference to the frame containing the table.
     */
   public ImportTimeDialog_SelectionListener (ImportTimeDialog parent)
   {
      this.parent = parent;
   }

   /**
     * Method called by the system when the selection changes in the table.
     * @param e Information about the change.
     */
   public void valueChanged (ListSelectionEvent e)
   {
      parent.listSelectionChanged ();
   }

}
