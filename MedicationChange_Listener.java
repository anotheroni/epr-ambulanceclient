import javax.swing.event.*;

/**
  * Class implementing a ChangeListener. Used by JSpinners.
  *
  * @version 20030109
  * @author Oskar Nilsson
  */
public class MedicationChange_Listener implements ChangeListener
{
 
   private MedicationPane mp;
 
   /**
     * Constructor.
     * @param mp A reference to the medication pane.
     */
   public MedicationChange_Listener(MedicationPane mp)
   {
      this.mp = mp;
   }
 
   /**
     * Method called when the state changes in the component the listener
     * is listening to.
     * @param evt The event.
     */
   public void stateChanged(ChangeEvent evt)
   {
      mp.updateConcLabel();
   }

}
