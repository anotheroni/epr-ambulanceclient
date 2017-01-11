import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import javax.swing.text.*;

/**
  * Class implementing the main frame for the patient record.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class PatientRecord extends JFrame implements MessageInterface
{
   private AmbulanceRecord ar;
   private DB2Connect dbcon;
   private WordList wl;
   private LogHandler lg;
   private StatusField statusFld;

   private int recordId;
 
   private int userId;
   private int driverId;
   private int ambulanceId;
   private int stationId;
   private boolean isUnSigned = true;
   private String userName;
   private String driverName;

   private int selectedPane; // The selected pane in the main tabbed pane

   private JTabbedPane mainPane;
   private Font dFont;

   private RecordInformationPane recordPane;
   private MATASPane matasPane;
   private ObservationsPane observationsPane;
   private ActionsPane actionsPane;
   private MedicationPane medicationPane;
   private MiscPane miscPane;

   private AltPatientParametersPane altParametersPane;
   
   /**
     * Constuctor that creates a new patient record from scratch.
     * @param ar A reference to the main class.
     * @param dbcon A reference to the database.
     * @param wl A reference to the word list to use for auto completion.
     * @param lg A reference to the log handler.
     * @param ambulanceId Id number of the ambulance.
     * @param stationId Id number of the ambulance station.
     * @param driverId Id number of the driver.
     * @param userId Id number of the carer.
     * @param userName Username of the carer.
     * @param driverName Username of the driver.
     */
   public PatientRecord(AmbulanceRecord ar, DB2Connect dbcon, WordList wl,
         LogHandler lg, int ambulanceId, int stationId, int driverId,
         int userId, String userName, String driverName)
   {
      super("Patientjournal");
      setSize(600, 600);
      setResizable(false);

      this.ar = ar;
      this.dbcon = dbcon;
      this.wl = wl;
      this.lg = lg;
      
      this.userId = userId;
      this.driverId = driverId;
      this.ambulanceId = ambulanceId;
      this.stationId = stationId;
      this.userName = userName;
      this.driverName = driverName;

      Locale locale = new Locale("sv","SW");
      DateFormat formatter;

      formatter = DateFormat.getDateInstance(DateFormat.SHORT, locale);
      String date = formatter.format(new java.util.Date());

      formatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
      String time = formatter.format(new java.util.Date());
  
      ResultSet rs;
      try {
        dbcon.dbQueryUpdate ("INSERT INTO EPR.AMBULANCE_RECORD " +
            "(DATE, ALARM_TIME, AMBULANCE_NR, STATION_ID, DRIVER_ID, " +
            "CARER_ID, SIGN_ID, CREATION_TIME) VALUES ('" + date + "', '" +
            time + "', " + ambulanceId + ", " + stationId + ", " +
            driverId + ", " + userId + ", " + userId + ", '" + date + " " +
            time + "')");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/PatientRecord",
                  "Creating record"));
      }

      try {
         rs = dbcon.dbQuery("SELECT RECORD_ID FROM EPR.AMBULANCE_RECORD " +
               "WHERE DATE = '" + date + "' AND CREATION_TIME = '" +
               date + " " + time + "' AND ALARM_TIME = '" + time +
               "' AND DRIVER_ID = " + driverId + " AND CARER_ID = " + userId);
         rs.next();
         recordId = rs.getInt(1);   // RECORD_ID
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/PatientRecord",
                  "Reading record id"));
      }

      buildFrame();
   }

   /**
     * Constructor that opens a patient record from the database.
     * @param ar A reference to the main class.
     * @param dbcon A reference to the database.
     * @param wl A reference to the word list to use for auto completion.
     * @param lg A reference to the log handler.
     * @param recordId The id of the patient record to read from the database. 
    */
   public PatientRecord(AmbulanceRecord ar, DB2Connect dbcon, WordList wl,
         LogHandler lg, int recordId)
   {
      super("Patientjournal");
      setSize(600, 600);
      setResizable(false);

      this.ar = ar;
      this.dbcon = dbcon;
      this.wl = wl;
      this.lg = lg;

      this.recordId = recordId;

      ResultSet rs;
      try {
         rs = dbcon.dbQuery("SELECT CARER_ID, DRIVER_ID, AMBULANCE_NR, " +
               "STATION_ID, SIGN_TIME, usr.USER_NAME, drv.USER_NAME " +
               "FROM EPR.AMBULANCE_RECORD, EPR.STAFF AS usr, " +
               "EPR.STAFF AS drv " +
               "WHERE RECORD_ID = " + recordId +
               " AND CARER_ID = usr.STAFF_ID AND DRIVER_ID = drv.STAFF_ID");
         rs.next();
         userId = rs.getInt(1);  // CARER_ID
         driverId = rs.getInt(2);   // DRIVER_ID
         ambulanceId = rs.getInt(3);   // AMBULANCE_NR
         stationId = rs.getInt(4);  // STATION_ID
         isUnSigned = (rs.getTimestamp(5) == null);   // SIGN_TIME
         userName = rs.getString(6);
         driverName = rs.getString(7);
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/PatientRecord",
                  "Reading record"));
      }

      buildFrame();
   }

   /**
     * Method that builds the frame GUI. Used by the constructors.
     */
   private void buildFrame()
   {
      PatientRecord_WListener wlistener = new PatientRecord_WListener(ar,this);
      this.addWindowListener(wlistener);

      dFont = new Font("Default", Font.PLAIN, 12);

      //--- Record Panel
      recordPane = new RecordInformationPane(this, ar, dbcon, wl, lg, dFont,
           recordId, isUnSigned);

      //--- MATAS Panel
      matasPane = new MATASPane(this, dbcon, wl, lg, dFont, recordId,
           isUnSigned);

      //--- Observations Panel
      observationsPane = new ObservationsPane(this, dbcon, wl, lg, dFont, 
            recordId, isUnSigned);

      //--- Actions Panel
      actionsPane = new ActionsPane(this, dbcon, wl, lg, dFont, recordId,
            isUnSigned);
 
      //--- Medication Panel
      medicationPane = new MedicationPane(this, dbcon, wl, lg, dFont, recordId,
            isUnSigned, userId, userName, driverId, driverName);

      //--- Alt Patient Parameters Pane
      altParametersPane = new AltPatientParametersPane(this, dbcon, lg, dFont,
            recordId, isUnSigned);
 
      //--- Misc Pane
      miscPane = new MiscPane(this, dbcon, wl, lg, dFont, recordId, isUnSigned);

      //--- Main Pane
      Change_Listener paneListener = new Change_Listener(this, "paneChanged");

      mainPane = new JTabbedPane();
      mainPane.addChangeListener(paneListener);

      mainPane.add(recordPane, "Journalinfo");
      mainPane.add(matasPane, "MATAS");
      mainPane.add(observationsPane, "Observationer");
      mainPane.add(actionsPane, "Åtgärder");
      mainPane.add(medicationPane, "Mediciner");
      mainPane.add(altParametersPane, "Patientparametrar");
      mainPane.add(miscPane, "Övrigt");

      statusFld = new StatusField(dFont);
      statusFld.setFocusable(false);

      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(mainPane, BorderLayout.CENTER);
      this.getContentPane().add(statusFld, BorderLayout.SOUTH);

      selectedPane = 0;
   }
 
   /**
     * Method used by the change listener to signal that the tabbed pane has
     * changed.
     * Saves to old pane state to the database.
     */
   public void paneChanged()
   {
      int newPane = mainPane.getSelectedIndex();
      if (newPane == selectedPane)
         return;
      try {
         switch (selectedPane) {
            case 0:
               recordPane.savePane();
               break;
            case 1:
               matasPane.savePane();
               break;
            case 2:
               observationsPane.savePane();
               break;
            case 3:
               actionsPane.savePane();
               break;
            case 4:
               medicationPane.savePane();
               break;
            case 5:
               altParametersPane.savePane();
               break;
            case 6:
               miscPane.savePane();
               break;
         }
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/paneChanged",
                  "Saving pane " + selectedPane));
      }

      selectedPane = newPane;
      statusFld.clear();
   }
   
   /**
     * Method used by the change listener to set the time changed flag.
     */ 
   public void timeChanged()
   {
      recordPane.timeChanged();
   }
   
   /**
     * Method used to get the id of the record.
     * @return Id of the record.
     */
   public int getRecordId()
   {
      return recordId;
   }

   /**
     * Method used to get the user id for the owner of the record.
     * @return User id of the record owner.
     */
   public int getUserId()
   {
      return userId;
   }

   /**
     * Metohd used to get the user id of the driver.
     * @return User id of the driver.
    */
   public int getDriverId()
   {
      return driverId;
   }

   /**
    * Method used to close the patient record.
    */
   public void close()
   {
      try {
         recordPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save record"));
      }
      try {
         matasPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save matas"));
      }
      try {
         observationsPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save observation"));
      }
      try {
         actionsPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save actions"));
      }
      try {
         medicationPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save medication"));
      }
      try {
         altParametersPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save parameters"));
      }
      try {
         miscPane.savePane();
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "PatientRecord/close",
                  "save misc"));
      }
      dispose();
   }

   /**
     * Metohd that adds a comment to the comment text area in the misc pane.
     * @param comment The comment to add.
     */
   public void addComment (String comment)
   {
      miscPane.addComment (comment);
   }

   public void addDiagnosis(String diagnosis) 
   {
       matasPane.addDiagnosis(diagnosis);
   }

   /**
     * Method that updates the contents of the status field.
     * If the string has length 0 nothing is added.
     * @param message The text to update the status field with.
     */
   public void setMessage(String message)
   {
      if (message != null && message.length () > 0)
         statusFld.setText(message);
   }
}
