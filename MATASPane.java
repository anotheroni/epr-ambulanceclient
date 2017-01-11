import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Class implementing the MATAS panel in a patient record.
 *
 * @version 20030904
 * @author Oskar Nilsson
 */
public class MATASPane extends JPanel
{

   private final int DEFAULT_DIAGNOSIS_GROUP = 1;
   private final int DEFAULT_DIAGNOSIS_TYPE = 1;

   private PatientRecord pr;
   private DB2Connect dbcon;
   private WordList wl;
   private LogHandler lg;
   private int recordId;
   private boolean isEditable;

   private boolean dbRecordExists = false;

   private JTextArea medTA = null;
   private JTextArea allergyTA = null;
   private JTextArea anamnesisTA = null;
   private JTextArea diagnosisTA = null;
   private JTextArea foodTA = null;

   private JPanel diagnosisPanel;
   private JComboBox diagnosisTypeCBox = null;
   private Vector diagnosisTypeVec;
   private JComboBox diagnosisGroupCBox = null;
   private Vector diagnosisGroupVec;
   private int curDiagnosisGroup;

   /**
    * Constructor, builds the panel.
    * @param pr A reference to the current patient record.
    * @param dbcon A refernece to the database.
    * @param wl The word list used by auto comeplete.
    * @param lg The log handler to report errors to
    * @param dFont The font used to display labels.
    * @param recordId The id number of the current record.
    * @param isEditable True if the patient record is editable.
    */
   public MATASPane(PatientRecord pr, DB2Connect dbcon, WordList wl,
         LogHandler lg, Font dFont, int recordId, boolean isEditable)
   {
      super();

      this.pr = pr;
      this.dbcon = dbcon;
      this.wl = wl;
      this.lg = lg;
      this.recordId = recordId;
      this.isEditable = isEditable;

      ResultSet rs = null;
      ListEntry def = null;

      MATASPane_Listener listener = new MATASPane_Listener(this);

      try {
         rs = dbcon.dbQuery("SELECT MEDICINE, ALLERGY, ANAMNESIS, DIAGNOSIS, " +
               "LAST_MEAL FROM EPR.MATAS WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
            dbRecordExists = true;

         medTA = new JTextArea(rs.getString(1));   // MEDICINE
         allergyTA = new JTextArea(rs.getString(2));  // ALLERGY
         anamnesisTA = new JTextArea(rs.getString(3));   // ANAMNESIS
         diagnosisTA = new JTextArea(rs.getString(4));   // DIAGNOSIS
         foodTA = new JTextArea(rs.getString(5));  // LAST_MEAL
         rs.close();
      } catch (SQLException e) {
         if (medTA == null)
            medTA = new JTextArea();
         if (allergyTA == null)
            allergyTA = new JTextArea();
         if (anamnesisTA == null)
            anamnesisTA = new JTextArea();
         if (diagnosisTA == null)
            diagnosisTA = new JTextArea();
         if (foodTA == null)
            foodTA = new JTextArea();
      }

      // -- Medicine
      medTA.setLineWrap(true);
      medTA.setWrapStyleWord(true);
      medTA.setEnabled(isEditable);
      // Focus
      medTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      medTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener medTAListener = 
         new AutoComplete_Listener(medTA, wl, pr.getUserId());
      medTA.addKeyListener(medTAListener);

      JScrollPane medScrollPane = new JScrollPane(medTA);
      medScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      medScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Medicin"),
               medScrollPane.getBorder()));
      medScrollPane.setBounds(5, 0, 580, 75);
      medScrollPane.getVerticalScrollBar().setFocusable(false);

      // -- Allergy
      allergyTA.setLineWrap(true);
      allergyTA.setWrapStyleWord(true);
      allergyTA.setEnabled(isEditable);
      // Focus
      allergyTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      allergyTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener allergyTAListener =
         new AutoComplete_Listener(allergyTA, wl, pr.getUserId());
      allergyTA.addKeyListener(allergyTAListener);

