import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;

/**
  * Class implementing the traffic accident statistics dialog.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class StatisticsDialog extends JDialog implements AdminDialog
{
   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;
   private boolean isEditable;

   private boolean recExists = false;

   private JRadioButton[] policeBt = new JRadioButton[3];
   private JRadioButton[] helmBt = new JRadioButton[3];
   private JRadioButton[] beltBt = new JRadioButton[3];
   private JRadioButton[] neckBt = new JRadioButton[3];
   private JRadioButton[] airBt = new JRadioButton[3];
   
   /**
     * Constructor, creates the dialog.
     * @param pr A reference to the patient record.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param isEnabled True if the dialog should be editable.
     */
   public StatisticsDialog(PatientRecord pr, DB2Connect dbcon,
         LogHandler lg, boolean isEditable)
   {
      super(pr, "Trafikolycksstatistik", true);
      setSize(300, 245);
      setResizable(false);
      
      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;
      this.isEditable = isEditable;

      Dialog_Listener listener = new Dialog_Listener(this);

      ResultSet rs;
      int policeSel = 2, helmSel = 2, beltSel = 2, neckSel = 2, airSel = 2;
      
      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.STATISTICS WHERE RECORD_ID = "
               + pr.getRecordId());
         if (rs.next())
         {
            recExists = true;
            policeSel = rs.getInt("POLICE_CONNECTED");
            helmSel = rs.getInt("HELMET");
            beltSel = rs.getInt("SEAT_BELT");
            neckSel = rs.getInt("NECK_SUPPORT");
            airSel = rs.getInt("AIRBAG");
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "StatisticsDialog/StatisticsDialog",
                  "Reading statistics"));
      }

      JLabel policeLbl = new JLabel("Polis inkoppl.");
      policeLbl.setBounds(10,10,80,25);

      policeBt[0] = new JRadioButton("Ja");
      policeBt[0].setBounds(110,10,50,25);
      policeBt[0].setEnabled(isEditable);
      policeBt[1]  = new JRadioButton("Nej");
      policeBt[1].setBounds(160,10,50,25);
      policeBt[1].setEnabled(isEditable);
      policeBt[2] = new JRadioButton("Okänt");
      policeBt[2].setBounds(210,10,100,25);
      policeBt[2].setEnabled(isEditable);
      policeBt[policeSel].setSelected(true);

      ButtonGroup policeBtGr = new ButtonGroup();
      policeBtGr.add(policeBt[0]);
      policeBtGr.add(policeBt[1]);
      policeBtGr.add(policeBt[2]);

      JLabel helmLbl = new JLabel("Hjälm");
      helmLbl.setBounds(10,45,80,25);

      helmBt[0] = new JRadioButton("Ja");
      helmBt[0].setBounds(110,45,50,25);
      helmBt[0].setEnabled(isEditable);
      helmBt[1]  = new JRadioButton("Nej");
      helmBt[1].setBounds(160,45,50,25);
      helmBt[1].setEnabled(isEditable);
      helmBt[2] = new JRadioButton("Okänt");
      helmBt[2].setBounds(210,45,100,25);
      helmBt[2].setEnabled(isEditable);
      helmBt[helmSel].setSelected(true);

      ButtonGroup helmBtGr = new ButtonGroup();
      helmBtGr.add(helmBt[0]);
      helmBtGr.add(helmBt[1]);
      helmBtGr.add(helmBt[2]);

      JLabel beltLbl = new JLabel("Bilbälte");
      beltLbl.setBounds(10,80,80,25);

      beltBt[0] = new JRadioButton("Ja");
      beltBt[0].setBounds(110,80,50,25);
      beltBt[0].setEnabled(isEditable);
      beltBt[1]  = new JRadioButton("Nej");
      beltBt[1].setBounds(160,80,50,25);
      beltBt[1].setEnabled(isEditable);
      beltBt[2] = new JRadioButton("Okänt");
      beltBt[2].setBounds(210,80,100,25);
      beltBt[2].setEnabled(isEditable);
      beltBt[beltSel].setSelected(true);

      ButtonGroup beltBtGr = new ButtonGroup();
      beltBtGr.add(beltBt[0]);
      beltBtGr.add(beltBt[1]);
      beltBtGr.add(beltBt[2]);

      JLabel neckLbl = new JLabel("Nackstöd");
      neckLbl.setBounds(10,115,80,25);

      neckBt[0] = new JRadioButton("Ja");
      neckBt[0].setBounds(110,115,50,25);
      neckBt[0].setEnabled(isEditable);
      neckBt[1]  = new JRadioButton("Nej");
      neckBt[1].setBounds(160,115,50,25);
      neckBt[1].setEnabled(isEditable);
      neckBt[2] = new JRadioButton("Okänt");
      neckBt[2].setBounds(210,115,100,25);
      neckBt[2].setEnabled(isEditable);
      neckBt[neckSel].setSelected(true);

      ButtonGroup neckBtGr = new ButtonGroup();
      neckBtGr.add(neckBt[0]);
      neckBtGr.add(neckBt[1]);
      neckBtGr.add(neckBt[2]);
 
      JLabel airLbl = new JLabel("Airbag utlöst");
      airLbl.setBounds(10,150,80,25);

      airBt[0] = new JRadioButton("Ja");
      airBt[0].setBounds(110,150,50,25);
      airBt[0].setEnabled(isEditable);
      airBt[1]  = new JRadioButton("Nej");
      airBt[1].setBounds(160,150,50,25);
      airBt[1].setEnabled(isEditable);
      airBt[2] = new JRadioButton("Okänt");
      airBt[2].setBounds(210,150,100,25);
      airBt[2].setEnabled(isEditable);
      airBt[airSel].setSelected(true);

      ButtonGroup airBtGr = new ButtonGroup();
      airBtGr.add(airBt[0]);
      airBtGr.add(airBt[1]);
      airBtGr.add(airBt[2]);
      
      JButton okBt = new JButton("OK");
      okBt.setBounds(75, 190, 70, 25);
      okBt.setMargin(new Insets(2,4,2,4));
      okBt.addActionListener(listener);
      okBt.setActionCommand("ok");

      JButton cancelBt = new JButton("Avbryt");
      cancelBt.setBounds(155, 190, 70, 25);
      cancelBt.addActionListener(listener);
      cancelBt.setActionCommand("quit");

      JPanel panel = new JPanel();
      panel.setLayout(null);
      panel.setBounds(0, 0, 300, 225);
      panel.add(policeLbl);
      panel.add(policeBt[0]);
      panel.add(policeBt[1]);
      panel.add(policeBt[2]);
      panel.add(helmLbl);
      panel.add(helmBt[0]);
      panel.add(helmBt[1]);
      panel.add(helmBt[2]);
      panel.add(beltLbl);
      panel.add(beltBt[0]);
      panel.add(beltBt[1]);
      panel.add(beltBt[2]);
      panel.add(neckLbl);
      panel.add(neckBt[0]);
      panel.add(neckBt[1]);
      panel.add(neckBt[2]);
      panel.add(airLbl);
      panel.add(airBt[0]);
      panel.add(airBt[1]);
      panel.add(airBt[2]);
      panel.add(okBt);
      panel.add(cancelBt);

      getContentPane().add(panel);
 
      show();
   }

   /**
     * Method called by the action listener when ok is pressed.
     */
   public void okPressed()
   {
      int i, policeSel, helmSel, beltSel, neckSel, airSel;
      
      policeSel = (policeBt[0].isSelected()?0:(policeBt[1].isSelected()?1:2));
      helmSel = (helmBt[0].isSelected()?0:(helmBt[1].isSelected()?1:2));
      beltSel = (beltBt[0].isSelected()?0:(beltBt[1].isSelected()?1:2));
      neckSel = (neckBt[0].isSelected()?0:(neckBt[1].isSelected()?1:2));
      airSel = (airBt[0].isSelected()?0:(airBt[1].isSelected()?1:2));
      
      try {
         if (recExists)
            dbcon.dbQueryUpdate (
                  "UPDATE EPR.STATISTICS SET POLICE_CONNECTED = " +
                  policeSel + ", HELMET = " + helmSel + ", SEAT_BELT = " +
                  beltSel + ", NECK_SUPPORT = " + neckSel + ", AIRBAG = " +
                  airSel + " WHERE RECORD_ID = " + pr.getRecordId());
         else
            dbcon.dbQueryUpdate (
                  "INSERT INTO EPR.STATISTICS (RECORD_ID, " +
                  "POLICE_CONNECTED, HELMET, SEAT_BELT, NECK_SUPPORT, " +
                  "AIRBAG) VALUES (" + pr.getRecordId() + ", " +
                  policeSel + ", " + helmSel + ", " + beltSel + ", " +
                  neckSel + ", " + airSel + ")");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "StatisticsDialog/okPressed",
                  "Update/Insert statistics " + recExists));
      }

      this.dispose();
   }

   /**
     * Method called by the action listener when cancel is pressed.
     */
   public void cancelPressed()
   {
      this.dispose();
   }


}
