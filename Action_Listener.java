import java.awt.event.*;
import javax.swing.JOptionPane;

/**
  * Class implementing an action listener for the ambulance parient
  * record client.
  *
  * @version 20030315
  * @author Oskar Nilsson
  */
public class Action_Listener implements ActionListener
{

   private AmbulanceRecord ar;
   private DB2Connect dbcon;
   private LogHandler lg;

   /**
     * Constructor, initsializes the local variables.
     * @param ar A reference to the main frame.
     * @param dbcon A reference to the database.
     */
   public Action_Listener(AmbulanceRecord ar, DB2Connect dbcon, LogHandler lg)
   {
      this.ar = ar;
      this.dbcon = dbcon;
      this.lg = lg;
   }
   
   /**
     * Method called by the system when an action event occurs.
     * @param action The event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      
      if (command.equals("login")) {
         LoginDialog ld = new LoginDialog(ar, dbcon, lg);
      } else if (command.equals("logout")) {
         ar.logOut();
      } else if (command.equals("sync")) {
         ar.synchronize();
      } else if (command.equals("fetch")) {
         
      } else if (command.equals("new")) {
         ar.newRecord();
      } else if (command.equals("open")) {
         ar.openRecord();
      } else if (command.equals("send")) {
           ar.sendRecords();
      } else if (command.equals("print")) {
         ar.printRecord();
      } else if (command.equals("sign")) {
         ar.signRecord();
      }
   }

} //class
