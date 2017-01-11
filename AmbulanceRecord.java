import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.util.prefs.BackingStoreException;

/**
  * Main class for the Ambulance Record application.
  * 
  * @version 20030904
  * @author Oskar Nilsson
  */
public class AmbulanceRecord extends JFrame
{
   private final String SERVERADDR = "u07w255";
   private final int SERVERPORT = 1080;
   private final int SERVERPNRPORT = 1090;

   private final String VLLPATH = "images/vll.gif";
   private ImageIcon vllImg;

   private static DB2Connect dbcon;
   private static WordList wl;
   private static LogHandler lg;
   private Vector patientRecords;
   private StatusField statusFld;
   private ClientKeytool serverCertTest, clientCertTest;
 
   private SynchronizeDialog sd = null;
   private SendDialog sendDialog;

   private int userId = 0;
   private int driverId = 0;
   private int ambulanceId = 0;
   private int stationId = 0;
   private int medLevel = 0;
   private boolean loggedOn = false;
   private String userName;
   private String driverName;
 
   private RecordTableModel recordTModel;
   private JTable recordTable;
 
   private final char[] hexChars ={'0','1','2','3','4','5','6','7','8',
                                   '9','A','B','C','D','E','F'};

   private JButton logBt;
   private JButton openBt;
   private JButton newBt;
   private JButton sendBt;
   private JButton printBt;
   private JButton signBt;
 
   /**
     * Constructor, creates the record table window.
     */
   public AmbulanceRecord()
   {
      super("Ambulansjournal");
      setSize(400,400);

      Insets ins = new Insets(1,1,1,1);

      Action_Listener listener = new Action_Listener(this, dbcon, lg);
      patientRecords = new Vector();

      // Record list
      JButton syncBt = new JButton(new ImageIcon("images/Refresh24.gif"));
      syncBt.setMargin(ins);
      syncBt.setToolTipText("Synkronisera databasen med servern");
      syncBt.setActionCommand("sync");
      syncBt.addActionListener(listener);
      syncBt.setMnemonic(KeyEvent.VK_Y);

      openBt = new JButton(new ImageIcon("images/Open24.gif"));
      openBt.setMargin(ins);
      openBt.setToolTipText("Öppna journal");
      openBt.setEnabled(false);
      openBt.setActionCommand("open");
      openBt.addActionListener(listener);
      openBt.setMnemonic(KeyEvent.VK_O);
 
      newBt = new JButton(new ImageIcon("images/New24.gif"));
      newBt.setMargin(ins);
      newBt.setToolTipText("Skapa en ny journal");
      newBt.setEnabled(false);
      newBt.setActionCommand("new");
      newBt.addActionListener(listener);
      newBt.setMnemonic(KeyEvent.VK_N);

      signBt = new JButton(new ImageIcon("images/Edit24.gif"));
      signBt.setMargin(ins);
      signBt.setToolTipText("Signera journal");
      signBt.setEnabled(false);
      signBt.setActionCommand("sign");
      signBt.addActionListener(listener);
      signBt.setMnemonic(KeyEvent.VK_I);

      sendBt = new JButton(new ImageIcon("images/SendMail24.gif"));
      sendBt.setMargin(ins);
      sendBt.setToolTipText("Skicka journal till servern");
      sendBt.setEnabled(false);
      sendBt.setActionCommand("send");
      sendBt.addActionListener(listener);
      sendBt.setMnemonic(KeyEvent.VK_S);
 
      printBt = new JButton(new ImageIcon("images/Print24.gif"));
      printBt.setMargin(ins);
      printBt.setToolTipText("Skriv ut journal");
      printBt.setEnabled(false);
      printBt.setActionCommand("print");
      printBt.addActionListener(listener);
      printBt.setMnemonic(KeyEvent.VK_P);

      logBt = new JButton(new ImageIcon("images/Key24.gif"));
      logBt.setMargin(ins);
      logBt.setToolTipText("Logga in");
      logBt.setActionCommand("login");
      logBt.addActionListener(listener);
      logBt.setEnabled(false);
      logBt.setMnemonic(KeyEvent.VK_L);

      JPanel northListPanel = new JPanel();
      northListPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      northListPanel.add(newBt);
      northListPanel.add(openBt);
      northListPanel.add(printBt);
      northListPanel.add(signBt);
      northListPanel.add(sendBt);
      northListPanel.add(syncBt);
      northListPanel.add(logBt);

      recordTModel = new RecordTableModel(this, dbcon, lg);
      recordTable = new JTable(recordTModel);

      recordTable.addMouseListener(new java.awt.event.MouseAdapter()
         {
            private boolean working = false;
            
            public void mouseClicked(MouseEvent e)
            {
               if (e.getClickCount() >= 2 && !working)
               {
                   working = true;  // Prevent multiple double clicks
                   openRecord ();
                   working = false;
               } 
            }
         }
      );

      recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      // Set tab to be a traversal key
      recordTable.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      recordTable.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
      
      JScrollPane recordScrollPane = new JScrollPane(recordTable);
 
      recordScrollPane.setBorder(BorderFactory.createTitledBorder(
               "Tillgängliga journaler"));
     
      // Status Fld
      statusFld = new StatusField(new Font("Default", Font.PLAIN, 12));
      statusFld.setFocusable(false);

      // Main Layout
      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(northListPanel, BorderLayout.NORTH);
      contentPane.add(recordScrollPane, BorderLayout.CENTER);
      contentPane.add(statusFld, BorderLayout.SOUTH);
   }

