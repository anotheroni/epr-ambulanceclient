import javax.swing.*;
import java.awt.BorderLayout;
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
public class ImportTimeDialog extends JDialog implements ImportDialogInterface
{

   private RecordInformationPane parent;
   private DB2Connect dbcon;
   private LogHandler lg;
   private Parse mobitexParser;

   private int selectedId = 0;

   private JList list;
   private Vector listData;

   private JLabel dateDataLbl;
   private JLabel alarmDataLbl;
   private JLabel arrivalDataLbl;
   private JLabel loadedDataLbl;
   private JLabel handoverDataLbl;
   private JLabel closingDataLbl;
   private JLabel prioOutDataLbl;
   private JLabel prioInDataLbl;

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
   public ImportTimeDialog(PatientRecord pr, RecordInformationPane parent,
         DB2Connect dbcon, LogHandler lg, Parse mobitexParser)
   {
      super (pr, "Importera tidsinformation");
      setSize (400, 328);
      setLocation(pr.getX()+50, pr.getY()+50);

      this.parent = parent;
      this.dbcon = dbcon;
      this.lg = lg;
      this.mobitexParser = mobitexParser;

      ResultSet rs;
      listData = new Vector ();

      ImportTimeDialog_Listener listener = new ImportTimeDialog_Listener (this);

      //--- Create the list with time entrys
      try {
         rs = dbcon.dbQuery ("SELECT ID, DATE, ALARM " +
               "FROM EPR.MOBITEX_TIME ORDER BY DATE, ALARM");
         while (rs.next ())
            listData.add (new ListEntry (rs.getString(2) + " " +
                     rs.getString(3), rs.getInt(1)));
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage (),
                  "ImportTimeDialog/ImportTimeDialog",
                  "Reading timedata from the database"));
      }

      list = new JList (listData);
      list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener (
            new ImportTimeDialog_SelectionListener (this));
      
      JScrollPane listScrollPane = new JScrollPane (list);
      listScrollPane.setBounds (0, 0, 150, 300);

      //--- Info panel
      JPanel infoPanel = new JPanel ();
      infoPanel.setLayout (null);
      infoPanel.setBounds (150, 0, 250, 300);

      JLabel dateLbl = new JLabel ("Datum");
      dateLbl.setBounds (10, 15, 80, 25);
      dateDataLbl = new JLabel ();
      dateDataLbl.setBounds (80, 15, 80, 25);

      JLabel alarmLbl = new JLabel ("Uppdarag");
      alarmLbl.setBounds (10, 50, 80, 25);
      alarmDataLbl = new JLabel ();
      alarmDataLbl.setBounds (80, 50, 80, 25);

      JLabel arrivalLbl = new JLabel ("Framme");
      arrivalLbl.setBounds (10, 80, 80, 25);
      arrivalDataLbl = new JLabel ();
      arrivalDataLbl.setBounds (80, 80, 80, 25);

      JLabel loadedLbl = new JLabel ("Lastat");
      loadedLbl.setBounds (10, 110, 80, 25);
      loadedDataLbl = new JLabel ();
      loadedDataLbl.setBounds (80, 110, 80, 25);

      JLabel handoverLbl = new JLabel ("Lämnar");
      handoverLbl.setBounds (10, 140, 80, 25);
      handoverDataLbl = new JLabel ();
      handoverDataLbl.setBounds (80, 140, 80, 25);

      JLabel closingLbl = new JLabel ("Klar");
      closingLbl.setBounds (10, 170, 80, 25);
      closingDataLbl = new JLabel ();
      closingDataLbl.setBounds (80, 170, 80, 25);

      JLabel prioOutLbl = new JLabel ("Prio");
      prioOutLbl.setBounds (160, 50, 40, 25);
      prioOutDataLbl = new JLabel ();
      prioOutDataLbl.setBounds (200, 50, 50, 25);

      JLabel prioInLbl = new JLabel ("Prio");
      prioInLbl.setBounds (160, 110, 40, 25);
      prioInDataLbl = new JLabel ();
      prioInDataLbl.setBounds (200, 110, 50, 25);

      removeBt = new JButton ("Ta bort");
      removeBt.setBounds (20, 270, 100, 25);
      removeBt.setActionCommand ("remove");
      removeBt.addActionListener (listener);
      removeBt.setEnabled (false);

      importBt = new JButton ("Importera");
      importBt.setBounds (130, 270, 100, 25);
      importBt.setActionCommand ("import");
      importBt.addActionListener (listener);
      importBt.setEnabled (false);

      infoPanel.add (dateLbl);
      infoPanel.add (dateDataLbl);
      infoPanel.add (alarmLbl);
      infoPanel.add (alarmDataLbl);
      infoPanel.add (arrivalLbl);
      infoPanel.add (arrivalDataLbl);
      infoPanel.add (loadedLbl);
      infoPanel.add (loadedDataLbl);
      infoPanel.add (handoverLbl);
      infoPanel.add (handoverDataLbl);
      infoPanel.add (closingLbl);
      infoPanel.add (closingDataLbl);
      infoPanel.add (prioOutLbl);
      infoPanel.add (prioOutDataLbl);
      infoPanel.add (prioInLbl);
      infoPanel.add (prioInDataLbl);
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
      parent.enableImportTime();
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
         rs = dbcon.dbQuery ("SELECT * FROM EPR.MOBITEX_TIME WHERE ID = " +
              selectedId);
         rs.next ();
         dateDataLbl.setText (rs.getString (3));
         alarmDataLbl.setText (rs.getString (4));
         arrivalDataLbl.setText (rs.getString (7));
         loadedDataLbl.setText (rs.getString (8));
         handoverDataLbl.setText (rs.getString (10));
         closingDataLbl.setText (rs.getString (11));
         prioOutDataLbl.setText (rs.getString (6));
         prioInDataLbl.setText (rs.getString (9));
         rs.close ();
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage(),
                  "ImportTimeDialog/listSelectionChanged",
                  "Reading time entrys from db"));
      }
  }

   /**
     * Method that removes the selected list entry from the database.
     */
   public void removeEntry ()
   {
      int res = JOptionPane.showConfirmDialog(this,
            "Tiderna kommer att tas bort permanent, vill du fortsätta?",
            "Ta bort tider", 
            JOptionPane.YES_NO_OPTION);
      if (res == JOptionPane.NO_OPTION)
         return;

      try {
         selectedId = ((ListEntry)list.getSelectedValue ()).getNumber ();
      } catch (NullPointerException e) {
         return;
      }

      try {
         dbcon.dbQueryUpdate ("DELETE FROM EPR.MOBITEX_TIME WHERE ID = " +
               selectedId);
      } catch (SQLException e) {
         lg.addLog (new Log (e.getMessage (),
                  "ImportTimeDialog/removeEntry",
                  "Removing a time entry"));
      }

      removeBt.setEnabled (false);
      importBt.setEnabled (false);

      dateDataLbl.setText ("");
      alarmDataLbl.setText ("");
      arrivalDataLbl.setText ("");
      loadedDataLbl.setText ("");
      handoverDataLbl.setText ("");
      closingDataLbl.setText ("");
      prioOutDataLbl.setText ("");
      prioInDataLbl.setText ("");

      listData.remove (list.getSelectedValue ());
      list.setListData (listData);
   }

   /**
     * Method the imports the selected times into the patient record.
     */
   public void importTimes ()
   {
      parent.importTimes (selectedId);
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
      if (state)  // More messages to import
         mobitexParser.importMessage (this);
      else  // No more messages, update the list
      {
         ResultSet rs;

         listData.clear ();

         //--- Create the list with time entrys
         try {
            rs = dbcon.dbQuery ("SELECT ID, DATE, ALARM " +
                  "FROM EPR.MOBITEX_TIME ORDER BY DATE, ALARM");
            while (rs.next ())
               listData.add (new ListEntry (rs.getString(2) + " " +
                        rs.getString(3), rs.getInt(1)));
         } catch (SQLException e) {
            lg.addLog (new Log (e.getMessage (),
                     "ImportTimeDialog/ImportTimeDialog",
                     "Reading timedata from the database"));
         }

         list.setListData (listData);
      }
   }
   
}
