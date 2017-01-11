import javax.swing.event.*;

/**
  * Class implementing a ChangeListener. Used by JSpinners and JTabbedPane.
  *
  * @version 021110
  * @author Oskar Nilsson
  */
public class Change_Listener implements ChangeListener
{
   
   private PatientRecord pr;
   private String mname;
   
   /**
     * Constructor.
     * @param pr A reference to the patient record listening to.
     * @param mname The name of the method to call after an event.
     */
   public Change_Listener(PatientRecord pr, String mname)
   {
      this.pr = pr;
      this.mname = mname;
   }
   
   /**
     * Method called when the state changes in the component the listener
     * is listening to.
     * @param evt The event.
     */
   public void stateChanged(ChangeEvent evt)
   {
      if (mname.equals("timeChanged"))
         pr.timeChanged();
      else if (mname.equals("paneChanged"))
         pr.paneChanged();
   }

}
