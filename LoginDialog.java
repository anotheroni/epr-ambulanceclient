import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;

/**
 * Class representing the login frame.
 *
 * @author Oskar Nilsson
 * @version 20030416
 */
public class LoginDialog extends JDialog implements KeyListener
{

   private JComboBox stationCBox;
   private JComboBox ambulanceCBox;
   private JComboBox driverCBox;
   private JComboBox userCBox;
   private JPasswordField passwdFld;

   private DB2Connect dbcon;
   private LogHandler lg;
   private AmbulanceRecord ar;

   private int uid_idx, did_idx, sid_idx, aid_idx;

   /**
    * Constuctor, creates the login dialog frame.
    * @param parent A reference to the AmbulanceRecord object.
    */
   public LoginDialog(AmbulanceRecord parent, DB2Connect dbcon, LogHandler lg)
   {
      super(parent, "Logga in", true);
      setSize(220, 260);
      setResizable(false);
      setLocation(parent.getX()+50, parent.getY()+50);
      ar = parent;

      this.dbcon = dbcon;
      this.lg = lg;      
      LoginDialog_Listener listener = new LoginDialog_Listener(parent, this);

      ResultSet rs = null;
      int uid = 0, did = 0, sid = 0, aid = 0;
      boolean settings = false;

      // Read settings from DB
      try {
         rs = dbcon.dbQuery("SELECT USER_ID, DRIVER_ID, STATION_ID, " +
               "AMBULANCE_ID FROM EPR.SETTINGS");
         settings = rs.next();
         if (settings)
         {
            uid = rs.getInt(1);  // USER_ID
            did = rs.getInt(2);  // DRIVER_ID
            sid = rs.getInt(3);  // STATION_ID
            aid = rs.getInt(4);  // AMBULANCE_ID
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "LoginDialog/LoginDialog",
                  "Read settings"));
      }
         
      ListEntry stations[];
      stations = getStations(sid);  // Fetch stations from the database

      JLabel stationLbl = new JLabel("Station");
      stationLbl.setBounds(10, 10, 60, 25);
      stationCBox = new JComboBox(stations);
      stationCBox.setBounds(70, 10, 115, 25);
      
      if (settings)  // If a default setting exists, select it
         try {
            stationCBox.setSelectedIndex(sid_idx);
         } catch (IllegalArgumentException e) { } // Needed when the db is empty

      ListEntry ambulances[];
      ambulances = getAmbulances(aid); // Fetch ambulances from the database

      JLabel ambulanceLbl = new JLabel("Ambulans");
      ambulanceLbl.setBounds(10, 45, 60, 25);
      ambulanceCBox = new JComboBox(ambulances);
      ambulanceCBox.setBounds(70, 45,115, 25);

      if (settings)  // If a default setting exists, select it
         try {
            ambulanceCBox.setSelectedIndex(aid_idx);
         } catch (IllegalArgumentException e) { } // Needed when the db is empty

      ImageIcon swapIco = new ImageIcon("images/change.gif");
      JButton swapBt = new JButton(swapIco);
      swapBt.setBounds(190, 105, 15, 30);
      swapBt.setMargin(new Insets(0,0,0,0));
      swapBt.addActionListener(listener);
      swapBt.setActionCommand("swap");
      swapBt.setFocusable(false); 

      ListEntry users[];
      users = getUserIds(uid, did);  // Fetch users from the database

      JLabel driverLbl = new JLabel("Förare");
      driverLbl.setBounds(10, 90, 60, 25);
      driverCBox = new JComboBox(users);
      driverCBox.setBounds(70, 90, 115, 25);

      if (settings)  // If a default setting exists, select it
         try {
            driverCBox.setSelectedIndex(did_idx);
         } catch (IllegalArgumentException e) { } // Needed when the db is empty

      JLabel userLbl = new JLabel("Vårdare");
      userLbl.setBounds(10, 125, 60, 25);
      userCBox = new JComboBox(users);
      userCBox.setBounds(70, 125, 115, 25);