   /**
     * Method that checks the local cerificates to see if they are valid.
     * Prints a warning message if it's less than 10 days left until it is
     * unvalid.
     */
   public void checkCerts()
   {
      try {
         clientCertTest = new ClientKeytool();
         if (clientCertTest.getMessage() != null)
            statusFld.setWarning(clientCertTest.getMessage());
      } catch (Exception e) {
         statusFld.setText("Ett fel upstod vid kontroll av cerifikaten");
      }
   }
   
   /**
     * Meyhod that returns the current user id.
     * @return The current user id if a user is logged on else 0.
     */
   public int getUserId()
   {
      if (loggedOn)
         return userId;
      else
         return 0;
   }

   /**
     * Method that returns the current users username.
     * @return The current users username.
     */
   public String getUserName()
   {
      return userName;
   }

   /**
     * Method that creates a new patient record.
     */
   public void newRecord()
   {
      PatientRecord pr = new PatientRecord(this, dbcon, wl, lg, ambulanceId,
           stationId, driverId, userId, userName, driverName);
      pr.show();
      patientRecords.add(pr);
      recordTModel.updateTable();
   }
  
   /**
     * Method that sends patient record to the server.
     */
   public void sendRecords()
   {
      int row = recordTable.getSelectedRow();
      int id;
      PatientRecord pr;
      
      if (row != -1)
      {
         int res = JOptionPane.showConfirmDialog(this,
               "Journalen kommer efter sändning att tas bort från den " +
               "lokala klienten, fortsät sändning?",
               "Skicka journal till servern", 
               JOptionPane.YES_NO_OPTION);
         if (res == JOptionPane.NO_OPTION)
            return;
         
         id = recordTModel.getRowId(row);
         for (int i=0 ; i < patientRecords.size() ; i++)
         {
            pr = (PatientRecord) patientRecords.elementAt(i);
            if (pr.getRecordId() == id)
            {
               JOptionPane.showMessageDialog(this,
                     "Journalen är öppen, stäng den och försök igen",
                     "Sändningsfel", JOptionPane.ERROR_MESSAGE);
               return;
            }
         }

         int t[] = {id};
         new SendDialog(this, "Server komunikation",
               "Skickar journalen till servern", lg, dbcon, t);
      }
      else
         statusFld.setText("Ingen journal är vald!");
   }

   /**
     * Callback method for send dialog that sets the reference to send
     * dialog in AmbulanceRecord. Needed because the constructor doesn't
     *  return until the threads are finished. Resulted in a null pointer
     * exception in resultsFromSend.
     * @param sendDialog A reference to the send dialog.
     */
   public void setSendDialog(SendDialog sendDialog)
   {
      this.sendDialog = sendDialog;
   }

