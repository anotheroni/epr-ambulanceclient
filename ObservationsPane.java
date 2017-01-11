import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.PopupFactory.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
  * Class implementing the observations pane in a patient record.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class ObservationsPane extends JPanel
{
   private final int PANELWIDTH = 580;
   private final int HEIGHT1 = 54;
   private final int HEIGHT2 = 82;
   private final int TEXTFLDLENGTH = 20;
 
   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;
   private boolean isEditable;
   
   private Insets ins;

   private ObservationsPane_Listener listener;

   private PopupFactory popupFactory;

   private JToggleButton[] uaBt;
   private JTextField[] comentFld;
   private AutoComplete_Listener[] comentFldListener;
   private boolean[] withoutRemark = null;

   private boolean[] selectedAttr = null;
 
   private Vector[] btVec;
   private Vector[] idVec;

   private Popup fracturePopup;
   private FracturePanel fracturePanel = null;
   private JToggleButton fractureBt;

   private Popup luxationPopup;
   private LuxationPanel luxationPanel = null;
   private JToggleButton luxationBt;
  
   private Popup paralysisPopup;
   private ParalysisPanel paralysisPanel;
   private JToggleButton paralysisBt;
   private int paralysisRight = 0, paralysisLeft = 0;
   private boolean paralysisInDb = false;
 
   /**
     * Constructor, creates an observation pane.
     * @param pr A reference to the patient record the pane is a part of.
     * @param dbcon A reference to the database.
     * @param wl A reference to the wordlist used by auto complete.
     * @param lg Log handler used to log errors.
     * @param dFont The font to use for JLabels.
     * @param recordId The id of the patient record.
     * @param isEditable True if the patient record is editable.
     */
   public ObservationsPane(PatientRecord pr, DB2Connect dbcon, WordList wl,
         LogHandler lg, Font dFont, int recordId, boolean isEditable)
   {
      super();

      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.isEditable = isEditable;

      int currentPos = 0;
      int height;
      int numAttributes = 0;
      int xPos;
      uaBt = new JToggleButton[8];
      withoutRemark = new boolean[8];
      comentFld = new JTextField[8];
      comentFldListener = new AutoComplete_Listener[8];
      btVec = new Vector[8];
      idVec = new Vector[8];

      ResultSet rs = null;

      ins = new Insets(2,4,2,4);
      Dimension dim;
 
      int tmpID;
      JToggleButton tmpBt;

      listener = new ObservationsPane_Listener(this);
      popupFactory = PopupFactory.getSharedInstance();

      for (int i=0 ; i < 8 ; i++)
      {
         comentFld[i] = new JTextField(TEXTFLDLENGTH);
         comentFld[i].setEnabled(isEditable);
         ((AbstractDocument)comentFld[i].getDocument()).
            setDocumentFilter(new LimitedTextFilter(128));
         comentFldListener[i] =
         new AutoComplete_Listener(comentFld[i], wl, pr.getUserId());
         comentFld[i].addKeyListener(comentFldListener[i]);
     }

      // Get the examination descriptions
      try {
         for (int i=0 ; i < withoutRemark.length ; i++)
            withoutRemark[i] = false;
 
         rs = dbcon.dbQuery("SELECT WITHOUT_REMARK, EXAMINATION_TYPE_ID, " +
              "DESCRIPTION_FIELD FROM EPR.EXAMINATION_DESCRIPTION WHERE " +
              "RECORD_ID = " + recordId);
         while (rs.next())
         {
            tmpID = rs.getInt(2) - 1;  // EXAMINATION_TYPE_ID
            withoutRemark[tmpID] = (rs.getInt(1) == 1);  // WITHOUT_REMARK
            comentFld[tmpID].setText(rs.getString(3));   // DESCRIPTION_FIELD
         }
         rs.close();
      } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/ObservationsPane",
                  "Reading examination description"));
      }

      // Get the selected attributes from the database
      try {
         rs = dbcon.dbQuery("SELECT MAX(ATTRIBUTE_ID) FROM " +
               "EPR.EXAMINATION_ATTRIBUTES");
         rs.next();

         selectedAttr = new boolean[rs.getInt(1)+1];
         rs.close();
         for (int i=0 ; i < selectedAttr.length ; i++)
            selectedAttr[i] = false;

         rs = dbcon.dbQuery("SELECT EXAMINATION_ATTRIBUTE " +
              "FROM EPR.EXAMINATION WHERE RECORD_ID = " +
              recordId + " ORDER BY EXAMINATION_ATTRIBUTE");
         while (rs.next())
            selectedAttr[rs.getInt(1)] = true;  // EXAMINATION_ATTRIBUTE

         rs.close();
      } catch (SQLException e) {
             lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/ObservationsPane",
                  "Reading examination attributes"));
      }

      // Create and initialize all standard components
      for (int i=0 ; i < 8 ; i++)
      {
         btVec[i] = new Vector();
         idVec[i] = new Vector();
         uaBt[i] = new JToggleButton("UA");
         uaBt[i].setMargin(ins);
         uaBt[i].setSelected(withoutRemark[i]);
         uaBt[i].setEnabled(isEditable);
      }

      
      // Prepeare a database query
      PreparedStatement ps = null;
      try {
         ps = dbcon.prepareStatement(
            "SELECT ATTRIBUTE_NAME, ATTRIBUTE_ID FROM " +
            "EPR.EXAMINATION_ATTRIBUTES " +
            "WHERE EXAMINATION_TYPE_ID = ? AND DISABLE = 0 " +
            "ORDER BY GUI_ORDER");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/ObservationsPane",
                  "Prepare statement"));
      }

      //--- AT
      JPanel atPanel = new JPanel();
      FlowLayout atLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      atPanel.setLayout(atLayout);
      atPanel.setBorder(BorderFactory.createTitledBorder("Allmäntillstånd"));
      atPanel.add(uaBt[0]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 1);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("atbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[0].add(tmpBt);
               idVec[0].add(new Integer(tmpID));
               atPanel.add(tmpBt);
            }
            rs.close();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Reading examination attributes"));
         }
      }

      atPanel.add(comentFld[0]);
 
      dim = atLayout.preferredLayoutSize(atPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      atPanel.setBounds(4, currentPos, PANELWIDTH, height); 
      currentPos += height;

      //--- Head
      JPanel headPanel = new JPanel();
      FlowLayout headLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      headPanel.setLayout(headLayout);
      headPanel.setBorder(BorderFactory.createTitledBorder("Huvud/Halsrygg"));
      headPanel.add(uaBt[1]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 2);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("headbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[1].add(tmpBt);
               idVec[1].add(new Integer(tmpID));
               headPanel.add(tmpBt);
            }
            rs.close();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: head"));
         }
      }

      headPanel.add(comentFld[1]);
 
      dim = headLayout.preferredLayoutSize(headPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      headPanel.setBounds(4, currentPos, PANELWIDTH, height); 
      currentPos += height;
    
      //--- Heart
      JPanel heartPanel = new JPanel();
      FlowLayout heartLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      heartPanel.setLayout(heartLayout);
      heartPanel.setBorder(BorderFactory.createTitledBorder("Hjärta"));
      heartPanel.add(uaBt[2]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 3);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("heartbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[2].add(tmpBt);
               idVec[2].add(new Integer(tmpID));
               heartPanel.add(tmpBt);
            }
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: heart"));
         }
      }

      heartPanel.add(comentFld[2]);
 
      dim = heartLayout.preferredLayoutSize(heartPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      heartPanel.setBounds(4, currentPos, PANELWIDTH, height); 
      currentPos += height;

      //--- Lungs
      JPanel lungsPanel = new JPanel();
      FlowLayout lungsLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      lungsPanel.setLayout(lungsLayout);
      lungsPanel.setBorder(
            BorderFactory.createTitledBorder("Lungor/Anding/Bröstkorg"));
      lungsPanel.add(uaBt[3]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 4);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("lungsbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[3].add(tmpBt);
               idVec[3].add(new Integer(tmpID));
               lungsPanel.add(tmpBt);
            }
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: lungs"));
         }
      }

      lungsPanel.add(comentFld[3]);
 
      dim = lungsLayout.preferredLayoutSize(lungsPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      lungsPanel.setBounds(4, currentPos, PANELWIDTH, height);
      currentPos += height;
 
      //--- Abdomnen
      JPanel abdPanel = new JPanel();
      FlowLayout abdLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      abdPanel.setLayout(abdLayout);
      abdPanel.setBorder(BorderFactory.createTitledBorder("Buk"));
      abdPanel.add(uaBt[4]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 5);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("abdbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[4].add(tmpBt);
               idVec[4].add(new Integer(tmpID));
               abdPanel.add(tmpBt);
            }
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: abdomnen"));
         }
      }

      abdPanel.add(comentFld[4]);

      dim = abdLayout.preferredLayoutSize(abdPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      abdPanel.setBounds(4, currentPos, PANELWIDTH, height);
      currentPos += height;
    
      //--- Extremities
      JPanel extrPanel = new JPanel();
      FlowLayout extrLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      extrPanel.setLayout(extrLayout);
      extrPanel.setBorder(BorderFactory.createTitledBorder("Extrimiteter"));
      extrPanel.add(uaBt[5]);

      if (rs != null)
      {
         try {
            ps.setInt(1, 6);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("extrbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[5].add(tmpBt);
               idVec[5].add(new Integer(tmpID));
               extrPanel.add(tmpBt);
            }
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: extremities"));
         }
      }

      fractureBt = new JToggleButton("Fraktur");
      fractureBt.setMargin(ins);
      fractureBt.setFont(dFont);
      fractureBt.addActionListener(listener);
      fractureBt.setActionCommand("fracturebt");

      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.FRACTURE WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            for (int i=1 ; i <= 19 ; i++)
            {
               if (rs.getByte(i+1) == 116)   // 't'
               {
                  fractureBt.setSelected(true);
                  break;
               }
            }
         }
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/ObservationsPane",
                  "Read fractures"));
      }

      luxationBt = new JToggleButton("Luxation");
      luxationBt.setMargin(ins);
      luxationBt.setFont(dFont);
      luxationBt.addActionListener(listener);
      luxationBt.setActionCommand("luxationbt");

      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.LUXATION WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            for (int i=1 ; i <= 10 ; i++)
            {
               if (rs.getByte(i+1) == 116)   // 't'
               {
                  luxationBt.setSelected(true);
                  break;
               }
            }
         }
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/ObservationsPane",
                  "Read luxation"));
      }
      
      extrPanel.add(fractureBt);
      extrPanel.add(luxationBt);
      extrPanel.add(comentFld[5]);

      dim = extrLayout.preferredLayoutSize(extrPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      extrPanel.setBounds(4, currentPos, PANELWIDTH, height);
      currentPos += height;
 
      //--- Back
      JPanel backPanel = new JPanel();
      FlowLayout backLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      backPanel.setLayout(backLayout);
      backPanel.setBorder(BorderFactory.createTitledBorder("Ryggrad/Bäcken"));
      backPanel.add(uaBt[6]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 7);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("backbt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[6].add(tmpBt);
               idVec[6].add(new Integer(tmpID));
               backPanel.add(tmpBt);
            }
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: back"));
         }
      }
 
      backPanel.add(comentFld[6]);

      dim = backLayout.preferredLayoutSize(backPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      backPanel.setBounds(4, currentPos, PANELWIDTH, height);
      currentPos += height;

      //--- Neurology
      JPanel neuPanel = new JPanel();
      FlowLayout neuLayout = new FlowLayout(FlowLayout.LEFT, 5, 2);
      neuPanel.setLayout(neuLayout);
      neuPanel.setBorder(BorderFactory.createTitledBorder("Neurologi"));
      neuPanel.add(uaBt[7]);

      if (ps != null)
      {
         try {
            ps.setInt(1, 8);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(1));  // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.addActionListener(listener);
               tmpBt.setActionCommand("neubt");
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(2);   // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[7].add(tmpBt);
               idVec[7].add(new Integer(tmpID));
               neuPanel.add(tmpBt);
            }
            rs.close();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ObservationsPane/ObservationsPane",
                     "Read examination types: neurology"));
         }
      }

      try {
         rs = dbcon.dbQuery("SELECT RIGHT_SIDE, LEFT_SIDE FROM EPR.PARALYSIS " +
               "WHERE RECORD_ID = " + recordId);
         if (rs.next())
         {
            paralysisRight = rs.getInt(1);
            paralysisLeft = rs.getInt(2);
            paralysisInDb = true;
         }
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/ObservationsPane",
                  "Read examination types: paralysis"));
      }

      if (ps != null)
         try { ps.close(); } catch (SQLException e) {}

      paralysisBt = new JToggleButton("Förlamning");
      paralysisBt.setMargin(ins);
      paralysisBt.setFont(dFont);
      paralysisBt.addActionListener(listener);
      paralysisBt.setActionCommand("paralysisbt");
      if (paralysisRight > 0 || paralysisLeft > 0)
         paralysisBt.setSelected(true);
              
      neuPanel.add(paralysisBt);
      neuPanel.add(comentFld[7]);

      dim = neuLayout.preferredLayoutSize(neuPanel);
      height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
      neuPanel.setBounds(4, currentPos, PANELWIDTH, height);
      currentPos += height;
 
      //--- Main
      setLayout(null);
      add(atPanel);
      add(headPanel);
      add(heartPanel);
      add(lungsPanel);
      add(abdPanel);
      add(extrPanel);
      add(backPanel);
      add(neuPanel);
 
   }

   /**
     * Method called when tha record is closed.
     * Saves the state to the database.
     */
   public void savePane()
   {
      int id;

      if (!isEditable)  // No reason to save changes if it isn't editable
         return;
 
      // Save the state of the togglebuttons
      for (int j=0 ; j < 8 ; j++)
      { 
         for (int i=0 ; i < btVec[j].size() ; i++)
         {
            id = ((Integer)idVec[j].elementAt(i)).intValue();
            // Only save state in db if the selection has changed
            if (((JToggleButton)btVec[j].elementAt(i)).isSelected() !=
                  selectedAttr[id])
               updateExamination(id);
         }
      }

      // Save the state of the without remark butttons and coment textfield.
      for (int i=0 ; i < uaBt.length ; i++)
      {
         if (comentFld[i].getText().length() > 0)  // Text in comment field
         {
            uaBt[i].setSelected(false);//If there's text,there can't be a remark
            try {
               dbcon.dbQueryUpdate (
                     "INSERT INTO EPR.EXAMINATION_DESCRIPTION (" +
                     "RECORD_ID, EXAMINATION_TYPE_ID, WITHOUT_REMARK, " +
                     "DESCRIPTION_FIELD) " +
                     "VALUES (" + recordId + ", " + (i+1) + ", " + 0 + ", '" +
                     comentFld[i].getText() + "')");
            } catch (SQLException e) {
               try {
                  dbcon.dbQueryUpdate (
                        "UPDATE EPR.EXAMINATION_DESCRIPTION SET " +
                        "WITHOUT_REMARK = 0, DESCRIPTION_FIELD = '" +
                        comentFld[i].getText() +
                        "' WHERE RECORD_ID = " + recordId +
                        " AND EXAMINATION_TYPE_ID = " + (i+1));
               } catch (SQLException f) {
                  lg.addLog(new Log(e.getMessage(),
                           "ObservationsPane/savePane",
                           "Insert/Update examination description"));
               }
            }
         }
         else if (uaBt[i].isSelected())   // No remark selected
         {
            withoutRemark[i] = !withoutRemark[i];
            try {
               dbcon.dbQueryUpdate (
                     "INSERT INTO EPR.EXAMINATION_DESCRIPTION (" +
                     "RECORD_ID, EXAMINATION_TYPE_ID, WITHOUT_REMARK, " +
                     "DESCRIPTION_FIELD) " +
                     "VALUES (" + recordId + ", " + (i+1) + ", 1 , '')");
            } catch (SQLException e) {
               try {
                  dbcon.dbQueryUpdate (
                        "UPDATE EPR.EXAMINATION_DESCRIPTION SET " +
                        "WITHOUT_REMARK = 1, DESCRIPTION_FIELD = ''" +
                        " WHERE RECORD_ID = " + recordId +
                        " AND EXAMINATION_TYPE_ID = " + (i+1));
               } catch (SQLException f) {
                  lg.addLog(new Log(e.getMessage(),
                           "ObservationsPane/savePane",
                           "Insert/Update examination description no remark"));
               }
            }
         }
         else  // No text and no remark isn't selected
         {
            try {
               dbcon.dbQueryUpdate (
                     "DELETE FROM EPR.EXAMINATION_DESCRIPTION " +
                     "WHERE RECORD_ID = " + recordId +
                     " AND EXAMINATION_TYPE_ID = " + (i+1));
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "ObservationsPane/savePane",
                        "Delete examination description"));
            }
         }
      }

      // Save fractures
      if (fracturePanel != null && fracturePanel.getSelected())
         fracturePanel.save();

      // Save luxation
      if (luxationPanel != null && luxationPanel.getSelected())
         luxationPanel.save();

      // Save paralysis
      if (paralysisPanel != null)
      {
         if (paralysisInDb && paralysisPanel.isNotNormal()) // In db and not no
         {
            try {
               dbcon.dbQueryUpdate (
                     "UPDATE EPR.PARALYSIS SET RIGHT_SIDE = " +
                     paralysisPanel.getRightSide() + ", LEFT_SIDE = " +
                     paralysisPanel.getLeftSide() + " WHERE RECORD_ID = " +
                     recordId);
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "ObservationsPane/savePane",
                        "Updateing paralysis"));
            }
         }
         else if (paralysisInDb) // In db and normal
         {
            try {
               dbcon.dbQueryUpdate (
                     "DELETE FROM EPR.PARALYSIS WHERE RECORD_ID = " + recordId);
               paralysisInDb = false;
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "ObservationsPane/savePane",
                        "Delete paralysis"));
            }
         }
         else if (paralysisPanel.isNotNormal()) // Not in db and not normal
         {
            try {
               dbcon.dbQueryUpdate (
                     "INSERT INTO EPR.PARALYSIS (RECORD_ID, " +
                  "RIGHT_SIDE, LEFT_SIDE) VALUES (" + recordId + ", " +
                  paralysisPanel.getRightSide() + ", " +
                  paralysisPanel.getLeftSide() + ")");
               paralysisInDb = true;
            } catch (SQLException e) {
               lg.addLog(new Log(e.getMessage(),
                        "ObservationsPane/savePane",
                        "Insert paralysis"));
            }
         }

      }
   }

   /**
    * Method used to update an examination. If it is selected an entry is
    * inserted, if it is unselected the attribute is removed.
    * @param id The attribute id to update.
    */
   private void updateExamination(int id)
   {
      try {
         if (selectedAttr[id])
            dbcon.dbQueryUpdate (
                  "DELETE FROM EPR.EXAMINATION WHERE " +
                  "RECORD_ID = " + recordId +
                  " AND EXAMINATION_ATTRIBUTE = " + id);
         else
            dbcon.dbQueryUpdate (
                  "INSERT INTO EPR.EXAMINATION (RECORD_ID, " +
                  "EXAMINATION_ATTRIBUTE) VALUES (" + recordId + ", " +
                  id + ")");

         selectedAttr[id] = !selectedAttr[id];
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "ObservationsPane/updateExamination",
                  "DELETE/INSERT examination "+id+"("+selectedAttr[id]+")"));
      }
   }

   /**
    * Method that deselects the ua-button in the selected observations panel.
    * Called by the action listener when a button in the panel is pressed.
    * @param i The number of the panel that the button was pressed in.
    */
   public void btChange(int id)
   {
      for (int i=0 ; i < btVec[id].size() ; i++)
      {
         if (((JToggleButton)btVec[id].elementAt(i)).isSelected())
         {
            uaBt[id].setSelected(false);
            return;
         }
      }
   }

   /**
    * Method that shows the select fracture location popup.
    */
   public void selectFracture()
   {
      if (fracturePanel == null)
         fracturePanel = new FracturePanel(this, listener, dbcon, lg, recordId,
               isEditable);
      
      fracturePopup = popupFactory.getPopup(pr, fracturePanel,
            pr.getX()+100, pr.getY()+150);

      fractureBt.setEnabled(false);
      fracturePopup.show();
   }

   /**
    * Method called when closing the fracture popup.
    */
   public void closeFracture()
   {
      fractureBt.setSelected(fracturePanel.getSelected());
      fractureBt.setEnabled(true);
      fracturePopup.hide();
   }

   /**
    * Method that shows the select luxation location popup.
    */
   public void selectLuxation()
   {
      if (luxationPanel == null)
         luxationPanel = new LuxationPanel(this, listener, dbcon, lg, recordId,
               isEditable);
      
      luxationPopup = popupFactory.getPopup(pr, luxationPanel,
            pr.getX()+100, pr.getY()+150);

      luxationBt.setEnabled(false);
      luxationPopup.show();
   }

   /**
    * Method called when closing the luxation popup.
    */
   public void closeLuxation()
   {
      luxationBt.setSelected(luxationPanel.getSelected());
      luxationBt.setEnabled(true);
      luxationPopup.hide();
   }

   /**
    * Method that shows the select paralysis popup.
    */
   public void selectParalysis()
   {
      if (paralysisPanel == null)
         paralysisPanel = new ParalysisPanel(listener, paralysisRight,
               paralysisLeft, isEditable);

      paralysisPopup = popupFactory.getPopup(pr, paralysisPanel,
            pr.getX()+100, pr.getY()+200);
      paralysisBt.setEnabled(false);
      paralysisPopup.show();
   }

   /**
    * Method called when closing the paralysis popup.
    */
   public void closeParalysis()
   {
      paralysisBt.setSelected(paralysisPanel.isNotNormal());
      paralysisBt.setEnabled(true);
      paralysisPopup.hide();
   }

} 
