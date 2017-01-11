import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

/**
 * Class implementing the GUI for importing information from the mobitex
 * system.
 *
 * @version 20030904
 * @author Oskar Nilsson
 */
public class ImportInfoDialog extends JDialog implements ImportDialogInterface
{

   private RecordInformationPane parent;
   private DB2Connect dbcon;
   private LogHandler lg;
   private Parse mobitexParser;

   private int selectedId = 0;

   private JList list;
   private Vector listData;

   private JLabel commissionDataLbl;
   private JLabel alarmDataLbl;
   private JLabel addressDataLbl;
   private JLabel zoneDataLbl;
   private JLabel comment1DataLbl;
   private JLabel comment2DataLbl;
   private JLabel personidDataLbl;
   private JLabel toAddressDataLbl;
   private JLabel fNameDataLbl;
   private JLabel lNameDataLbl;
   private JLabel homeAddressDataLbl;
   private JLabel homeCountyDataLbl;
   private JLabel relativeDataLbl;
   private JLabel anamnesisDataLbl;
   
   private JButton removeBt;
   private JButton importBt;

   /**
    * Constructor, builds the GUI
    * @param pr The parent frame.
    * @param parent The panel in the patient record to import information into.
    * @param dbcon A reference to the database.
    * @param lg The loghandler to report errors to.
    * @param mobitexParser Object that reads new messages from Mobitex
    */
   public ImportInfoDialog(PatientRecord pr, RecordInformationPane parent,
         DB2Connect dbcon, LogHandler lg, Parse mobitexParser)
   {
      super (pr, "Importera larm information");
      setSize (500, /*328*/ 440);
      setLocation(pr.getX()+50, pr.getY()+50);

      this.parent = parent;
      this.dbcon = dbcon;
      this.lg = lg;
      this.mobitexParser = mobitexParser;

      Font dFont = new Font("Default", Font.PLAIN, 12);

      ResultSet rs;
      listData = new Vector ();

      ImportInfoDialog_Listener listener = new ImportInfoDialog_Listener (this);

      //--- Create the list with time entrys
      try {
         rs = dbcon.dbQuery ("SELECT ID, COMMISSION_NUMBER FROM " +
               "EPR.MOBITEX_INFORMATION ORDER BY COMMISSION_NUMBER");
         while (rs.next ())
            listData.add (new ListEntry (rs.getString(2), rs.getInt(1)));
         rs.close ();
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage (),
                  "ImportInfoDialog/ImportInfoDialog",
                  "Reading information data from the database"));
      }

      list = new JList (listData);
      list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener (
            new ImportInfoDialog_SelectionListener (this));

      JScrollPane listScrollPane = new JScrollPane (list);
      listScrollPane.setBounds (0, 0, 100, /*300*/410);

      //--- Info panel
      JPanel infoPanel = new JPanel ();
      infoPanel.setLayout (null);
      infoPanel.setBounds (100, 0, 400, /*300*/410);

      JLabel commissionLbl = new JLabel ("Ärendenummer");
      commissionLbl.setBounds (10, 15, 110, 20);
      commissionDataLbl = new JLabel ();
      commissionDataLbl.setBounds (110, 15, 120, 20);
      commissionDataLbl.setFont (dFont);

      JLabel alarmLbl = new JLabel ("Ht-förklaring");
      alarmLbl.setBounds (10, 40, 80, 20);
      alarmDataLbl = new JLabel ();
      alarmDataLbl.setBounds (90, 40, 300, 20);
      alarmDataLbl.setFont (dFont);

      JLabel addressLbl = new JLabel ("Adress");
      addressLbl.setBounds (10, 65, 80, 20);
      addressDataLbl = new JLabel ();
      addressDataLbl.setBounds (90, 65, 300, 20);
      addressDataLbl.setFont (dFont);

      JLabel zoneLbl = new JLabel ("Zon");
      zoneLbl.setBounds (10, 90, 80, 20);
      zoneDataLbl = new JLabel ();
      zoneDataLbl.setBounds (90, 90, 300, 20);
      zoneDataLbl.setFont (dFont);

      JLabel toAddressLbl = new JLabel("Till adress");
      toAddressLbl.setBounds(10, 115, 80, 20);
      toAddressDataLbl = new JLabel();
      toAddressDataLbl.setBounds(90, 115, 300, 20);
      toAddressDataLbl.setFont(dFont);
      
      JLabel comment1Lbl = new JLabel ("Kommentarer");
      comment1Lbl.setBounds (10, 140, 80, 20);
      comment1DataLbl = new JLabel ();
      comment1DataLbl.setBounds (90, 140, 300, 20);
      comment1DataLbl.setFont (dFont);
      comment2DataLbl = new JLabel ();
      comment2DataLbl.setBounds (90, 165, 300, 20);
      comment2DataLbl.setFont (dFont);

      JLabel personidLbl = new JLabel ("Personnumer");
      personidLbl.setBounds (10, 190, 80, 20);
      personidDataLbl = new JLabel ();
      personidDataLbl.setBounds (90, 190, 100, 20);
      personidDataLbl.setFont (dFont);

      JLabel fNameLbl = new JLabel("Förnamn");
      fNameLbl.setBounds(10, 215, 80, 20);
      fNameDataLbl = new JLabel();
      fNameDataLbl.setBounds(90, 215, 100, 20);
      fNameDataLbl.setFont(dFont);
      
      JLabel lNameLbl = new JLabel("Efternamn");
      lNameLbl.setBounds(10, 240, 80, 20);
      lNameDataLbl = new JLabel();
      lNameDataLbl.setBounds(90, 240, 100, 20);
      lNameDataLbl.setFont(dFont);

      JLabel homeAddressLbl = new JLabel("Hemadress");
      homeAddressLbl.setBounds(10, 265, 80, 20);
      homeAddressDataLbl = new JLabel();
      homeAddressDataLbl.setBounds(90, 265, 300, 20);
      homeAddressDataLbl.setFont(dFont);

      JLabel homeCountyLbl = new JLabel("HemLandsting");
      homeCountyLbl.setBounds(10, 290, 80, 20);  
      homeCountyDataLbl = new JLabel();
      homeCountyDataLbl.setBounds(90, 290, 50, 20);
      homeCountyDataLbl.setFont(dFont);

      JLabel relativeLbl = new JLabel("Anhörig");
      relativeLbl.setBounds(10, 315, 80, 20);
      relativeDataLbl = new JLabel();
      relativeDataLbl.setBounds(90, 315, 300, 20);
      relativeDataLbl.setFont(dFont);

      JLabel anamnesisLbl = new JLabel("Besvär");
      anamnesisLbl.setBounds(10, 340, 80, 20);
      anamnesisDataLbl = new JLabel();
      anamnesisDataLbl.setBounds(90, 340, 300, 20);
      anamnesisDataLbl.setFont(dFont);
      
      removeBt = new JButton ("Ta bort");
      removeBt.setBounds (180, 380, 100, 25);
      removeBt.setActionCommand ("remove");
      removeBt.addActionListener (listener);
      removeBt.setEnabled (false);

      importBt = new JButton ("Importera");
      importBt.setBounds (290, 380, 100, 25);
      importBt.setActionCommand ("import");
      importBt.addActionListener (listener);
      importBt.setEnabled (false);

      infoPanel.add (commissionLbl);
      infoPanel.add (commissionDataLbl);
      infoPanel.add (alarmLbl);
      infoPanel.add (alarmDataLbl);
      infoPanel.add (addressLbl);
      infoPanel.add (addressDataLbl);
      infoPanel.add (zoneLbl);
      infoPanel.add (zoneDataLbl);
      infoPanel.add (toAddressLbl);
      infoPanel.add (toAddressDataLbl);
      infoPanel.add (comment1Lbl);
      infoPanel.add (comment1DataLbl);
      infoPanel.add (comment2DataLbl);
      infoPanel.add (personidLbl);
      infoPanel.add (personidDataLbl);
      infoPanel.add (fNameLbl);
      infoPanel.add (fNameDataLbl);
      infoPanel.add (lNameLbl);
      infoPanel.add (lNameDataLbl);
      infoPanel.add (homeAddressLbl);
      infoPanel.add (homeAddressDataLbl);
      infoPanel.add (homeCountyLbl);
      infoPanel.add (homeCountyDataLbl);
      infoPanel.add (relativeLbl);
      infoPanel.add (relativeDataLbl);
      infoPanel.add (anamnesisLbl);
      infoPanel.add (anamnesisDataLbl);
      
      infoPanel.add (removeBt);
      infoPanel.add (importBt);
      
      //--- Main frame
      getContentPane().setLayout (null);
      getContentPane().add (listScrollPane);
      getContentPane().add (infoPanel);
 
      addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               enableImport ();
               dispose ();
            }
      }); 

      show ();

      mobitexParser.importMessage (this);

   }

   /**
     * Method called by the window listener when the frame is closed to
     * reenable the import button in the Patient record information pane.
     */
   public void enableImport ()
   {
      parent.enableImportInfo();
   }

   /**
     * Method called when selection changes in the table.
     */
   public void listSelectionChanged ()
   {
      ResultSet rs;
      
      try {
         selectedId = ((ListEntry)list.getSelectedValue ()).getNumber ();
      } catch (NullPointerException e) {
         return;
      }
      
      removeBt.setEnabled (true);
      importBt.setEnabled (true);

      try {
         rs = dbcon.dbQuery (
              "SELECT * FROM EPR.MOBITEX_INFORMATION WHERE ID = " +
              selectedId);
         rs.next ();
         commissionDataLbl.setText (rs.getString (2));
         alarmDataLbl.setText (rs.getString (3));
         addressDataLbl.setText (rs.getString (5));
         zoneDataLbl.setText (rs.getString (6));
         comment1DataLbl.setText (rs.getString (19));
         comment2DataLbl.setText (rs.getString (20));
         personidDataLbl.setText (rs.getString (10));
         toAddressDataLbl.setText(rs.getString (7));
         fNameDataLbl.setText(rs.getString (8));
         lNameDataLbl.setText(rs.getString (9));
         homeAddressDataLbl.setText(rs.getString (15));
         homeCountyDataLbl.setText(rs.getString (16));
         relativeDataLbl.setText(rs.getString (18));
         anamnesisDataLbl.setText(rs.getString(17));
         rs.close ();
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage(),
                  "ImportInfoDialog/listSelectionChanged",
                  "Reading time entrys from db"));
      }
  }

   /**
     * Method that removes the selected list entry from the database.
     */
   public void removeEntry ()
   {
      int res = JOptionPane.showConfirmDialog(this,
            "Informationen kommer att tas bort permanent, vill du fortsätta?",
            "Ta bort information", 
            JOptionPane.YES_NO_OPTION);
      if (res == JOptionPane.NO_OPTION)
         return;

      try {
         selectedId = ((ListEntry)list.getSelectedValue ()).getNumber ();
      } catch (NullPointerException e) {
         return;
      }

      try {
         dbcon.dbQueryUpdate (
               "DELETE FROM EPR.MOBITEX_INFORMATION WHERE ID = " + selectedId);
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage (),
                  "ImportInfoDialog/removeEntry",
                  "Removing an information entry"));
      }

      removeBt.setEnabled (false);
      importBt.setEnabled (false);

      commissionDataLbl.setText ("");
      alarmDataLbl.setText ("");
      addressDataLbl.setText ("");
      zoneDataLbl.setText ("");
      comment1DataLbl.setText ("");
      comment2DataLbl.setText ("");
      personidDataLbl.setText ("");
      toAddressDataLbl.setText("");
      fNameDataLbl.setText("");
      lNameDataLbl.setText("");
      homeAddressDataLbl.setText("");
      homeCountyDataLbl.setText("");
      relativeDataLbl.setText("");
      anamnesisDataLbl.setText("");

      listData.remove (list.getSelectedValue ());
      list.setListData (listData);
   }

   /**
     * Method the imports the selected info into the patient record.
     */
   public void importInfo ()
   {
      parent.importInfo (selectedId);
      enableImport ();
      dispose ();
   }

   /**
     * Method called by Parse after an import.
     * @param state True if a new message was received, false if the mobitex
     * device was empty.
     */
   public void returnState (boolean state)
   {
      if (state)
         mobitexParser.importMessage (this);
      else
      {
         ResultSet rs;
 
         listData.clear ();

         //--- Create the list with time entrys
         try {
            rs = dbcon.dbQuery ("SELECT ID, COMMISSION_NUMBER FROM " +
                  "EPR.MOBITEX_INFORMATION ORDER BY COMMISSION_NUMBER");
            while (rs.next ())
               listData.add (new ListEntry (rs.getString(2), rs.getInt(1)));
            rs.close ();
         } catch (SQLException e) {
            lg.addLog (new Log (e.getMessage (),
                     "ImportInfoDialog/ImportInfoDialog",
                     "Reading information data from the database"));
         }

         list.setListData (listData);
      }

  }

}
