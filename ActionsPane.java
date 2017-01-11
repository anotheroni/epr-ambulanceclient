import javax.swing.*;
import javax.swing.table.*;
import javax.swing.PopupFactory.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Class implementing the actions pane in a patient record.
 *
 * @version 20030904
 * @author Oskar Nilsson
 */
public class ActionsPane extends JPanel
{
   private final int NUMPANELS = 7;
   private final int PANELWIDTH = 580;
   private final int HEIGHT1 = 54;
   private final int HEIGHT2 = 82;
   private final int TEXTFLDLENGTH = 20;

   private final String[] NAMES = {
      "Allmäntillstånd", "Huvud/Halsrygg",
      "Hjärta", "Lungor/Andning/Bröstkorg", "Extrimiteter", "Ryggrad/Bäcken",
      "Neurologi"
   };

   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;
   private boolean isEditable;

   private JPanel[] panel;
   private FlowLayout[] panelLayout;

   private boolean[] selectedAttr = null;

   private Vector[] btVec;
   private Vector[] idVec;

   private JTextArea descriptionTA;
   private boolean descriptionExists;

   /**
    * Constructor, creates an observation pane.
    * @param pr A reference to the patient record the pane is a part of.
    * @param dbcon A reference to the database.
    * @param wl A reference to the word list used by autocomplete.
    * @param lg The log to report errors to.
    * @param dFont The font to use for JLabels.
    * @param recordId The id of the patient record.
    * @param isEditable True if the record is editable.
    */
   public ActionsPane(PatientRecord pr, DB2Connect dbcon, WordList wl, 
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

      panel = new JPanel[NUMPANELS]; 
      panelLayout = new FlowLayout[NUMPANELS];
      btVec = new Vector[NUMPANELS];
      idVec = new Vector[NUMPANELS];

      ResultSet rs = null;
      PreparedStatement ps = null;

      Insets ins = new Insets(2,4,2,4);
      Dimension dim;

      int tmpID;
      JToggleButton tmpBt;

      // Get the selected attributes from the database
      try {
         rs = dbcon.dbQuery("SELECT MAX(ATTRIBUTE_ID) FROM " +
               "EPR.ACTION_ATTRIBUTES WHERE DISABLE = 0");
         rs.next();

         selectedAttr = new boolean[rs.getInt(1)+1];
         for (int i=0 ; i < selectedAttr.length ; i++)
            selectedAttr[i] = false;
         rs.close();

         rs = dbcon.dbQuery("SELECT ACTION_ATTRIBUTE_ID " +
               "FROM EPR.ACTION_PERFORMED WHERE RECORD_ID = " + recordId);
         while (rs.next())
            selectedAttr[rs.getInt(1)] = true;  // ACTION_ATTRIBUTE_ID
         rs.close();

      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/ActionsPane",
                  "Reading selected attributes"));
      }

      //--- Panels
      try {
         ps = dbcon.prepareStatement(
               "SELECT ATTRIBUTE_ID, ATTRIBUTE_NAME " +
               "FROM EPR.ACTION_ATTRIBUTES WHERE ACTION_TYPE_ID = ? " +
               "AND DISABLE = 0 ORDER BY GUI_ORDER");
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/ActionsPane",
                  "Preparing attribute name query"));
      }
      for (int i=0 ; i < NUMPANELS ; i++)
      {
         panel[i] = new JPanel();
         panelLayout[i] = new FlowLayout(FlowLayout.LEFT, 5, 2); 
         panel[i].setLayout(panelLayout[i]);
         panel[i].setBorder(BorderFactory.createTitledBorder(NAMES[i]));

         btVec[i] = new Vector();
         idVec[i] = new Vector();

         if (ps == null)
            continue;

         try {
            ps.setInt(1, i+1);
            rs = ps.executeQuery();
            while (rs.next())
            {
               tmpBt = new JToggleButton(rs.getString(2)); // ATTRIBUTE_NAME
               tmpBt.setMargin(ins);
               tmpBt.setFont(dFont);
               tmpBt.setEnabled(isEditable);
               tmpID = rs.getInt(1);    // ATTRIBUTE_ID
               try {
                  tmpBt.setSelected(selectedAttr[tmpID]);
               } catch (ArrayIndexOutOfBoundsException f) {}
               btVec[i].add(tmpBt);
               idVec[i].add(new Integer(tmpID));
            }
            rs.close();
         } catch (SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/ActionsPane",
                  "Reading attribute names " + i));
         }

         for (int j=0 ; j < btVec[i].size() ; j++)
            panel[i].add((JToggleButton)btVec[i].elementAt(j));

         dim = panelLayout[i].preferredLayoutSize(panel[i]);
         height = (dim.getWidth() > PANELWIDTH ? HEIGHT2 : HEIGHT1);
         panel[i].setBounds(4, currentPos, PANELWIDTH, height); 
         currentPos += height;

      }  // panel for-loop
      
      if (ps != null)
         try { ps.close(); } catch (SQLException e) {}

      //--- Textarea
      try {
         rs = dbcon.dbQuery(
               "SELECT DESCRIPTION FROM EPR.ACTION_DESCRIPTION " +
               "WHERE RECORD_ID = " + recordId);
         if (rs.next())
         {
            descriptionTA = new JTextArea(rs.getString(1)); // DESCRIPTION
            descriptionExists = true;
         }
         else
         {
            descriptionTA = new JTextArea();
            descriptionExists = false;
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/ActionsPane",
                  "Reading action description"));
         descriptionTA = new JTextArea();
      }
      descriptionTA.setEditable(isEditable);
      descriptionTA.setLineWrap(true);
      descriptionTA.setWrapStyleWord(true);

      // Focus
      descriptionTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      descriptionTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener descTAListener = 
         new AutoComplete_Listener(descriptionTA, wl, pr.getUserId());
      descriptionTA.addKeyListener(descTAListener);

      JScrollPane descScrollPane = new JScrollPane(descriptionTA);
      descScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      descScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Beskrivning"),
               descScrollPane.getBorder()));
      descScrollPane.setBounds(4, currentPos, PANELWIDTH, 90);
      descScrollPane.getVerticalScrollBar().setFocusable(false);

      //--- Main
      setLayout(null);
      for (int i=0 ; i < NUMPANELS ; i++)
         add(panel[i]);
      add(descScrollPane);
   }

   /**
    * Method called when tha record is closed.
    * Saves the state to the database.
    */
   public void savePane()
   {
      int id;
      PreparedStatement psDel = null;
      PreparedStatement psIns = null;

      if (!isEditable)  // No reason to save if the pane isn't editable
         return;

      try {
         psDel = dbcon.prepareStatement(
               "DELETE FROM EPR.ACTION_PERFORMED WHERE RECORD_ID = ? " +
               " AND ACTION_ATTRIBUTE_ID = ?");
         psIns = dbcon.prepareStatement(
               "INSERT INTO EPR.ACTION_PERFORMED (RECORD_ID, " +
               "ACTION_ATTRIBUTE_ID) VALUES (?, ?)");
      } catch (SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/savePane",
                  "Preparing statements"));
      }

      // Save the state of the togglebuttons
      for (int j=0 ; j < NUMPANELS ; j++)
      {
         for (int i=0 ; i < btVec[j].size() ; i++)
         {
            id = ((Integer)idVec[j].elementAt(i)).intValue();
            // Only save state in db if the selection has changed
            if (((JToggleButton)btVec[j].elementAt(i)).isSelected() !=
                  selectedAttr[id])
            {
               try {
                  if (selectedAttr[id])
                  {
                     if (psDel == null)
                        continue;
                     psDel.setInt(1, recordId);
                     psDel.setInt(2, id);
                     psDel.executeUpdate();
                  }
                  else
                  {
                     if (psIns == null)
                        continue;
                     psIns.setInt(1, recordId);
                     psIns.setInt(2, id);
                     psIns.executeUpdate();
                  }

                  selectedAttr[id] = !selectedAttr[id];
               } catch (SQLException e) {
                  lg.addLog(new Log(e.getMessage(),
                           "ActionsPane/savePane",
                           "DELETE/INSERT actions "+id+","+selectedAttr[id]));
               }
            }
         }
      }
      if (psDel != null)
         try { psDel.close(); } catch (SQLException e) {}
      if (psIns != null)
         try { psIns.close(); } catch (SQLException e) {}
      // Save text area
      if (descriptionExists)
      {
         try {
            dbcon.dbQueryUpdate(
                  "UPDATE EPR.ACTION_DESCRIPTION SET DESCRIPTION = '" +
                  descriptionTA.getText() + "' WHERE RECORD_ID = " + recordId);
         } catch (SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/savePane",
                  "Update description"));
         }
      }
      else
      {
         try {
            dbcon.dbQueryUpdate (
                  "INSERT INTO EPR.ACTION_DESCRIPTION (RECORD_ID, " +
                  "DESCRIPTION) VALUES (" + recordId + ", '" +
                  descriptionTA.getText() + "')");
            descriptionExists = true;
         } catch (SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "ActionsPane/savePane",
                  "Inserting description"));
         }
      }
   }

}
