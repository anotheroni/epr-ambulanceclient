import java.awt.event.*;
import javax.swing.JOptionPane;

/**
  * Class implementing an action listener for the login dialog.
  *
  * @version 20030407
  * @author Oskar Nilsson
  */
public class LoginDialog_Listener implements ActionListener
{

   private AmbulanceRecord ar;
   private LoginDialog ld;

   /**
     * Constructor initializes the local variables.
     * @param er The ambulance record frame to login to.
     * @param ld The login dialog that the listener listens to.
     */
   public LoginDialog_Listener(AmbulanceRecord ar, LoginDialog ld)
   {
      this.ar = ar;
      this.ld = ld;
   }

   /**
     * Method called by the system when a button is pressed in the login dialog.
     * @param action The event that happened.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      String result;
      
      if (command.equals("login")) {
         if ((result = ar.login(ld.getUserId(), ld.getPassword(),
                    ld.getDriverId(), ld.getAmbulance(), ld.getStation(),
                    ld.getUserName(), ld.getDriverName()))
                    == null)
            ld.dispose();
         else
            JOptionPane.showMessageDialog(ld, result);
      } else if (command.equals("quit")) {
         ld.dispose();
      } else if (command.equals("swap")) {
         ld.swapUsers();
      }
   }

} //class
