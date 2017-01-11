import java.awt.event.*;

/**
  * Class implementing an action listener for the observations pane.
  *
  * @version 20030225
  * @author Oskar Nilsson
  */
public class ObservationsPane_Listener implements ActionListener
{

   private ObservationsPane op;

   /**
     * Constructor, initializes the local variables.
     * @param op A reference to the observation panel.
     */
   public ObservationsPane_Listener(ObservationsPane op)
   {
      this.op = op;
   }

   /**
     * Method called by the system when an action event ocurs.
     * @param action The event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      if (command.equals("atbt")) {
         op.btChange(0);
      } else if (command.equals("headbt")) {
         op.btChange(1);
      } else if (command.equals("heartbt")) {
         op.btChange(2);
      } else if (command.equals("lungsbt")) {
         op.btChange(3);
      } else if (command.equals("abdbt")) {
         op.btChange(4);
      } else if (command.equals("extrbt")) {
         op.btChange(5);
      } else if (command.equals("backbt")) {
         op.btChange(6);
      } else if (command.equals("neubt")) {
         op.btChange(7);
      } else if (command.equals("fracturebt")) {
         op.selectFracture();
      } else if (command.equals("closefracturebt")) {
         op.closeFracture();
      } else if (command.equals("luxationbt")) {
         op.selectLuxation();
      } else if (command.equals("closeluxationbt")) {
         op.closeLuxation();
      } else if (command.equals("paralysisbt")) {
         op.selectParalysis();
      } else if (command.equals("closeparalysis")) {
         op.closeParalysis();
      }
   }

} //class
