import java.awt.event.*;

/**
  * Class implementing an action listener for the patient parameters pane.
  *
  * @version 20030223
  * @author Oskar Nilsson
  */
public class AltPatientParametersPane_Listener implements ActionListener
{

   private AltPatientParametersPane pp;

   /**
     * Constructor, initzializes the local variables.
     * @param pp Reference to the parent panel.
     */
   public AltPatientParametersPane_Listener(AltPatientParametersPane pp)
   {
      this.pp = pp;
   }

   /**
     * Method called the system when buttons are pressed.
     * @param action The event.
     */
   public void actionPerformed(ActionEvent action)
   {
      String command = action.getActionCommand();
      if (command.equals("addColumn")) {
         pp.addColumn();
      } else if (command.equals("RLS")) {
         pp.rlsCh();
      } else if (command.equals("puls")) {
         pp.pulsCh();
      } else if (command.equals("bp")) {
         pp.bpCh();
      } else if (command.equals("breath")) {
         pp.breathCh();
      } else if (command.equals("gcs")) {
         pp.gcsCh();
      } else if (command.equals("oxy")) {
         pp.oxyCh();
      } else if (command.equals("sat")) {
         pp.satCh();
      } else if (command.equals("glukos")) {
         pp.glukosCh();
      } else if (command.equals("bodypos")) {
         pp.bodyposCh();
      } else if (command.equals("vas")) {
         pp.vasCh();
      } else if (command.equals("eye")) {
         pp.eyeCh();
      } else if (command.equals("showAll")) {
         pp.showAll();
      } else if (command.equals("hideAll")) {
         pp.hideAll();
      }
   }
 
} //class
