import java.awt.event.*;

/**
  * Class implementing an action listener for the Send dialog.
  *
  * @version 20030308
  * @author Oskar Nilsson
  */
public class SendDialog_Listener implements ActionListener
{

   private SendDialog dialog;

   /**
     * Constructor, initializes the local variables.
     * @param dialog A reference to the send dialog.
     */
   public SendDialog_Listener(SendDialog dialog)
   {
      this.dialog = dialog;
   }

   /**
     * Method called by the system when a button in the record information pane
     * is pressed.
     * @param action The event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
       if (command.equals("cancel")) {
         dialog.destroy();
      }
   }

}
