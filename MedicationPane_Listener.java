import java.awt.event.*;

/**
  * Class implementing an action listener for the medication pane.
  *
  * @version 20030308
  * @author Oskar Nilsson
  */
public class MedicationPane_Listener implements ActionListener
{

   private MedicationPane mp;

   /**
     * Constructor, initializes the local variables.
     * @param mp A renference to the medication pane.
     */
   public MedicationPane_Listener(MedicationPane mp)
   {
      this.mp = mp;
   }

   /**
     * Method called by the system when an action event occurs in medication
     * pane.
     * @param action The action event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
       if (command.equals("give")) {
         mp.giveMedication();
       } else if (command.equals("medicineChange")) {
          mp.updateConcLabel();
       } else if (command.equals("addeffect")) {
         mp.addEffect();
       } else if (command.equals("delete")) {
         mp.deleteMed();
       }
   }

} //class
