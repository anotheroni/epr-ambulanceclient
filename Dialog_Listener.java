import javax.swing.JOptionPane;
import java.awt.event.*;

/**
  * Action listener user by dialogs in the admin database client.
  *
  * @version 021207
  * @author Oskar Nilsson
*/
public class Dialog_Listener implements ActionListener
{

   private AdminDialog ad;

   /**
     * Constructor, sets object variables.
     */
   public Dialog_Listener(AdminDialog ad)
   {
      this.ad = ad;
   }

   /**
     * Metohd called by the system when a button in the dialog is pressed.
     * @param action Information about the event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      String result;

      if (command.equals("ok")) {
         ad.okPressed();
      } else if (command.equals("quit")) {
         ad.cancelPressed();
      }
      
   }

}
