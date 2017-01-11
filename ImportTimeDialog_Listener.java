import java.awt.event.*;

/**
  * Class implementing an action listener for the Import time dialog. 
  *
  * @version 20030523
  * @author Oskar Nilsson
  */
public class ImportTimeDialog_Listener implements ActionListener
{

   private ImportTimeDialog parent;

   /**
     * Constructor, initsializes the local variables.
     * @param parent A reference to the main frame.
     */
   public ImportTimeDialog_Listener (ImportTimeDialog parent)
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
         parent.importTimes ();
      } else if (command.equals ("remove")) {
         parent.removeEntry ();
      }
   }

}
