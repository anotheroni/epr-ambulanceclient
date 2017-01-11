import javax.swing.*;
import javax.swing.text.*;
import java.text.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
  * Class implementing the Record Information Pane.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class RecordInformationPane extends JPanel
{
   private PatientRecord pr;
   private AmbulanceRecord ar;
   private DB2Connect dbcon;
   private LogHandler lg;
   private Parse mobitexParser;

   private int recordId;
   private boolean isEditable;

   private boolean patientInfoExists;  // Flag set when reading p info from db
 
   private JSpinner dateSpin;

   private JButton importInfoBt;
   private JButton importTimeBt;

   private JComboBox prioInCBox;
   private JComboBox prioOutCBox;
  
   private JSpinner distanceSpin;
  
   private JTextField alarmCauseFld;

   private JComboBox alarmCauseCBox;
   private Vector alarmCauseVec;
   private int alarmId;
   
   private JTextField pickupFld;
   private JTextField dropFld;

   private JComboBox inCityCBox;
   private int inCityId;
   private Vector inCityVec;
   private JComboBox inZoneCBox;
   private int inZoneId;
   private Vector inZoneVec;
   private JComboBox inPlaceCBox;
   private int inPlaceId;
   private Vector inPlaceVec;
   private JComboBox outCityCBox;
   private int outCityId;
   private Vector outCityVec;
   private JComboBox outZoneCBox;
   private int outZoneId;
   private Vector outZoneVec;
   private JComboBox outPlaceCBox;
   private int outPlaceId;
   private Vector outPlaceVec;

   private String oldalTmStr;   
   private JTextField alTmFld; 
   private JTextField avTmFld;
   private JTextField aaTmFld;
   private JTextField laTmFld;
   private JTextField ahTmFld;
   private JTextField enTmFld;

   private JButton fetchInfoBt;
   private JButton stopFetchBt;
   private AmbulanceClientPatient ambCliPat = null;
   private JTextField pnrFld;
   private JTextField fNameFld;
   private JTextField sNameFld;
   private JTextField addrFld;
   private JTextField relativeFld;
   
   /**
     * Construtor, build the GUI and reads data from the database.
     * @param pr A reference to the patient record.
     * @param ar A reference to the main class.
     * @param dbcon A reference to the database.
     * @param wl A reference to the worldlist used by auto complete.
     * @param dFont The font to use in the panel.
     * @param recordId The id number of the patient record.
     * @param isEditable True if the record is editable.
     */
   public RecordInformationPane(PatientRecord pr, AmbulanceRecord ar,
        DB2Connect dbcon, WordList wl, LogHandler lg, Font dFont, int recordId,
        boolean isEditable)
   {
      super();
      this.pr = pr;
      this.ar = ar;
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.isEditable = isEditable;

      mobitexParser = new Parse (dbcon, lg);

      ResultSet rs;

      RecordInformationPane_Listener listener = 
         new RecordInformationPane_Listener(this, pr, dbcon, isEditable);

      //--- Date
      JLabel dateLbl = new JLabel("Datum");
      dateLbl.setBounds (10, 30, 60, 25);
      dateLbl.setFont(dFont);

      SpinnerDateModel dateModel = new SpinnerDateModel();
      dateSpin = new JSpinner(dateModel);
      dateSpin.setBounds (70, 30, 90, 30);
      setJSpinnerFormat(dateSpin, "yyyy-MM-dd");
      dateSpin.setEnabled(isEditable);

      //--- Import from MOBITEX
      importInfoBt = new JButton ("Info");
      importInfoBt.setToolTipText ("Importera larm information från mobitex");
      importInfoBt.setMargin (new Insets (1, 1, 1, 1));
      importInfoBt.setBounds (15, 20, 60, 25);
      importInfoBt.setActionCommand ("importInfo");
      importInfoBt.addActionListener (listener);
      importInfoBt.setEnabled (isEditable);

      importTimeBt = new JButton ("Tider");
      importTimeBt.setToolTipText ("Importera tider från mobitex");
      importTimeBt.setMargin (new Insets (1, 1, 1, 1));
      importTimeBt.setBounds (90, 20, 60, 25);
      importTimeBt.setActionCommand ("importTime");
      importTimeBt.addActionListener (listener);
      importTimeBt.setEnabled (isEditable);

      JPanel importPanel = new JPanel ();
      importPanel.setBounds (200, 20, 160, 50);
      importPanel.setBorder(BorderFactory.createTitledBorder(
               "Importera från Mobitex"));
      importPanel.setLayout (null);

      importPanel.add (importInfoBt);
      importPanel.add (importTimeBt);

      //--- Priorities
      JLabel prioOutLbl = new JLabel("Priotet ut");
      prioOutLbl.setBounds (155, 75, 60, 25);
      prioOutLbl.setFont(dFont);

      JLabel prioInLbl = new JLabel("Priotet in");
      prioInLbl.setBounds (265, 75, 60, 25);
      prioInLbl.setFont(dFont);

      Object[] values = {"-", "1", "2", "3"};

      prioOutCBox = new JComboBox(values);
      prioOutCBox.setBounds (215, 75, 35, 25);
      prioOutCBox.setEnabled(isEditable);

      prioInCBox = new JComboBox(values);
      prioInCBox.setBounds (325, 75, 35, 25);
      prioInCBox.setEnabled(isEditable);

      //--- Distance
      JLabel distanceLbl = new JLabel("Körda km");
      distanceLbl.setBounds (10, 75, 60, 25);
      distanceLbl.setFont(dFont);

      distanceSpin = new JSpinner(new SpinnerNumberModel(0,0,9000,1));
      distanceSpin.setBounds (70, 75, 60, 25);
      distanceSpin.setEnabled(isEditable);

      //--- Text
      alarmCauseFld = new JTextField();   // More in AlarmCause Panel
      alarmCauseFld.setEnabled(isEditable);
      AutoComplete_Listener alarmCauseFldListener =
         new AutoComplete_Listener(alarmCauseFld, wl, pr.getUserId());
      alarmCauseFld.addKeyListener(alarmCauseFldListener);     

      pickupFld = new JTextField(); // More in Pickup Panel
      pickupFld.setEnabled(isEditable);
      AutoComplete_Listener pickupFldListener =
         new AutoComplete_Listener(pickupFld, wl, pr.getUserId());
      pickupFld.addKeyListener(pickupFldListener);     

      dropFld = new JTextField();   // More in Dropoff Panel
      dropFld.setEnabled(isEditable);
      AutoComplete_Listener dropFldListener =
         new AutoComplete_Listener(dropFld, wl, pr.getUserId());
      dropFld.addKeyListener(dropFldListener);     

      //--- Times
      Change_Listener timeListener = new Change_Listener(pr, "timeChanged");

      JPanel timePanel = new JPanel();
      timePanel.setLayout(null);
      timePanel.setBounds(390, 75, 195, 205);
      timePanel.setBorder(BorderFactory.createTitledBorder("Tider"));
 
      JLabel alTmLbl = new JLabel("Larm");
      alTmLbl.setBounds (10,20,140,25);
      alTmLbl.setFont(dFont);
 
      alTmFld = new JTextField ();
      ((AbstractDocument)alTmFld.getDocument()).
         setDocumentFilter(new LimitedTimeFilter(8, alTmFld));
      alTmFld.setBounds (125, 20, 60, 25);
      alTmFld.setEnabled (isEditable);

      JLabel avTmLbl = new JLabel("Avfärd");
      avTmLbl.setBounds (10, 50, 140, 25);
      avTmLbl.setFont(dFont);

      avTmFld = new JTextField ();
      ((AbstractDocument)avTmFld.getDocument ()).
         setDocumentFilter (new LimitedTimeFilter (8, avTmFld));
      avTmFld.setBounds (125, 50, 60, 25);
      avTmFld.setEnabled (isEditable);

      JLabel aaTmLbl = new JLabel("Ankomst hämtplats");
      aaTmLbl.setBounds(10, 80, 140, 25);
      aaTmLbl.setFont(dFont);

      aaTmFld = new JTextField ();
      ((AbstractDocument)aaTmFld.getDocument ()).
         setDocumentFilter (new LimitedTimeFilter (8, aaTmFld));
      aaTmFld.setBounds (125, 80, 60, 25);
      aaTmFld.setEnabled (isEditable);

      JLabel laTmLbl = new JLabel("Avfärd hämtplats");
      laTmLbl.setBounds(10, 110, 140, 25);
      laTmLbl.setFont(dFont);

      laTmFld = new JTextField ();
      ((AbstractDocument)laTmFld.getDocument ()).
         setDocumentFilter (new LimitedTimeFilter (8, laTmFld));
      laTmFld.setBounds (125, 110, 60, 25);
      laTmFld.setEnabled (isEditable);
 
      JLabel ahTmLbl = new JLabel("Ankomst avlämning");
      ahTmLbl.setBounds(10, 140, 140, 25);
      ahTmLbl.setFont(dFont);

      ahTmFld = new JTextField ();
      ((AbstractDocument)ahTmFld.getDocument ()).
         setDocumentFilter (new LimitedTimeFilter (8, ahTmFld));
      ahTmFld.setBounds (125, 140, 60, 25);
      ahTmFld.setEnabled (isEditable);

      JLabel enTmLbl = new JLabel("Avslut");
      enTmLbl.setBounds(10, 170, 140, 25);
      enTmLbl.setFont(dFont);

      enTmFld = new JTextField ();
      ((AbstractDocument)enTmFld.getDocument ()).
         setDocumentFilter (new LimitedTimeFilter (8, enTmFld));
      enTmFld.setBounds (125, 170, 60, 25);
      enTmFld.setEnabled (isEditable);

      timePanel.add (alTmLbl);
      timePanel.add (alTmFld);
      timePanel.add (avTmLbl);
      timePanel.add (avTmFld);
      timePanel.add (aaTmLbl);
      timePanel.add (aaTmFld);
      timePanel.add (laTmLbl);
      timePanel.add (laTmFld);
      timePanel.add (ahTmLbl);
      timePanel.add (ahTmFld);
      timePanel.add (enTmLbl);
      timePanel.add (enTmFld);

      //- Read from database
      try {
         rs = dbcon.dbQuery("SELECT DATE, ALARM_TIME, LEFT_STATION_TIME, " +
              "ARRIVAL_ACCIDENT_TIME, LEFT_ACCIDENT_TIME, " + 
              "HAND_OVER_TIME, MISSION_CLOSED_TIME, " +
              "PRIORITY_OUT, PRIORITY_IN, DRIVED_KM, " +
              "ALARM_CAUSE, ALARM_CAUSE_ID, ACCIDENT_SCENE, HOSPITAL_NAME, " +
              "ACCIDENT_CITY, ACCIDENT_ZONE, ACCIDENT_PLACE, DROPOFF_CITY, " +
              "DROPOFF_ZONE, DROPOFF_PLACE " +
              "FROM EPR.AMBULANCE_RECORD WHERE " +
              "RECORD_ID = " + recordId);
         rs.next();
         dateSpin.setValue(rs.getDate(1));   // DATE
         alTmFld.setText (rs.getString (2));   // ALARM_TIME
         // Save the time to restore to if the new time is incorrect
         oldalTmStr = rs.getString (2);
         try {
            avTmFld.setText (rs.getString (3));   // LEFT_STATION_TIME
         } catch (IllegalArgumentException f) { }
         try {
            aaTmFld.setText (rs.getString (4));   // ARRIVAL_ACCIDENT_TIME
         } catch (IllegalArgumentException f) { }
         try {
            laTmFld.setText (rs.getString (5));   // LEFT_ACCIDENT_TIME
         } catch (IllegalArgumentException f) { }
         try {
            ahTmFld.setText (rs.getString (6)); // HAND_OVER_TIME
         } catch (IllegalArgumentException f) { }
         try {
            enTmFld.setText (rs.getString (7)); // MISSION_CLOSED_TIME
         } catch (IllegalArgumentException f) { }
         try {
            prioInCBox.setSelectedIndex(rs.getInt(9));   // PRIORITY_IN
         } catch (IllegalArgumentException f) {}
         try {
            prioOutCBox.setSelectedIndex(rs.getInt(8));  // PRIORITY_OUT
         } catch (IllegalArgumentException f) {}
         try {
            distanceSpin.setValue(new Float(rs.getFloat(10)));  // DRIVED_KM
         } catch (IllegalArgumentException f) {}
         
         alarmCauseFld.setText(rs.getString(11));  // ALARM_CAUSE
         pickupFld.setText(rs.getString(13));   // ACCIDENT_SCENE
         dropFld.setText(rs.getString(14));  // HOSPITAL_NAME

         alarmId = rs.getInt(12);   // ALARM_CAUSE_ID
         inCityId = rs.getInt(15);  // ACCIDENT_CITY
         inZoneId = rs.getInt(16);  // ACCIDENT_ZONE
         inPlaceId = rs.getInt(17); // ACCIDENT_PLACE
         outCityId = rs.getInt(18); // DROPOFF_CITY
         outZoneId = rs.getInt(19); // DROPOFF_ZONE
         outPlaceId = rs.getInt(20);   // DROPOFF_PLACE

         rs.close();
 
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading record data"));
      }

      // Listeners added after setValue to avoid an event
      alTmFld.setActionCommand ("timefld");
      alTmFld.addActionListener (listener);
      avTmFld.setActionCommand ("timefld");
      avTmFld.addActionListener (listener);
      aaTmFld.setActionCommand ("timefld");
      aaTmFld.addActionListener (listener);
      laTmFld.setActionCommand ("timefld");
      laTmFld.addActionListener (listener);
      ahTmFld.setActionCommand ("timefld");
      ahTmFld.addActionListener (listener);
      enTmFld.setActionCommand ("timefld");
      enTmFld.addActionListener (listener);
      
      //dateSpin.addChangeListener(timeListener);

      //- Alarm Cause DB call
      try {
         rs = dbcon.dbQuery("SELECT ALARM_ID, ALARM_NAME " +
               "FROM EPR.ALARM_CAUSE WHERE DISABLE = 0 ORDER BY ALARM_NAME");
         alarmCauseVec = new Vector();
         alarmCauseVec.add(new ListEntry("",0));
         ListEntry tmp, defCa = null;
         while (rs.next()) 
         {
            tmp = new ListEntry(rs.getString(2),   // ALARM_NAME
                  rs.getInt(1)); // ALARM_ID
            alarmCauseVec.add(tmp);
            if (tmp.getNumber() == alarmId)
               defCa = tmp;
         }
         rs.close();

         alarmCauseCBox = new JComboBox(alarmCauseVec);
         alarmCauseCBox.setBounds(10,20,180,25);
         if (defCa != null)
            alarmCauseCBox.setSelectedItem(defCa);
         alarmCauseCBox.setEnabled(isEditable);

      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading alarm causes"));
      }

      //--- Alarm cause panel
      JPanel alarmCausePanel = new JPanel();
      alarmCausePanel.setLayout(null);
      alarmCausePanel.setBorder(BorderFactory.createTitledBorder("Larmorsak"));
      alarmCausePanel.setBounds(5,280,580,80);

      alarmCauseFld.setBounds(10,50,560,25);
      ((AbstractDocument)alarmCauseFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(64));

      alarmCausePanel.add(alarmCauseFld);
      alarmCausePanel.add(alarmCauseCBox);

      //- Zone in
      try {
         rs = dbcon.dbQuery(
               "SELECT ZONE_ID, ZONE_NAME " +
               "FROM EPR.ZONE WHERE DISABLE = 0 AND CITY_ID = " +
               inCityId + " ORDER BY ZONE_NAME");
         inZoneVec = new Vector();
         inZoneVec.add(new ListEntry("",0));
         ListEntry tmp, inDef = null;
         while (rs.next())
         {
            tmp = new ListEntry(rs.getString(2),   // ZONE_NAME
                  rs.getInt(1)); // ZONE_ID
            inZoneVec.add(tmp);
            if (tmp.getNumber() == inZoneId)
               inDef = tmp;
         }
         rs.close();
 
         inZoneCBox = new JComboBox(inZoneVec);
         inZoneCBox.setBounds(200,20,180,25);
         if (inDef != null)
            inZoneCBox.setSelectedItem(inDef);
         inZoneCBox.setEnabled(isEditable);

      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading in zones"));
      }

      //- Zone out
      try {
          rs = dbcon.dbQuery(
               "SELECT ZONE_ID, ZONE_NAME " +
               "FROM EPR.ZONE WHERE DISABLE = 0 AND CITY_ID = " +
               outCityId + " ORDER BY ZONE_NAME");
         outZoneVec = new Vector();
         outZoneVec.add(new ListEntry("",0));
         ListEntry tmp, outDef = null;
         while (rs.next())
         {
            tmp = new ListEntry(rs.getString(2),   // ZONE_NAME
                  rs.getInt(1)); // ZONE_ID
            outZoneVec.add(tmp);
            if (tmp.getNumber() == outZoneId)
               outDef = tmp;
         }
         rs.close();

         outZoneCBox = new JComboBox(outZoneVec);
         outZoneCBox.setBounds(200,20,180,25);
         if (outDef != null)
            outZoneCBox.setSelectedItem(outDef);
         outZoneCBox.setEnabled(isEditable);

      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading out zones"));
      }

      //- City
      try {
         rs = dbcon.dbQuery("SELECT CITY_ID, CITY_NAME FROM " +
               "EPR.CITY WHERE DISABLE = 0 ORDER BY CITY_NAME");
         inCityVec = new Vector();
         inCityVec.add(new ListEntry("",0));
         outCityVec = new Vector();
         outCityVec.add(new ListEntry("",0));

         ListEntry tmp, inDef = null, outDef = null;
         while(rs.next())
         {
            tmp = new ListEntry(rs.getString(2),   // CITY_NAME
                  rs.getInt(1)); // CITY_ID
            inCityVec.add(tmp);
            outCityVec.add(tmp);
            if (tmp.getNumber() == inCityId)
               inDef = tmp;
            if (tmp.getNumber() == outCityId)
               outDef = tmp;
         }
         rs.close();

         inCityCBox = new JComboBox(inCityVec);
         inCityCBox.setBounds(10,20,180,25);
         if (inDef != null)
            inCityCBox.setSelectedItem(inDef);
         inCityCBox.addActionListener(listener);
         inCityCBox.setActionCommand("inCityChange");
         inCityCBox.setEnabled(isEditable);

         outCityCBox = new JComboBox(outCityVec);
         outCityCBox.setBounds(10,20,180,25);
         if (outDef != null)
            outCityCBox.setSelectedItem(outDef);
         outCityCBox.addActionListener(listener);
         outCityCBox.setActionCommand("outCityChange");
         outCityCBox.setEnabled(isEditable);
            
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading cities"));
      }

      //- Place
      try {
         rs = dbcon.dbQuery("SELECT PLACE_ID, PLACE_NAME " +
               "FROM EPR.PLACE WHERE DISABLE = 0 ORDER BY PLACE_NAME");
         inPlaceVec = new Vector();
         inPlaceVec.add(new ListEntry("",0));
         outPlaceVec = new Vector();
         outPlaceVec.add(new ListEntry("",0));

         ListEntry tmp, inDef = null, outDef = null;
         while (rs.next())
         {
            tmp = new ListEntry(rs.getString(2),   // PLACE_NAME
                  rs.getInt(1)); // PLACE_ID
            inPlaceVec.add(tmp);
            outPlaceVec.add(tmp);
            if (tmp.getNumber() == inPlaceId)
               inDef = tmp;
            if (tmp.getNumber() == outPlaceId)
               outDef = tmp;
         }
         rs.close();

         inPlaceCBox = new JComboBox(inPlaceVec);
         inPlaceCBox.setBounds(390, 20, 180, 25);
         if (inDef != null)
            inPlaceCBox.setSelectedItem(inDef);
         inPlaceCBox.setEnabled(isEditable);

         outPlaceCBox = new JComboBox(outPlaceVec);
         outPlaceCBox.setBounds(390, 20, 180, 25);
         if (outDef != null)
            outPlaceCBox.setSelectedItem(outDef);
         outPlaceCBox.setEnabled(isEditable);

      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading places"));
      }

      //--- Pickup panel
      JPanel pickupPanel = new JPanel();
      pickupPanel.setLayout(null);
      pickupPanel.setBorder(BorderFactory.createTitledBorder("Hämtplats"));
      pickupPanel.setBounds(5,360,580,80);

      pickupFld.setBounds(10,50,560,25);
      ((AbstractDocument)pickupFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(64));

      pickupPanel.add(pickupFld);
      pickupPanel.add(inCityCBox);
      pickupPanel.add(inZoneCBox);
      pickupPanel.add(inPlaceCBox);

      //--- Dropoff panel
      JPanel dropoffPanel = new JPanel();
      dropoffPanel.setLayout(null);
      dropoffPanel.setBorder(
            BorderFactory.createTitledBorder("Avlämningsplats"));
      dropoffPanel.setBounds(5,440,580,80);

      dropFld.setBounds(10,50,560,25);
      ((AbstractDocument)dropFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(64));

      dropoffPanel.add(dropFld);
      dropoffPanel.add(outCityCBox);
      dropoffPanel.add(outZoneCBox);
      dropoffPanel.add(outPlaceCBox);

      //--- Patient
      JPanel patientPanel = new JPanel();
      patientPanel.setLayout(null);
      patientPanel.setBounds (5, 110, 375, 170);
      patientPanel.setBorder(
            BorderFactory.createTitledBorder("Patient information"));

      fetchInfoBt = new JButton(new ImageIcon("images/Refresh24.gif"));
      fetchInfoBt.setToolTipText("Hämta namn från PNR");
      fetchInfoBt.setBounds(215,15,26,26);
      fetchInfoBt.setMargin(new Insets(1,1,1,1));
      fetchInfoBt.addActionListener(listener);
      fetchInfoBt.setActionCommand("fetchInfo");
      fetchInfoBt.setEnabled(isEditable);

      stopFetchBt = new JButton(new ImageIcon("images/Stop24.gif"));
      stopFetchBt.setToolTipText("Avbryt kontakten med PNR");
      stopFetchBt.setBounds(250,15,26,26);
      stopFetchBt.setMargin(new Insets(1,1,1,1));
      stopFetchBt.addActionListener(listener);
      stopFetchBt.setActionCommand("stopFetch");
      stopFetchBt.setVisible(false);
      
      JLabel pnrLbl = new JLabel("Personnummer");
      pnrLbl.setFont(dFont);
      pnrLbl.setBounds(10,15,90,25);
      pnrFld = new JTextField();
      pnrFld.setBounds(110,15,95,25);
      pnrFld.addActionListener(listener);
      pnrFld.setActionCommand("patient");
      pnrFld.setEnabled(isEditable);

      ((AbstractDocument)pnrFld.getDocument()).
         setDocumentFilter(new LimitedNumberFilter(12));

      JLabel fNameLbl = new JLabel("Förnamn");
      fNameLbl.setFont(dFont);
      fNameLbl.setBounds(10,45,60,25);
      fNameFld = new JTextField();
      fNameFld.setBounds(80,45,280,25);
      fNameFld.addActionListener(listener);
      fNameFld.setActionCommand("patient");
      fNameFld.setEnabled(isEditable);
      ((AbstractDocument)fNameFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(32));

      JLabel sNameLbl = new JLabel("Efternamn");
      sNameLbl.setFont(dFont);
      sNameLbl.setBounds(10,75,60,25);
      sNameFld = new JTextField();
      sNameFld.setBounds(80,75,280,25);
      sNameFld.addActionListener(listener);
      sNameFld.setActionCommand("patient");
      sNameFld.setEnabled(isEditable);
      ((AbstractDocument)sNameFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(32));

      JLabel addrLbl = new JLabel("Adress");
      addrLbl.setFont(dFont);
      addrLbl.setBounds(10,105,60,25);
      addrFld = new JTextField();
      addrFld.setBounds(80,105,280,25);
      addrFld.addActionListener(listener);
      addrFld.setActionCommand("patient");
      addrFld.setEnabled(isEditable);
      ((AbstractDocument)addrFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(64));

      JLabel relativeLbl = new JLabel("Anhörig");
      relativeLbl.setFont(dFont);
      relativeLbl.setBounds(10,135,60,25);
      relativeFld = new JTextField();
      relativeFld.setBounds(80,135,280,25);
      relativeFld.addActionListener(listener);
      relativeFld.setActionCommand("patient");
      relativeFld.setEnabled(isEditable);
      ((AbstractDocument)relativeFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(64));

      patientPanel.add(fetchInfoBt);
      patientPanel.add(stopFetchBt);
      patientPanel.add(pnrLbl);
      patientPanel.add(pnrFld);
      patientPanel.add(fNameLbl);
      patientPanel.add(fNameFld);
      patientPanel.add(sNameLbl);
      patientPanel.add(sNameFld);
      patientPanel.add(addrLbl);
      patientPanel.add(addrFld);
      patientPanel.add(relativeLbl);
      patientPanel.add(relativeFld);

      //- Read patient values from the database
      try {
         rs = dbcon.dbQuery("SELECT PERSON_ID, FIRST_NAME, LAST_NAME, " +
               "ADDRESS, RELATIVE_INFORMATION FROM EPR.PATIENT WHERE " +
               "RECORD_ID = " + recordId);
         patientInfoExists = rs.next();
         if (patientInfoExists)
         {
            pnrFld.setText(rs.getString(1));    // PERSON_ID
            fNameFld.setText(rs.getString(2));  // FIRST_NAME
            sNameFld.setText(rs.getString(3));  // LAST_NAME
            addrFld.setText(rs.getString(4));   // ADDRESS
            relativeFld.setText(rs.getString(5));  // RELATIVE_INFORMATION
         }
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/RecordInformationPane",
                  "Reading patient info"));
      }
      if (pnrFld.getText().length() <= 0)
         pnrFld.setText("19");
 
      //--- Main
      setLayout(null);
      add(dateLbl);
      add(importPanel);
      add(dateSpin);
      add(prioInLbl);
      add(prioInCBox);
      add(prioOutLbl);
      add(prioOutCBox);
      add(distanceLbl);
      add(distanceSpin);
      add(alarmCausePanel);
      add(pickupPanel);
      add(dropoffPanel);
      add(timePanel);
      add(patientPanel);
   }

   /**
     * Method that saves the panel state in the database.
     */
   public void savePane()
   {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

      if (!isEditable)  // No reason to save if the pane isn't editable
         return;
 
      StringBuffer qStr = new StringBuffer(250);

      qStr.append("UPDATE EPR.AMBULANCE_RECORD SET DATE = '").
         append(dateFormat.format(dateSpin.getValue())).
         append("'");

      if (alTmFld.getText().length() > 4)
      {
         qStr.append(",ALARM_TIME='").append(alTmFld.getText ());
          if (alTmFld.getText().length() == 7)
         {
            qStr.append ("0");
            try {
               alTmFld.getDocument().insertString(7, "0", null);
            } catch (BadLocationException e) { }
         }
         else if (alTmFld.getText().length() == 6)
         {
            qStr.append ("00");
            try {
               alTmFld.getDocument().insertString(6, "00", null);
            } catch (BadLocationException e) { }
         }
         qStr.append("'");
         oldalTmStr = alTmFld.getText ();
      }
      else
      {
         pr.setMessage(
               "En larmtid måste finnas, den gammla tiden kommer att behållas");
         alTmFld.setText (oldalTmStr);
      }

      if (avTmFld.getText().length() > 4)
      {
         qStr.append(",LEFT_STATION_TIME='").append(avTmFld.getText ());
         if (avTmFld.getText().length() == 7)
         {
            qStr.append ("0");
            try {
               avTmFld.getDocument().insertString(7, "0", null);
            } catch (BadLocationException e) { }
         }
         else if (avTmFld.getText().length() == 6)
         {
            qStr.append ("00");
            try {
               avTmFld.getDocument().insertString(6, "00", null);
            } catch (BadLocationException e) { }
         }
         qStr.append("'");
      }
      else
      {
         qStr.append(",LEFT_STATION_TIME=null");
         avTmFld.setText ("");
      }

      if (aaTmFld.getText().length() > 4)
      {
         qStr.append(",ARRIVAL_ACCIDENT_TIME='").append(aaTmFld.getText ());
         if (aaTmFld.getText().length() == 7)
         {
            qStr.append ("0");
            try {
               aaTmFld.getDocument().insertString(7, "0", null);
            } catch (BadLocationException e) { }
         }
         else if (aaTmFld.getText().length() == 6)
         {
            qStr.append ("00");
            try {
               aaTmFld.getDocument().insertString(6, "00", null);
            } catch (BadLocationException e) { }
         }
         qStr.append("'");
      }
      else
      {
         qStr.append(",ARRIVAL_ACCIDENT_TIME=null");
         aaTmFld.setText ("");
      }

      if (laTmFld.getText().length() > 4)
      {
         qStr.append(",LEFT_ACCIDENT_TIME='").append(laTmFld.getText());
         if (laTmFld.getText().length() == 7)
         {
            qStr.append ("0");
            try {
               laTmFld.getDocument().insertString (7, "0", null);
            } catch (BadLocationException e) { }
         }
         else if (laTmFld.getText().length() == 6)
         {
            qStr.append ("00");
            try {
               laTmFld.getDocument().insertString (6, "00", null);
            } catch (BadLocationException e) { }
         }
         qStr.append("'");
      }
      else
      {
         qStr.append(",LEFT_ACCIDENT_TIME=null");
         laTmFld.setText ("");
      }

      if (ahTmFld.getText().length() > 4)
      {
         qStr.append(",HAND_OVER_TIME='").append(ahTmFld.getText ());
         if (ahTmFld.getText().length() == 7)
         {
            qStr.append ("0");
            try {
               ahTmFld.getDocument().insertString (7, "0", null);
            } catch (BadLocationException e) { }
         }
         else if (ahTmFld.getText().length() == 6)
         {
            qStr.append ("00");
            try {
               ahTmFld.getDocument().insertString (6, "00", null);
            } catch (BadLocationException e) { }
         }
         qStr.append("'");
      }
      else
      {
         qStr.append(",HAND_OVER_TIME=null");
         ahTmFld.setText ("");
      }
      if (enTmFld.getText().length() > 4)
      {
         qStr.append(",MISSION_CLOSED_TIME='").append(enTmFld.getText ());
         if (enTmFld.getText().length() == 7)
         {
            qStr.append ("0");
            try {
               enTmFld.getDocument().insertString (7, "0", null);
            } catch (BadLocationException e) { }
         }
         else if (enTmFld.getText().length() == 6)
         {
            qStr.append ("00");
            try {
               enTmFld.getDocument().insertString (6, "00", null);
            } catch (BadLocationException e) { }
         }
         qStr.append("'");
      }
      else
      {
         qStr.append(",MISSION_CLOSED_TIME=null");
         enTmFld.setText ("");
      }

      qStr.append(" WHERE RECORD_ID = ").append(recordId);
      try {
         dbcon.dbQueryUpdate (qStr.toString());
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/savePane",
                  "Update ambulance record"));
      }

      int tmpVal;
      qStr = new StringBuffer(250);
     
      qStr.append("UPDATE EPR.AMBULANCE_RECORD SET ");
      
      if (prioInCBox.getSelectedIndex() != 0)
         qStr.append("PRIORITY_IN = ").append(prioInCBox.getSelectedIndex()).
           append(", ");
      
      if (prioOutCBox.getSelectedIndex() != 0)
         qStr.append("PRIORITY_OUT = ").append(prioOutCBox.getSelectedIndex()).
           append(", ");

      if (alarmCauseCBox != null && (tmpVal = 
               ((ListEntry) alarmCauseCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(" ALARM_CAUSE_ID = ").append(tmpVal).append(", ");

      qStr.append("DRIVED_KM = ").append(distanceSpin.getValue()).
         append(", ALARM_CAUSE = '").append(alarmCauseFld.getText()).
         append("', ACCIDENT_SCENE = '").append(pickupFld.getText()).
         append("', HOSPITAL_NAME = '").append(dropFld.getText()).
         append("'");

      if (inCityCBox != null && (tmpVal =
               ((ListEntry) inCityCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(", ACCIDENT_CITY = ").append(tmpVal);

      if (outCityCBox != null && (tmpVal =
               ((ListEntry) outCityCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(", DROPOFF_CITY = ").append(tmpVal);

      if (inZoneCBox != null && (tmpVal =
            ((ListEntry) inZoneCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(", ACCIDENT_ZONE = ").append(tmpVal);
      
      if (outZoneCBox != null && (tmpVal =
            ((ListEntry) outZoneCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(", DROPOFF_ZONE = ").append(tmpVal);
      
      if (inPlaceCBox != null && (tmpVal =
            ((ListEntry) inPlaceCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(", ACCIDENT_PLACE = ").append(tmpVal);
      
      if (outPlaceCBox != null && (tmpVal = 
            ((ListEntry) outPlaceCBox.getSelectedItem()).getNumber()) != 0)
         qStr.append(", DROPOFF_PLACE = ").append(tmpVal);


      qStr.append(" WHERE RECORD_ID = ").append(recordId);
      
      try {
         dbcon.dbQueryUpdate (qStr.toString());
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/savePane",
                  "Update ambulance record"));
      }

      try {
         if (patientInfoExists)
         {
            dbcon.dbQueryUpdate ("UPDATE EPR.PATIENT SET " +
                  "PERSON_ID = '" + pnrFld.getText() + "', FIRST_NAME = '" +
                  fNameFld.getText() + "', LAST_NAME = '" + sNameFld.getText()
                  + "', ADDRESS = '" + addrFld.getText() +
                  "', RELATIVE_INFORMATION = '" + relativeFld.getText() +
                  "' WHERE RECORD_ID = " + recordId);
         }
         else
         {
            // Don't save pnr if nothing is entered (19 is the default value)
            String pnr;
            if (pnrFld.getText().equals("19"))
               pnr = "";
            else
               pnr = pnrFld.getText();
            
            dbcon.dbQueryUpdate (
                  "INSERT INTO EPR.PATIENT (RECORD_ID, PERSON_ID," +
                  " FIRST_NAME, LAST_NAME, ADDRESS, RELATIVE_INFORMATION)" + 
                  " VALUES (" + recordId + ", '" + pnr +
                  "', '" + fNameFld.getText() + "', '" +
                  sNameFld.getText() + "', '" + addrFld.getText() + "', '" +
                  relativeFld.getText() + "')");
            patientInfoExists = true;
         }
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/savePane",
                  "Patient info"));
      }
   }

   /**
     * Method that sets the date output format for a JSpinner.
     * @param spinner A reference to the spinner that should be configured.
     * @param format The date format to set. Use SimpleDateFormat notation.
     */
   private void setJSpinnerFormat(JSpinner spinner, String format)
   {
      JFormattedTextField tf =
         ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
      DefaultFormatterFactory factory =
         (DefaultFormatterFactory)tf.getFormatterFactory();
      DateFormatter formatter = (DateFormatter)factory.getDefaultFormatter();
      formatter.setFormat(new SimpleDateFormat(format));
   }
   
   /**
     * Method used by the change listener to set the time changed flag
     * NOT USED!
     * Called by timeChanged in PatientRecord.
     */
   public void timeChanged()
   {
   }

   /**
     * Methos called by the action listener when the in city combo box changes.
     * Changes the contents of the in zone combobox.
     */
   public void inCityChange()
   {
      ListEntry curItm = (ListEntry) inCityCBox.getSelectedItem();

      if (curItm.getNumber() == inCityId)
         return;

      ResultSet rs;

      try {
         rs = dbcon.dbQuery(
               "SELECT ZONE_ID, ZONE_NAME FROM EPR.ZONE WHERE CITY_ID = " +
               curItm.getNumber() + " AND DISABLE = 0");
         
         inZoneCBox.removeAllItems();
         inZoneCBox.addItem(new ListEntry("", 0));

         while (rs.next())
            inZoneCBox.addItem(new ListEntry(rs.getString(2),  // ZONE_NAME
                     rs.getInt(1)));   // ZONE_ID
         rs.close();
         inCityId = curItm.getNumber();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/inCityChange",
                  "Read zone"));
      }
   }

   /**
     * Methos called by the action listener when the out city combo box changes.
     * Changes the contents of the out zone combo box.
     */
   public void outCityChange()
   {
      ListEntry curItm = (ListEntry) outCityCBox.getSelectedItem();

      if (curItm.getNumber() == outCityId)
         return;

      ResultSet rs;

      try {
         rs = dbcon.dbQuery(
               "SELECT ZONE_ID, ZONE_NAME FROM EPR.ZONE WHERE CITY_ID = " +
               curItm.getNumber() + " AND DISABLE = 0");
         
         outZoneCBox.removeAllItems();
         outZoneCBox.addItem(new ListEntry("", 0));

         while (rs.next())
            outZoneCBox.addItem(new ListEntry(rs.getString(2), // ZONE_NAME
                     rs.getInt(1)));   // ZONE_ID
         rs.close();
         outCityId = curItm.getNumber();
      } catch (SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "RecordInformationPane/outCityChange",
                  "Read zone"));
      }
   }

   /**
     * Function that fetches information about the patient from PNR.
     * The contents of the personnumer field is used.
     */
   public void fetchInfo()
   {
      if (pnrFld.getText().length() == 12)
      {
         stopFetchBt.setVisible(true);
         fetchInfoBt.setEnabled(false);
         try {
            ambCliPat = new AmbulanceClientPatient(lg, pnrFld.getText(), this);
         } catch (java.util.prefs.BackingStoreException e) {
            pr.setMessage("Kan ej läsa inställningar från registret");
         }
      }
      else
         pr.setMessage(
               "Ett personnumer måste anges för att kunna kontakta PNR");
    }

   /**
     * Method that stops connection atempts to the server.
     */
   public void stopFetch()
   {
      if (ambCliPat != null)
      {
         ambCliPat.terminate();
         pr.setMessage("Anslutning till PNR har avbrutits");
         stopFetchBt.setVisible(false);
         fetchInfoBt.setEnabled(true);
      }
   }

   /**
     * Method used by AmbulanceClientPatient to return a succesfull result.
     * @param api The patient informaion.
     */
   public void setPatientInformation(AmbulancePatientInformation api)
   {
      fetchInfoBt.setEnabled(true);
      stopFetchBt.setVisible(false);

      pr.setMessage(api.getMessage());

      if (!api.informationFailed())
      {
         fNameFld.setText(api.getFirstName());
         sNameFld.setText(api.getLastName());
      
          // TODO check string length
         addrFld.setText(api.getAddress() + " " + api.getZipCode() + " " +
           api.getCity());
      }
   }

   /**
     * Method that creates an importInfoDialog to import alarm information
     * from Mobitex. Called by the action listener when the import info button
     * is pressed.
     */
   public void showImportInfo ()
   {
      importInfoBt.setEnabled (false);
      new ImportInfoDialog (pr, this, dbcon, lg, mobitexParser);
   }

   /**
    * Method that creates an importTimesDialog to import time information from
    * Mobitex. Called by the action listener when the import time button is
    * pressed.
    */
   public void showImportTime ()
   {
      importTimeBt.setEnabled (false);
      new ImportTimeDialog (pr, this, dbcon, lg, mobitexParser);
   }

   /**
     * Method used by AmbulanceClientPatient to return status informaion.
     */
   public void setMessage(String message)
   {
      pr.setMessage(message);
   }

   /**
     * Method that imports time information from mobitex into the patient
     * record.
     * @param id The record id in the database for the time information to
     * import.
     */
   public void importTimes (int id)
   {
      ResultSet rs;

      try {
         rs = dbcon.dbQuery ("SELECT * FROM EPR.MOBITEX_TIME WHERE ID = " + id);
         rs.next ();
         if (rs.getDate (3) != null)
            dateSpin.setValue (rs.getDate (3));
         if (rs.getTime (4) != null)
            alTmFld.setText (rs.getString (4));
         if (rs.getTime (7) != null)
            aaTmFld.setText (rs.getString (7));
         if (rs.getTime (8) != null)
            laTmFld.setText (rs.getString (8));
         if (rs.getTime (10) != null)
            ahTmFld.setText (rs.getString (10));
         if (rs.getTime (11) != null)
            enTmFld.setText (rs.getString (11));
         if (rs.getInt (6) != 0)
            prioOutCBox.setSelectedIndex (rs.getInt (6));
         if (rs.getInt (9) != 0)
            prioInCBox.setSelectedIndex (rs.getInt (9));
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage(),
                  "RecordInformationPane/importTimes",
                  "Reading time entrys from db"));
      }
   }

   /**
     * Method that imports alarm information from mobitex into the patient
     * record.
     * @param id The record id in the database for the alarm information to
     * import.
     */
   public void importInfo (int id)
   {
      ResultSet rs;

      try {
         rs = dbcon.dbQuery (
               "SELECT * FROM EPR.MOBITEX_INFORMATION WHERE ID = " + id);
         rs.next ();
         alarmCauseFld.setText (rs.getString (3));
         pickupFld.setText (rs.getString (5) + " " + rs.getString (6));
         pr.addComment (rs.getString (19));
         pr.addComment (rs.getString (20));
         pnrFld.setText (rs.getString (10));
         fNameFld.setText(rs.getString(8));
         sNameFld.setText(rs.getString(9));
         addrFld.setText(rs.getString(15));
         relativeFld.setText(rs.getString(18));
         dropFld.setText(rs.getString(7));
         pr.addDiagnosis(rs.getString(17));
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage(),
                  "RecordInformationPane/importInfo",
                  "Reading information entrys from db"));
      }
     ;
   }

   /**
     * Method that enables the info import button. Called when the import info
     * dialog closes.
     */
   public void enableImportInfo ()
   {
      importInfoBt.setEnabled (true);
   }

   /**
     * Method that enables the time import button. Called when the import time
     * dialog closes.
     */
   public void enableImportTime ()
   {
      importTimeBt.setEnabled (true);
   }

}