      JScrollPane allergyScrollPane = new JScrollPane(allergyTA);
      allergyScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      allergyScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Allergi"),
               allergyScrollPane.getBorder()));
      allergyScrollPane.setBounds(5, 80, 580, 75);
      allergyScrollPane.getVerticalScrollBar().setFocusable(false);

      // Anamnesis
      anamnesisTA.setLineWrap(true);
      anamnesisTA.setWrapStyleWord(true);
      anamnesisTA.setEnabled(isEditable);
      // Focus
      anamnesisTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      anamnesisTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener anamnesisTAListener =
         new AutoComplete_Listener(anamnesisTA, wl, pr.getUserId());
      anamnesisTA.addKeyListener(anamnesisTAListener);

      JScrollPane anamnesisScrollPane = new JScrollPane(anamnesisTA);
      anamnesisScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      anamnesisScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Tidigare sjukdomar"),
               anamnesisScrollPane.getBorder()));
      anamnesisScrollPane.setBounds(5, 160, 580, 75);
      anamnesisScrollPane.getVerticalScrollBar().setFocusable(false);

      //--- Diagnosis

      int curDiagnosisType = DEFAULT_DIAGNOSIS_TYPE;
      curDiagnosisGroup = DEFAULT_DIAGNOSIS_GROUP;

      // Read diagnosis values from the patient record in the database
      try {
         rs = dbcon.dbQuery("SELECT AR.DIAGNOSIS_ID, DT.DIAGNOSIS_GROUP_ID " +
               "FROM EPR.AMBULANCE_RECORD AS AR, EPR.DIAGNOSIS_TYPE AS DT " +
               "WHERE RECORD_ID = " + recordId +
               " AND AR.DIAGNOSIS_ID = DT.DIAGNOSIS_TYPE_ID");
         if (rs.next())
         {
            curDiagnosisGroup = rs.getInt(2);   // DIAGNOSIS_GROUP_ID
            curDiagnosisType = rs.getInt(1); // DIAGNOSIS_ID
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MATASPane/MATASPane",
                  "Reading diagnosis"));
      }

      // Diagnosis Comboboxes
      diagnosisPanel = new JPanel();
      try {
         rs = dbcon.dbQuery("SELECT DIAGNOSIS_GROUP_ID, DIAGNOSIS_GROUP_NAME " +
               "FROM EPR.DIAGNOSIS_GROUP");
         diagnosisGroupVec = new Vector(12);
         ListEntry tmp;
         def = null;
         while (rs.next())
         {
            tmp = new ListEntry(
                  rs.getString(2),  // DIAGNOSIS_GROUP_NAME
                  rs.getInt(1));    // DIAGNOSIS_GROUP_ID
            diagnosisGroupVec.add(tmp);
            if (tmp.getNumber() == curDiagnosisGroup)
               def = tmp;
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MATASPane/MATASPane",
                  "Read diagnoisis group"));
      }

      diagnosisGroupCBox = new JComboBox(diagnosisGroupVec);
      diagnosisGroupCBox.setBounds(5, 20, 200, 25);
      curDiagnosisGroup = diagnosisGroupCBox.getSelectedIndex();
      diagnosisGroupCBox.setMaximumRowCount(10);
      if (def != null)
         diagnosisGroupCBox.setSelectedItem(def);

      diagnosisGroupCBox.addActionListener(listener);
      diagnosisGroupCBox.setActionCommand("groupchange");
      diagnosisGroupCBox.setEnabled(isEditable);

      try {
         rs = dbcon.dbQuery(
               "SELECT DIAGNOSIS_TYPE_ID, DIAGNOSIS_TYPE_NAME FROM " +
               "EPR.DIAGNOSIS_TYPE WHERE DIAGNOSIS_GROUP_ID = " +
               DEFAULT_DIAGNOSIS_GROUP + " AND DISABLE = 0");
         diagnosisTypeVec = new Vector();
         ListEntry tmp;
         def = null;
         while (rs.next())
         {
            tmp = new ListEntry(rs.getString(2),   // DIAGNOSIS_TYPE_NAME
                  rs.getInt(1));    // DIAGNOSIS_TYPE_ID
            diagnosisTypeVec.add(tmp);
            if (tmp.getNumber() == curDiagnosisType)
               def = tmp;
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MATASPane/MATASPane",
                  "Read diagnosis type"));
      }

      diagnosisTypeCBox = new JComboBox(diagnosisTypeVec);
      diagnosisTypeCBox.setBounds(215,20,200,25);
      if (def != null)
         diagnosisTypeCBox.setSelectedItem(def);
      diagnosisTypeCBox.setEnabled(isEditable);

      diagnosisPanel.setBounds(5,240,580,55);
      diagnosisPanel.setLayout(null);
      diagnosisPanel.setBorder(BorderFactory.createTitledBorder(
               "Arbetsdiagnos"));
      diagnosisPanel.add(diagnosisTypeCBox);
      diagnosisPanel.add(diagnosisGroupCBox);

      // -- Diagnosis Textarea
      diagnosisTA.setLineWrap(true);
      diagnosisTA.setWrapStyleWord(true);
      diagnosisTA.setEnabled(isEditable);
      // Focus
      diagnosisTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      diagnosisTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener diagnosisTAListener =
         new AutoComplete_Listener(diagnosisTA, wl, pr.getUserId());
      diagnosisTA.addKeyListener(diagnosisTAListener);

      JScrollPane diagnosisScrollPane = new JScrollPane(diagnosisTA);
      diagnosisScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      diagnosisScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Aktuell skada/sjukdom"),
               diagnosisScrollPane.getBorder()));
      diagnosisScrollPane.setBounds(5, 295, 580, 75);
      diagnosisScrollPane.getVerticalScrollBar().setFocusable(false);

      // Food
      foodTA.setLineWrap(true);
      foodTA.setWrapStyleWord(true);
      foodTA.setEnabled(isEditable);
      // Focus
      foodTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      foodTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener foodTAListener =
         new AutoComplete_Listener(foodTA, wl, pr.getUserId());
      foodTA.addKeyListener(foodTAListener);

      JScrollPane foodScrollPane = new JScrollPane(foodTA);
      foodScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      foodScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Senaste Måltid"),
               foodScrollPane.getBorder()));
      foodScrollPane.setBounds(5, 375, 580, 75);
      foodScrollPane.getVerticalScrollBar().setFocusable(false);

      setLayout(null);
      add(medScrollPane);
      add(allergyScrollPane);
      add(anamnesisScrollPane);
      add(diagnosisPanel);
      add(diagnosisScrollPane);
      add(foodScrollPane);
   }

   /**
    * Method called when tha record is closed.
    * Saves the state to the database.
    */
   public void savePane()//TODO only save when needed, add flag in listener...
   {
      if (!isEditable)  // No reason to save if the pane isn't editable
         return;

      if (dbRecordExists)
      {
         try {
            PreparedStatement ps = dbcon.prepareStatement(
                  "UPDATE EPR.MATAS SET MEDICINE = ? , ALLERGY = ? ," +
                  "ANAMNESIS = ? , DIAGNOSIS = ?, LAST_MEAL = ? " +
                  "WHERE RECORD_ID = ?");
            ps.setString(1, medTA.getText());
            ps.setString(2, allergyTA.getText());
            ps.setString(3, anamnesisTA.getText());
            ps.setString(4, diagnosisTA.getText());
            ps.setString(5, foodTA.getText());
            ps.setInt(6, recordId);
            ps.execute();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "MATASPane/savePane",
                     "Update matas"));
         }
      }
      else
      {
         try {
            PreparedStatement ps = dbcon.prepareStatement(
                  "INSERT INTO EPR.MATAS (RECORD_ID, MEDICINE, " +
                  "ALLERGY, ANAMNESIS, DIAGNOSIS, LAST_MEAL) VALUES (" +
                  "?, ?, ?, ?, ?, ?)");
            ps.setInt(1, recordId);
            ps.setString(2, medTA.getText());
            ps.setString(3, allergyTA.getText());
            ps.setString(4, anamnesisTA.getText());
            ps.setString(5, diagnosisTA.getText());
            ps.setString(6, foodTA.getText());
            ps.execute();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "MATASPane/savePane",
                     "Insert matas"));
         }
         dbRecordExists = true;
      }

      try {
         dbcon.dbQueryUpdate (
               "UPDATE EPR.AMBULANCE_RECORD SET DIAGNOSIS_ID = " +
               ((ListEntry) diagnosisTypeCBox.getSelectedItem()).getNumber() +
               " WHERE RECORD_ID = " + recordId);
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MATASPane/savePane",
                  "Update diagnosis"));
      } catch (NullPointerException f) {}    // No value exists in cbox

   }

   /**
    * Method that is called when the Diagnosis Group combobox changes.
    * Updates the contents of the diagnosis type combobox.
    */
   public void diagnosisGroupChanged()
   {
      ListEntry selItm = (ListEntry) diagnosisGroupCBox.getSelectedItem();

      if (curDiagnosisGroup == selItm.getNumber())
         return;

      ResultSet rs;

      try {
         rs = dbcon.dbQuery(
               "SELECT DIAGNOSIS_TYPE_ID, DIAGNOSIS_TYPE_NAME " +
               "FROM EPR.DIAGNOSIS_TYPE WHERE DIAGNOSIS_GROUP_ID = " +
               selItm.getNumber() + " AND DISABLE = 0");

         diagnosisTypeCBox.removeAllItems();
         while (rs.next())
            diagnosisTypeCBox.addItem(new ListEntry(
                     rs.getString(2),  // DIAGNOSIS_TYPE_NAME
                     rs.getInt(1)));   // DIAGNOSIS_TYPE_ID
         rs.close();
         curDiagnosisGroup = selItm.getNumber();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MATASPane/diagnosisGroupChanged",
                  "Read diagnosis type"));
      }
   }

    /**
     * Method that adds a comment to the end of the diagnosis textarea.
     * @param anamnesis The comment to add.
     */
   public void addDiagnosis (String anamnesis)
   {
      if (diagnosisTA.getCaretPosition () > 0)
         diagnosisTA.append ("\n");
      diagnosisTA.append (anamnesis);
   }

}
