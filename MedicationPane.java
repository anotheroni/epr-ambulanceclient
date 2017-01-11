import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import javax.swing.text.AbstractDocument;

/**
  * Class implementing the medication panel in a patient record.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class MedicationPane extends JPanel implements FocusEventReceiver
{
   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;
   private boolean isEditable;

   private Medicine medicines[];

   private JComboBox medicineNameCBox;
   private JSpinner medicineAmountSpin;
   private JLabel medicineConcLbl;
   private JButton medicineGiveBt;
   private JButton deleteBt;
 
   private MedicationTableModel medicationTModel;
   private JTable medicationTable;

   private JPanel effectsPanel;
   private JTextField effectFld;
   private JButton addEffectBt;

   private MedicationEffectTableModel effectTModel;
   private JTable effectTable;

   private JTextField delDoctorFld;
   private boolean delDoctorExists = false;

   private int lastCell = 0;
 
   /**
     * Constructor, builds the panel.
     * @param pr A reference to the current patient record.
     * @param dbcon A refernece to the database.
     * @param wl A referencce to the word list used by autocomplete.
     * @param lg The log handler to report errors to.
     * @param dFont The font used to display labels.
     * @param recordId The id number of the current record.
     * @param isEditable True if the patient record is editable.
     * @param userId User id of the carer.
     * @param userName Username of the carer.
     * @param driverId User id of the driver.
     * @param driverName Username of the driver.
     */
   public MedicationPane(PatientRecord pr, DB2Connect dbcon, WordList wl,
        LogHandler lg, Font dFont, int recordId, boolean isEditable,
        int userId, String userName, int driverId, String driverName)
   {
      super();

      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.isEditable = isEditable;

      ResultSet rs = null;
      int numMed = 0;

      TableColumn col;

      MedicationPane_Listener listener = new MedicationPane_Listener(this);
 
      //--- Delegating doctor
      JLabel delDoctorLbl = new JLabel("Delegerande läkare");
      delDoctorLbl.setBounds(10, 15, 130, 25);

      delDoctorFld = new JTextField();
      ((AbstractDocument) delDoctorFld.getDocument ()).
         setDocumentFilter (new LimitedTextFilter (64));
      delDoctorFld.setBounds(130, 15, 250, 25);
      delDoctorFld.setEnabled(isEditable);

      try {
         rs = dbcon.dbQuery("SELECT DELEGATING_DOCTOR FROM " +
               "EPR.AMBULANCE_RECORD WHERE RECORD_ID = " + recordId);

         if (rs.next())
         {
            String name = rs.getString(1);   // DELEGATING_DOCTOR
            if (name != null && name.length() > 0)
            {
               delDoctorFld.setText(name);
               delDoctorExists = true;
            }
         }
         rs.close();
      } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/MedicationPane",
                  "Reading delegating doctor"));
      }
 
      // Read medicines
      try {
         rs = dbcon.dbQuery("SELECT COUNT(*) FROM EPR.MEDICINE " +
               "WHERE DISABLE = 0");
         rs.next();
         numMed = rs.getInt(1);
      } catch (SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/MedicationPane",
                  "Reading the number of medicines"));
      }
      medicines = new Medicine[numMed];
      try {
         rs = dbcon.dbQuery("SELECT MEDICINE_ID, MEDICINE_NAME, " +
               "MEDICINE_FORM, MEDICINE_SIZE, MEDICINE_UNIT, " +
               "MEDICINE_CONCENTRATION, MEDICINE_CONC_UNIT " +
               "FROM EPR.MEDICINE WHERE DISABLE = 0");
         for (int i=0 ; i < numMed ; i++)
         {
            rs.next();
            medicines[i] = new Medicine(
                  rs.getInt(1),  // MEDICINE_ID
                  rs.getString(2),  // MEDICINE_NAME
                  rs.getString(3),  // MEDICINE_FORM
                  rs.getInt(4),  // MEDICINE_SIZE
                  rs.getString(5),  // MEDICINE_UNIT
                  rs.getDouble(6),  // MEDICINE_CONCENTRATION
                  rs.getString(7)   // MEDICINE_CONC_UNIT
                  );
         }
         rs.close();
      } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/MedicationPane",
                  "Reading the medicines"));
      }

      medicineNameCBox = new JComboBox(medicines);
      medicineNameCBox.addActionListener(listener);
      medicineNameCBox.setActionCommand("medicineChange");
      medicineNameCBox.setBounds(10,55,300,25);

      medicineAmountSpin = new JSpinner(new SpinnerNumberModel(1,1,9000,1));
      medicineAmountSpin.setBounds(320,55,60,25);
      medicineAmountSpin.addChangeListener(new MedicationChange_Listener(this));

      medicineConcLbl = new JLabel(medicines[0].printConc(1));
      medicineConcLbl.setBounds(390,55,80,25);

      medicineGiveBt = new JButton("Ge");
      medicineGiveBt.setBounds(470,55,60,25);
      medicineGiveBt.addActionListener(listener);
      medicineGiveBt.setActionCommand("give");
      medicineGiveBt.setEnabled(isEditable);
 
      medicationTModel = new MedicationTableModel(pr, dbcon, lg, isEditable);
      medicationTable = new JTable(medicationTModel);
      medicationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      medicationTable.setRowHeight(25);
      medicationTable.setSurrendersFocusOnKeystroke(true);

      MedicationSelection_Listener sListener =
         new MedicationSelection_Listener(this, medicationTable);
      medicationTable.getSelectionModel().addListSelectionListener(sListener);
 
      // Time column
      col = medicationTable.getColumnModel().getColumn(0);
      col.setCellEditor(new DateTableCellEditor(this));
      col.setCellRenderer(new DateTableCellRenderer(lg));
      col.setPreferredWidth(70);
      col.setMinWidth(70);
 
      // Name column
      col = medicationTable.getColumnModel().getColumn(1);
      col.setPreferredWidth(200);
 
      // User column
      col = medicationTable.getColumnModel().getColumn(3);
      JComboBox userCBox = new JComboBox();
      userCBox.addItem(new ListEntry(userName, userId));
      userCBox.addItem(new ListEntry(driverName, driverId));
      userCBox.addItem(new ListEntry("Annan", 0));
      col.setCellEditor(new DefaultCellEditor(userCBox));
      col.setPreferredWidth(70);
 
      JScrollPane medicationScrollPane = new JScrollPane(medicationTable);
      medicationScrollPane.setBounds(10, 90, 570, 150);

      //--- Delete
      deleteBt = new JButton("Ta bort");
      deleteBt.setBounds(500, 250, 80, 25);
      deleteBt.setEnabled(false);
      deleteBt.addActionListener(listener);
      deleteBt.setActionCommand("delete");

      //--- Effects
      effectFld = new JTextField();
      effectFld.setBounds(10,20,460,25);

      AutoComplete_Listener effectFldListener =
         new AutoComplete_Listener(effectFld, wl, userId);
      effectFld.addKeyListener(effectFldListener);     

      addEffectBt = new JButton("Lägg till");
      addEffectBt.setBounds(480,20,80,25);
      addEffectBt.addActionListener(listener);
      addEffectBt.setActionCommand("addeffect");
      addEffectBt.setEnabled(isEditable);

      //- Table
      effectTModel = new MedicationEffectTableModel(pr, dbcon, lg);
      effectTable = new JTable(effectTModel);
      effectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      effectTable.setRowHeight(25);
      effectTable.setEnabled(isEditable);
      effectTable.setSurrendersFocusOnKeystroke(true);

      // Time column
      col = effectTable.getColumnModel().getColumn(0);
      col.setCellEditor(new DateTableCellEditor());
      col.setCellRenderer(new DateTableCellRenderer(lg));
      col.setPreferredWidth(70);
      col.setMinWidth(70);

      col = effectTable.getColumnModel().getColumn(1);
      col.setPreferredWidth(430);

      JScrollPane effectScrollPane = new JScrollPane(effectTable);
      effectScrollPane.setBounds(10,55,550,75);
 
      // Panel 
      effectsPanel = new JPanel();
      effectsPanel.setLayout(null);
      effectsPanel.setBounds(10,280,570,145);
      effectsPanel.setBorder(BorderFactory.createTitledBorder("Effekter"));

      effectsPanel.add(effectFld);
      effectsPanel.add(addEffectBt);
      effectsPanel.add(effectScrollPane);
      effectsPanel.setVisible(false);

      //--- Main panel
      setLayout(null);
      add(medicineNameCBox);
      add(medicineAmountSpin);
      add(medicineConcLbl);
      // Only needed if there exist a medication to give
      if (numMed != 0)
         add(medicineGiveBt);
      add(medicationScrollPane);
      add(delDoctorLbl);
      add(delDoctorFld);
      add(deleteBt);
      add(effectsPanel);
   }

   /**
     * Method called when tha record is closed.
     * Saves the state to the database.
     */
   public void savePane()
   {
      if (!isEditable)  // No reason to save if the pane isn't editable
         return;
 
      if (delDoctorExists || delDoctorFld.getText().length() > 0)
      {
         try {
            dbcon.dbQueryUpdate (
                  "UPDATE EPR.AMBULANCE_RECORD SET DELEGATING_DOCTOR = '" +
                  delDoctorFld.getText() + "' WHERE RECORD_ID = " + recordId);
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/savePane",
                  "Updating the delegating doctor"));
         }
      }
   }

   /**
     * Method that adds a medication post to the database.
     */
   public void giveMedication()
   {
      Medicine med = (Medicine) medicineNameCBox.getSelectedItem();
 
      try {
         dbcon.dbQueryUpdate ("INSERT INTO EPR.GIVEN_MEDICATIONS (" +
               "MEDICINE_ID, DOZAGE, RECORD_ID, GIVEN_TIME, GIVEN_BY) " +
               "VALUES (" + med.getId() + ", " +
               ((Integer) medicineAmountSpin.getValue()).intValue() + ", " +
               recordId + ", CURRENT TIME, " + pr.getUserId() + ")");
      } catch(SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/giveMedication",
                  "Insert medication"));
      }

      medicationTModel.updateTable();
   }

   /**
     * Method that updates the medicine concentration label when the medicine
     * or the amount changes.
     */
   public void updateConcLabel()
   {
      medicineConcLbl.setText(
            ((Medicine)medicineNameCBox.getSelectedItem()).printConc(
                 ((Integer)medicineAmountSpin.getValue()).intValue()
                 )
            );
   }

   /**
     * Method that add a medication effect to the database.
     */
   public void addEffect()
   {
      if (effectFld.getText().length() < 1)
      {
         pr.setMessage("Ingen effekt är angiven");
         return;
      }
      int medId = medicationTModel.getRowId(medicationTable.getSelectedRow());
      if (medId == -1)
      {
         pr.setMessage("Igen medicinering är vald");
         return;
      }
      try {
         dbcon.dbQueryUpdate ("INSERT INTO EPR.MEDICINE_EFFECTS " +
               "(GIVEN_MEDICINE_ID, EFFECT_TIME, EFFECT) " +
               "VALUES (" + medId + ", CURRENT TIME, '" + effectFld.getText() +
               "')");
      } catch(SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/addEffect",
                  "Inser effect"));
      }
      effectTModel.updateTable(medId);
      effectFld.setText("");
   }

   /**
     * Method called by the action listener to remove the selected medicine
     * in the table and remove it from the database.
     */
   public void deleteMed()
   {
      int medId = medicationTModel.getRowId(medicationTable.getSelectedRow());
      if (medId == -1)
      {
         pr.setMessage("Ignen medicinering är vald");
         return;
      }
      try {
         dbcon.dbQueryUpdate (
               "DELETE FROM EPR.GIVEN_MEDICATIONS WHERE RECORD_ID = " +
               recordId + " AND GIVEN_MEDICATION_ID = " + medId);
      } catch(SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                  "MedicationPane/deleteMed",
                  "Delete given medication"));
      }
      medicationTModel.updateTable();
   }
 
   /**
     * Method used by selection listener to signal that row selection has
     * changed.
     */
   public void rowSelectionChanged()
   {
      effectsPanel.setVisible(true);
      effectTModel.updateTable(medicationTModel.getRowId(
               medicationTable.getSelectedRow()));
      if (isEditable)
         deleteBt.setEnabled(true);
   }

   /**
     * Method called by the cell editor focus listener when the editor
     * looses focus. Saves the current value.
     * @param val The text in the editor.
     * @return null if all is ok, else an error message.
     */
   public String saveCurrentCell (String val)
   {
      medicationTModel.setValueAt(val, lastCell, 0);
      return null;
   }

   /**
     * Method that sets the current active cell (Not used).
     */
   public void setCurrentCell ()
   {
      ;
   }

   /**
     * Method called by DateTableCellEditor to report the current row in the
     * table being edited. Needed to be able to save the edited value when
     * focus is lost.
     * @param row The current row being edited.
     */
   public void setLastCell (int row)
   {
      lastCell = row;
   }

}