   /**
     * Method used by the send thread to tell that transmission succeded.
     * @param oldId The record id of the record that were succesfully
     * transmited. -1 certificate problem, -2 send failed.
     * @param serverId The record id that the reocord has on the server.
     * -1 certificate problem, -2 send failed
     * @param msg Message to inform the user of what went wrong.
     */
   public void resultsFromSend(int oldId, int serverId, String msg)
   {
      if (oldId < 0)
      {
         sendDialog.setMessage(msg);
         sendDialog.addOk();
         sendDialog = null;
      }
      else
      {
         // Remove the record
         try {
            dbcon.dbQueryUpdate (
                  "DELETE FROM EPR.AMBULANCE_RECORD WHERE RECORD_ID = " +
                  oldId);
         } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                  "Sändingen lyckades, men journalen kunde inte tas bort");
            lg.addLog(new Log(e.getMessage(),
                    "AmbulanceRecord/resultsFromSend",
                    "Remove record failed"));
            
            return;
         }
         recordTModel.updateTable();
         sendDialog.setMessage("Sändningen lyckades");
         sendDialog.addOk();
         sendDialog = null;
      }
   }

   /**
     * Method that opens an existing patient record.
     * The post selected in the record table will be opened.
     */
   public void openRecord()
   {
      int row = recordTable.getSelectedRow();
      int id;
      PatientRecord pr;
 
      if (row != -1)
      {
         id = recordTModel.getRowId(row);
         for (int i=0 ; i < patientRecords.size() ; i++)
         {
            pr = (PatientRecord) patientRecords.elementAt(i);
            if (pr.getRecordId() == id)
            {
               pr.requestFocus();
               return;
            }
         }
         pr = new PatientRecord(this, dbcon, wl, lg, id);
         pr.show();
         patientRecords.add(pr);
      }
      else
         statusFld.setText("Ingen journal är vald!");
   }
 
   /**
     * Method called when a patient record is closed.
     * @param pr The patient record that is to be closed.
     */
   public void recordClosed(PatientRecord pr)
   {
      patientRecords.remove(pr);
      recordTModel.updateTable();
      try {
         recordTable.setRowSelectionInterval(recordTable.getRowCount()-1,
            recordTable.getRowCount()-1);
      } catch (IllegalArgumentException e) { ; }
  }
  
   /**
     * Method that opens the preview frame for printing the selected patient
     * record.
     */
   public void printRecord()
   {
      int row = recordTable.getSelectedRow();
      if (row != -1)
         new PrintRecordFrame(dbcon, lg, recordTModel.getRowId(row));
      else
         statusFld.setText("Ingen journal är vald!");
   }

   /**
     * Method that signs the selected patient record.
     */
   public void signRecord()
   {
      int row = recordTable.getSelectedRow();
      if (row != -1)
      {
         ResultSet rs, delDocRS;
         int recordId = recordTModel.getRowId(row);
         String docName = null;

         try { // Check if the record already is signed
            rs = dbcon.dbQuery("SELECT SIGN_TIME FROM EPR.AMBULANCE_RECORD " +
                  "WHERE RECORD_ID = " + recordId);
            rs.next();
            if (rs.getTimestamp(1) != null)
            {
               statusFld.setText("Journalen är redan signerad");
               rs.close();
               return;
            }
            rs.close();
         } catch (SQLException e) {
             lg.addLog(new Log(e.getMessage(),
                    "AmbulanceRecord/signRecord",
                    "Checking signature"));
             return;
         }

         int res = JOptionPane.showConfirmDialog(this,
               "En signerad journal kan ej ändras, vill du signera?",
               "Signera journal", 
               JOptionPane.YES_NO_OPTION);
         if (res == JOptionPane.NO_OPTION)
            return;

         try { // Check if a delegation is needed for the medicine
            rs = dbcon.dbQuery("SELECT M.MEDICINE_NAME " +
                  "FROM EPR.GIVEN_MEDICATIONS AS GM, EPR.MEDICINE AS M " +
                  "WHERE GM.RECORD_ID = " + recordId +
                  " AND GM.MEDICINE_ID = M.MEDICINE_ID" +
                  " AND M.MEDICINE_LEVEL > " + medLevel);
            delDocRS = dbcon.dbQuery("SELECT DELEGATING_DOCTOR FROM " +
                  "EPR.AMBULANCE_RECORD WHERE RECORD_ID = " + recordId);
            if (delDocRS.next())
            {
               docName = delDocRS.getString("DELEGATING_DOCTOR");
               if (docName != null && docName.length() <= 0)
                  docName = null;
            }
            delDocRS.close();
            if (rs.next() && docName == null) // Need delegation
            {
               Vector medVec = new Vector();
               do {
                  medVec.add(rs.getString(1));  // MEDICINE_NAME
               } while (rs.next());

               DelegationDialog delDiag = new DelegationDialog(this, medVec);
               if (delDiag.getResult() == null)  // Pressed cancel
               {
                  delDiag.dispose();
                  return;
               }
               dbcon.dbQueryUpdate (
                     "UPDATE EPR.AMBULANCE_RECORD SET DELEGATING_DOCTOR = '" +
                     delDiag.getResult() + "' WHERE RECORD_ID = " + recordId);
               delDiag.dispose();
            }
            rs.close();

        } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "AmbulanceRecord/signRecord",
                     "Signing record"));
            return;
         } 

         boolean result = false;

         try { // Check if "another" has given medicine
            rs = dbcon.dbQuery("SELECT MEDICINE_ID FROM " +
                  "EPR.GIVEN_MEDICATIONS AS GM " +
                  "WHERE RECORD_ID = " + recordId + " AND GIVEN_BY = 0 AND " +
                  "NOT EXISTS (SELECT * FROM EPR.MEDICATION_GIVEN_BY AS I " +
                  "WHERE I.GIVEN_MEDICATION_ID = GM.GIVEN_MEDICATION_ID)");
            if (rs.next())
            {
               VerifySignatureDialog vsd = new 
                  VerifySignatureDialog(this, recordId, dbcon, lg);
               result = vsd.showDialog();
            }
            else
            {
               result = true;
            }
            rs.close();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "AmbulanceRecord/signRecord",
                     "Select given medications"));
            return;
         }
         
         if (result)
         {
            try { // Update the database
               dbcon.dbQueryUpdate (
                     "UPDATE EPR.AMBULANCE_RECORD SET SIGN_TIME = " +
                  "CURRENT TIMESTAMP WHERE RECORD_ID = " + recordId);
               recordTModel.updateTable();
               recordTable.setRowSelectionInterval(row, row);
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "AmbulanceRecord/signRecord",
                        "Update the database"));
            }
         }
      } 
      else
         statusFld.setText("Ingen journal är vald!");
   }
   
   /**
     * Method that quits the program.
     * @param code Exit code.
     */
   public void quit(int code)
   {
      dbcon.disconnect();
      System.exit(code);
   }

   /**
     * Method that checks if the password is correct and logs in
     * the user.
     * @param user ID-number of the user.
     * @param password The password.
     * @param driver ID-number of the driver.
     * @param ambulance ID-number of the ambulance.
     * @param station ID-number of the ambulance station.
     * @param userName Username of the user.
     * @param driverName Username of the driver.
     * @return null if login successfull else an error message.
     */
   public String login(int user, String passwd, int driver,
         int ambulance, int station, String userName, String driverName)
   {
      MessageDigest algorithm = null;
      ResultSet rs = null;
      String dbPasswd = "";   // User password in the database
      String inPasswd = null; // The password entered by the user
      byte digest[] = null;

      try {
         rs = dbcon.dbQuery("SELECT PASSWORD, MEDICAL_LEVEL FROM EPR.STAFF " +
               "WHERE STAFF_ID = " + user + "");
         rs.next();
         dbPasswd = rs.getString(1);
         medLevel = rs.getInt(2);
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "AmbulanceRecord/login",
                  "Find user"));
         return "Användaren hittades inte i databasen";
      }

      try {
            algorithm = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException e) {
         lg.addLog(new Log(e.getMessage(),
                  "AmbulanceRecord/login",
                  "Digest algorithm"));
      }

      //hash the inputted password
      algorithm.reset();
      algorithm.update(passwd.getBytes());
      digest = algorithm.digest();

      //convert the digest to a string
      inPasswd = hexStringFromBytes(digest);

      if(!dbPasswd.equals(inPasswd))
      {
         try {
            rs = dbcon.dbQuery("SELECT FAILED_ATTEMPTS FROM " +
                  "EPR.BLOCKED_USERS WHERE USER_ID = " + user);
            if (rs.next()) // Has failed before
            {
               int attempts = rs.getInt(1);
               if (attempts >= 2)   // Block user
               {
                  dbcon.dbQueryUpdate (
                        "UPDATE EPR.STAFF SET DISABLE = 1 WHERE " +
                        "STAFF_ID = " + user);
                  dbcon.dbQueryUpdate (
                        "UPDATE EPR.BLOCKED_USERS SET FAILED_ATTEMPTS = 3");
                  return "Felaktigt lösenord, kontot är låst kontakta " +
                     "administratören";
               }
               dbcon.dbQueryUpdate (
                     "UPDATE EPR.BLOCKED_USERS SET FAILED_ATTEMPTS = " +
                     (attempts + 1) + " WHERE USER_ID = " + user);
            }
            else  // First failed attempt
            {
               dbcon.dbQueryUpdate ("INSERT INTO EPR.BLOCKED_USERS (USER_ID, " +
                     "FAILED_ATTEMPTS) VALUES ( " + user + ", 1)");
            }
         } catch (SQLException e) {
             lg.addLog(new Log(e.getMessage(),
                    "AmbulanceRecord/login",
                    "Blocked users"));
         }
         return "Felaktigt lösenord";
      }

      logBt.setIcon(new ImageIcon("images/Lock24.gif"));
      logBt.setToolTipText("Logga ut");
      logBt.setActionCommand("logout");

      loggedOn = true;
      userId = user;
      driverId = driver;
      ambulanceId = ambulance;
      stationId = station;
      this.userName = userName;
      this.driverName = driverName;

