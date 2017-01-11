import java.awt.event.*;

/**
  * Class implementing an action listener for the Import info dialog.
  *
  * @version 20030523
  * @author Oskar Nilsson
  */
public class ImportInfoDialog_Listener implements ActionListener
{

   private ImportInfoDialog parent;

   /**
     * Constructor, initsializes the local variables.
     * @param parent A reference to the main frame.
     */
   public ImportInfoDialog_Listener (ImportInfoDialog parent)
   {
      this.parent = parent;
   }
   
   /**
     * Method called by the system when an action event occurs.
     * @param action The event.
     */
   public void actionPerformed (ActionEvent action)
   {
      String command = action.getActionCommand ();

      if (command.equals ("import")) {
         parent.importInfo ();
      } else if (command.equals ("remove")) {
         parent.removeEntry ();
      }
   }

}
