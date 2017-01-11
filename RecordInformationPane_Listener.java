import java.awt.event.*;

/**
  * Class implementing an action listener for the record information pane.
  *
  * @version 20030523
  * @author Oskar Nilsson
  */
public class RecordInformationPane_Listener implements ActionListener
{

   private RecordInformationPane rip;
   private PatientRecord pr;
   private DB2Connect dbcon;
   private boolean isEditable;

   /**
     * Constructor, initializes the local variables.
     * @param rip A reference to the record information pane.
     * @param pr A reference to the current patient record.
     * @param dbcon A reference to the database.
     * @param isEditable True if thepatient record is editable.
     */
   public RecordInformationPane_Listener(RecordInformationPane rip,
         PatientRecord pr, DB2Connect dbcon, boolean isEditable)
   {
      this.rip = rip;
      this.pr = pr;
      this.dbcon = dbcon;
      this.isEditable = isEditable;
   }

   /**
     * Method called by the system when a button in the record information pane
     * is pressed.
     * @param action The event.
     */
   public void actionPerformed(ActionEvent action) {
      String command = action.getActionCommand();
      if (command.equals("timefld")) {
         ((java.awt.Component) action.getSource()).transferFocus ();
      } else if (command.equals("inCityChange")) {
         rip.inCityChange();
      } else if (command.equals("outCityChange")) {
         rip.outCityChange();
      } else if (command.equals("fetchInfo")) {
         rip.fetchInfo();
      } else if (command.equals("stopFetch")) {
         rip.stopFetch();
      } else if (command.equals ("importInfo")) {
         rip.showImportInfo ();  
      } else if (command.equals ("importTime")) {
         rip.showImportTime ();
      }
   }

}
