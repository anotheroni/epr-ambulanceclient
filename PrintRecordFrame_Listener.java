import java.awt.event.*;

/**
  * Class implementing an action listener for the print parient record frame.
  *
  * @version 20030226
  * @author Oskar Nilsson
  */
public class PrintRecordFrame_Listener implements ActionListener
{

   private PrintRecordFrame prf;

   /**
     * Constructor initializes the local vriables.
     * @param prf A reference to the print frame.
     */
   public PrintRecordFrame_Listener(PrintRecordFrame prf)
   {
      this.prf = prf;
   }

   /**
     * Method called by the system to handle action events.
     * @param action Information about the event
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      
      if (command.equals("quit")) {
         prf.quit();
      } else if (command.equals("print")) {
         prf.print();
      } else if (command.equals("next")) {
         prf.nextPage();
      } else if (command.equals("prev")) {
         prf.prevPage();
      }
   }

} //class