//      fetchBt.setEnabled(true);
      openBt.setEnabled(true);
      newBt.setEnabled(true);
      sendBt.setEnabled(true);
      printBt.setEnabled(true);
      signBt.setEnabled(true);

      recordTModel.updateTable();
      if (recordTModel.getRowCount() != 0)
         recordTable.setRowSelectionInterval(recordTable.getRowCount()-1,
              recordTable.getRowCount()-1);

      recordTable.requestFocus();

      // Remove user from blocked list
      try {
         dbcon.dbQueryUpdate (
               "DELETE FROM EPR.BLOCKED_USERS WHERE USER_ID = " + user);
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "AmbulanceRecord/login",
                  "Delete from blocked users"));
      }

      // Save settings
      try {
         dbcon.dbQueryUpdate (
               "UPDATE EPR.SETTINGS SET USER_ID = " + user +
               ", DRIVER_ID = " + driver + ", AMBULANCE_ID = " + ambulance +
               ", STATION_ID = " + station);
      } catch(SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "AmbulanceRecord/login",
                  "Update settings"));
      }

      return null;
   }

   /**
     * Method used to log out the user.
     */
   public void logOut()
   {
      loggedOn = false;
      userId = 0;
      driverId = 0;
      ambulanceId = 0;
      stationId = 0;
      medLevel = 0;
      userName = null;
      driverName = null;

      logBt.setIcon(new ImageIcon("images/Key24.gif"));
      logBt.setToolTipText("Logga in");
      logBt.setActionCommand("login");

//      fetchBt.setEnabled(false);
      openBt.setEnabled(false);
      newBt.setEnabled(false);
      sendBt.setEnabled(false);
      printBt.setEnabled(false);
      signBt.setEnabled(false);

      PatientRecord pr;
      for (int i=0 ; i < patientRecords.size() ; i++)
      {
         pr = (PatientRecord) patientRecords.elementAt(i);
         pr.close();
      }
      patientRecords.removeAllElements();

      recordTModel.updateTable(); 
   }

   /**
     * Method that converts a byte array into hex and retuns the result in
     * a string.
     * @param b The array with bytes to convert to hexadecimal form.
     * @return A String with with the array in hexadecimal format.
     */
   private String hexStringFromBytes(byte[] b) { 
        String hex = ""; 
        int msb; 
        int lsb = 0; 
        int i; 
        
        // MSB maps to idx 0 
        for (i = 0; i < b.length; i++) { 
            msb = ((int)b[i] & 0x000000FF) / 16;
            lsb = ((int)b[i] & 0x000000FF) % 16; 
            hex = hex + hexChars[msb] + hexChars[lsb]; 
        } 
        return(hex); 
   }

   /**
     * Method called to synchronize the client with ther server.
     */
   public void synchronize()
   {
      if (sd == null)
         sd = new SynchronizeDialog(this, dbcon, lg);
      else
         sd.startSynchronization();
   }

   /**
     * Method that returns the server address.
     * return The server address.
     */
   public String getServerAddress()
   {
      return SERVERADDR;
   }

   /**
     * Method that returns the port number that the server PNR service is
     * listening to.
     * @return The server PNR service port number.
     */
   public int getServerPNRPort()
   {
      return SERVERPNRPORT;
   }

   /**
     * Method that enables the login/logout button
     */
   public void enableLogBt()
   {
       logBt.setEnabled(true);
   }

   /**
     * Method that starts the ambulance record application.
     * @param args Array with command promt parameters (not used).
     */
   public static void main(String args[])
   {
      // Create a log handler
      try {
         lg = new LogHandler("logg.txt");
      } catch (Exception e) {
         System.err.println("LogHandler exp " + e.getMessage());
      }
      lg.addLog(new Log("Ambulance Client"));

      RegisterKey rk = null;

      // Read the registry
      try {
         rk = new RegisterKey();
      } catch (Exception e) {
         JOptionPane.showMessageDialog(null,
               "Kan inte ladda inställningar från registret");
         System.exit(1);
      }
      // Connect to the database
      try { 
         dbcon = new DB2Connect(rk.getDBPath(), lg, false);
         dbcon.connect(rk.getDBUser(), rk.getDBPass());
      } catch (NullPointerException e) {
         JOptionPane.showMessageDialog(null, e.getMessage());
         System.exit(1);
      }
      wl = new WordList(dbcon, lg);
      AmbulanceRecord ar = new AmbulanceRecord();
      ar.show();
      LoginDialog ld = new LoginDialog(ar, dbcon, lg);
      ar.enableLogBt();

      ar.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               dbcon.disconnect();
               System.exit(0);
            }
      }); 
      ar.checkCerts();
   }
}
