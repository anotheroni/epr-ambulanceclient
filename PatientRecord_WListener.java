import java.awt.event.*;

/**
  * Window listener for the Patient record frame.
  *
  * @version 20030330
  * @author Oskar Nilsson
  */
public class PatientRecord_WListener extends WindowAdapter
{
   
   private AmbulanceRecord ar;
   private PatientRecord pr;
   
   /**
     * Constructor, initializes the local variables.
     * @param ar A reference to the main frame.
     * @param pr A reference to the patient record frame the listener is used
     * by.
     */
   public PatientRecord_WListener(AmbulanceRecord ar, PatientRecord pr)
   {
      this.ar = ar;
      this.pr = pr;
   }

   /**
     * Method called by the system when the frame is closed.
     * @param e Event information.
     */
   public void windowClosing(WindowEvent e)
   {
      pr.close();
      ar.recordClosed(pr);
   }
}