      if (settings)  // If a default setting exists, select it
         try {
            userCBox.setSelectedIndex(uid_idx);
         } catch (IllegalArgumentException e) { } // Needed when the db is empty

      JLabel passwdLbl = new JLabel("Lösenord");
      passwdLbl.setBounds(10, 160, 60, 25);
      passwdFld = new JPasswordField();
      passwdFld.setBounds(70, 160, 115, 25);
      passwdFld.addKeyListener(this);

      JButton loginBt = new JButton("Logga in");
      loginBt.setBounds(25, 200, 70, 25);
      loginBt.setMargin(new Insets(2,4,2,4));
      loginBt.addActionListener(listener);
      loginBt.setActionCommand("login");
      loginBt.addKeyListener(this);

      JButton cancelBt = new JButton("Avbryt");
      cancelBt.setBounds(115, 200, 70, 25);
      cancelBt.addActionListener(listener);
      cancelBt.setActionCommand("quit");

      JPanel panel = new JPanel();
      panel.setLayout(null);
      panel.setBounds(0, 0, 200, 240);

      panel.add(stationLbl);
      panel.add(stationCBox);
      panel.add(ambulanceLbl);
      panel.add(ambulanceCBox);
      panel.add(driverLbl); 
      panel.add(driverCBox);
      panel.add(userLbl);
      panel.add(userCBox);
      panel.add(swapBt);
      panel.add(passwdLbl);
      panel.add(passwdFld);
      panel.add(loginBt);
      panel.add(cancelBt);
      getContentPane().add(panel);

      passwdFld.requestFocus();

