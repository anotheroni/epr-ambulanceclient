import java.awt.event.*;

/**
  * Class implementing an action listener for the misc pane.
  *
  * @version 20030212
  * @author Oskar Nilsson
  */
public class MiscPane_Listener implements ActionListener
{

   private MiscPane mp;
   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;
   private boolean isEditable;

   /**
     * Constructor, initializes the local variables.
     * @param mp A reference to the misc pane.
     * @param pr A reference to the current patient record.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param isEditable True if thepatient record is editable.
     */
   public MiscPane_Listener(MiscPane mp, PatientRecord pr, DB2Connect dbcon,
         LogHandler lg, boolean isEditable)
   {
      this.mp = mp;
      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;
      this.isEditable = isEditable;
   }

   /**
     * Method called by the system when a button in the record information pane
     * is pressed.
     * @param action The event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      if (command.equals("statistics")) {
         new StatisticsDialog(pr, dbcon, lg, isEditable);
      }
   }

} //class