      show();
  }

   /**
    * Method to get all active users from the database.
    * @param uid The user id in the settings.
    * @param did The driver id in the settings.
    * @return An array containing userName/userId pairs.
    */
   private ListEntry[] getUserIds(int uid, int did)
   {
      ListEntry result[];
      ResultSet rs = null;
      int numUsers = 0;
      int id;

      try {
         rs = dbcon.dbQuery("SELECT COUNT(*) FROM EPR.STAFF WHERE "+
               "DISABLE = 0");
         if (rs.next())
            numUsers = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT USER_NAME, STAFF_ID FROM EPR.STAFF " +
               "WHERE DISABLE = 0 ORDER BY USER_NAME");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "LuxationImage/getUserIds",
                  "Read staff"));
         return new ListEntry[0];
      }
      result = new ListEntry[numUsers];
      for (int i=0 ; i < numUsers ; i++)
      {
         try {
            rs.next();
            id = rs.getInt(2);   // STAFF_ID
            result[i] = new ListEntry(rs.getString(1), id); // USER_NAME
            if (id == uid)
               uid_idx = i;
            if (id == did)
               did_idx = i;
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "LuxationImage/getUserIds",
                     "Read result set"));
            result[i] = new ListEntry("FEL", 0);
         }
      }
      try { rs.close(); } catch (SQLException e) { }
      return result;
   }

   /**
    * Method to get all active ambulances from the database.
    * @param aid The ambulance id in the settings.
    * @return An array containing ambulanceName/ambulanceId pairs.
    */ 
   private ListEntry[] getAmbulances(int aid)
   {
      ListEntry result[];
      ResultSet rs = null;
      int numAmbulances = 0;
      int id;

      try {
         rs = dbcon.dbQuery("SELECT COUNT(*) FROM EPR.AMBULANCE WHERE " +
               "DISABLE = 0");
         if (rs.next())
            numAmbulances = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT AMBULANCE_NAME, AMBULANCE_ID FROM " +
               "EPR.AMBULANCE WHERE DISABLE = 0 ORDER BY AMBULANCE_NAME");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "LuxationImage/getAmbulances",
                  "Read ambulance"));
         return new ListEntry[0];
      }

      result = new ListEntry[numAmbulances];
      for (int i=0 ; i < numAmbulances ; i++)
      {
         try {
            rs.next();
            id = rs.getInt(2);   // AMBULANCE_ID
            result[i] = new ListEntry(rs.getString(1), id); // AMBULANCE_NAME
            if (id == aid)
               aid_idx = i;
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "LuxationImage/getAmbulances",
                     "Read result set"));
            result[i] = new ListEntry("FEL", 0);
         }
      }
      try { rs.close(); } catch (SQLException e) { }
      return result;
   }

   /**
    * Method to get all active stations from the database.
    * @param sid The station id in the settings.
    * @return An array containing stationName/stationId pairs.
    */ 
   private ListEntry[] getStations(int sid)
   {
      ListEntry result[];
      ResultSet rs = null;
      int numStations = 0;
      int id;

      try {
         rs = dbcon.dbQuery("SELECT COUNT(*) FROM EPR.STATION WHERE " +
               "DISABLE = 0");
         if (rs.next())
            numStations = rs.getInt(1);
         rs.close();
         rs = dbcon.dbQuery("SELECT STATION_NAME, STATION_ID FROM " +
               "EPR.STATION WHERE DISABLE = 0 ORDER BY STATION_NAME");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "LuxationImage/getStations",
                  "Read stations"));
         return new ListEntry[0];
      }

      result = new ListEntry[numStations];
      for (int i=0 ; i < numStations ; i++)
      {
         try {
            rs.next();
            id = rs.getInt(2); // STATION_ID
            result[i] = new ListEntry(rs.getString(1), id); // STATION_NAME
            if (id == sid)
               sid_idx = i;
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "LuxationImage/getStations",
                     "Read stations"));
            result[i] = new ListEntry("FEL", 0);
         }
      }
      try { rs.close(); } catch (SQLException e) {}
      return result;
   }

   /**
    * Handle the key typed event.
    * Empty, needs to be defined due to implementing Keylistener.
    */
   public void keyTyped(KeyEvent e) {
      ;
   }

   /**
    * Handle the key pressed event.
    */
   public void keyPressed(KeyEvent e) {
      String result;
      int keyCode = e.getKeyCode();

      if (keyCode == java.awt.event.KeyEvent.VK_ENTER)
      {
         if ((result = ar.login(getUserId(), getPassword(), getDriverId(),
                     getAmbulance(), getStation(), getUserName(),
                     getDriverName())) == null)
            dispose();
         else
            JOptionPane.showMessageDialog(this, result);
      }
   }

   /**
    * Handle the key released event.
    * Empty, needs to be defined due to implementing Keylistener.
    */
   public void keyReleased(KeyEvent e) {
      ;
   }

   /**
    * Method to get the selected ambulance station id.
    * @return ID-number of the selected ambulance station.
    */
   public int getStation()
   {
      return ((ListEntry) stationCBox.getSelectedItem()).getNumber();
   }

   /**
    * Method the get the selected ambulance id.
    * @return ID-number of the selected ambulance.
    */
   public int getAmbulance()
   {
      return ((ListEntry)ambulanceCBox.getSelectedItem()).getNumber();
   }

   /**
    * Method to get the selected driver-ID.
    * @return ID-number of the selected driver.
    */
   public int getDriverId()
   {
      return ((ListEntry) driverCBox.getSelectedItem()).getNumber();
   }

   /**
     * Method to get the selected driver name.
     * @return Username of the selected driver.
     */
   public String getDriverName()
   {
      return ((ListEntry) driverCBox.getSelectedItem()).toString();
   }

   /**
    * Method to get the selected user-ID.
    * @return ID-number of the selected user.
    */
   public int getUserId()
   {
      return ((ListEntry) userCBox.getSelectedItem()).getNumber();
   }

   /**
     * Method to get the selected user name.
     * @return Username of the selected user.
     */
   public String getUserName()
   {
      return ((ListEntry) userCBox.getSelectedItem()).toString();
   }

   /**
    * Method the get the password.
    * @return The password.
    */
   public String getPassword()
   {
      return new String(passwdFld.getPassword());
   }

   /**
    * Method to swap the driver and the user.
    */
   public void swapUsers()
   {
      int tmp = userCBox.getSelectedIndex();
      userCBox.setSelectedIndex(driverCBox.getSelectedIndex());
      driverCBox.setSelectedIndex(tmp);
   }

}
