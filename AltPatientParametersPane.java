import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
  * Class implementing patient parameters pane in a patient record.
  *
  * @version 20030626
  * @author Oskar Nilsson
  */
public class AltPatientParametersPane extends JPanel
{
   private PatientRecord pr;
   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;
   private boolean isEditable;

   private JRowTable paramTable;
   private ParameterTableModel paramTModel;

   private JCheckBox rlsCB;
   private RLSObservationTableRow rlsRow = null;
 
   private JCheckBox pulsCB;
   private StdObservationTableRow pulsRow = null;
 
   private JCheckBox bpCB;
   private BpObservationTableRow bpRow = null;
 
   private JCheckBox breathCB;
   private StdObservationTableRow breathRow = null;
 
   private JCheckBox gcsCB;
   private GCSModel gcsModel = null;
 
   private JCheckBox oxyCB;
   private StdObservationTableRow oxyRow = null;
 
   private JCheckBox satCB;
   private StdObservationTableRow satRow = null;
 
   private JCheckBox glukosCB;
   private FloatObservationTableRow glukosRow = null;
 
   private JCheckBox bodyposCB;
   private BodyObservationTableRow bodyposRow = null;
 
   private JCheckBox vasCB;
   private StdObservationTableRow vasRow = null;

   private JCheckBox eyeCB;
   private EyeModel eyeModel = null;
 
   /**
     * Constructor, creates a patient parameters pane.
     * @param pr A reference to the patient record the pane is a part of.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param dFont The font to use for JLabels.
     * @param recordId The id of the patient record.
     * @param isEditable True if the patient record is editable
     */
   public AltPatientParametersPane(PatientRecord pr, DB2Connect dbcon,
         LogHandler lg, Font dFont, int recordId, boolean isEditable)
   {
      super();

      this.pr = pr;
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;
      this.isEditable = isEditable;
 
      AltPatientParametersPane_Listener listener = 
         new AltPatientParametersPane_Listener(this);

      ResultSet rs = null;

      Insets ins = new Insets(0,0,0,0);

      JButton addColumnBt = new JButton("Ny observation");
      addColumnBt.setBounds(5,10,120,25);
      addColumnBt.setToolTipText("Lägg till en ny observation");
      addColumnBt.addActionListener(listener);
      addColumnBt.setActionCommand("addColumn");
      addColumnBt.setEnabled(isEditable);

      // Checkboxes
      JPanel cbPanel = new JPanel();
      cbPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      cbPanel.setBounds(5, 40, 580, 90);
      cbPanel.setBorder(BorderFactory.createTitledBorder("Parametrar"));
      
      rlsCB = new JCheckBox("RLS");
      rlsCB.addActionListener(listener);
      rlsCB.setActionCommand("RLS");
      pulsCB = new JCheckBox("Puls");
      pulsCB.addActionListener(listener);
      pulsCB.setActionCommand("puls");
      bpCB = new JCheckBox("Blodtryck");
      bpCB.addActionListener(listener);
      bpCB.setActionCommand("bp");
      breathCB = new JCheckBox("Adnings frek.");
      breathCB.addActionListener(listener);
      breathCB.setActionCommand("breath");
      gcsCB = new JCheckBox("GCS");
      gcsCB.addActionListener(listener);
      gcsCB.setActionCommand("gcs");
      oxyCB = new JCheckBox("Syre");
      oxyCB.addActionListener(listener);
      oxyCB.setActionCommand("oxy");
      satCB = new JCheckBox("Saturation");
      satCB.addActionListener(listener);
      satCB.setActionCommand("sat");
      glukosCB = new JCheckBox("B-Glukos");
      glukosCB.addActionListener(listener);
      glukosCB.setActionCommand("glukos");
      bodyposCB = new JCheckBox("Kroppsposition");
      bodyposCB.addActionListener(listener);
      bodyposCB.setActionCommand("bodypos");
      vasCB = new JCheckBox("VAS");
      vasCB.addActionListener(listener);
      vasCB.setActionCommand("vas");
      eyeCB = new JCheckBox("Puppiler");
      eyeCB.addActionListener(listener);
      eyeCB.setActionCommand("eye");

      JButton addAllBt = new JButton("Visa alla");
      addAllBt.setSize(90,25);
      addAllBt.setMargin(ins);
      addAllBt.setToolTipText("Visa alla parametrar");
      addAllBt.addActionListener(listener);
      addAllBt.setActionCommand("showAll");

      JButton hideAllBt = new JButton("Dölj alla");
      hideAllBt.setSize(90,25);
      hideAllBt.setMargin(ins);
      hideAllBt.setToolTipText("Dölj alla parametrar");
      hideAllBt.addActionListener(listener);
      hideAllBt.setActionCommand("hideAll");

      cbPanel.add(pulsCB);
      cbPanel.add(bpCB);
      cbPanel.add(breathCB);
      cbPanel.add(oxyCB);
      cbPanel.add(satCB);
      cbPanel.add(glukosCB);
      cbPanel.add(vasCB);
      cbPanel.add(rlsCB);
      cbPanel.add(gcsCB);
      cbPanel.add(eyeCB);
      cbPanel.add(bodyposCB);
      cbPanel.add(addAllBt);
      cbPanel.add(hideAllBt);

      // --- Parameters Table ---
      paramTModel = new ParameterTableModel(pr, dbcon, isEditable);
      paramTable = new JRowTable(paramTModel);
      paramTable.setRowHeight(22);
      paramTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      paramTable.setSurrendersFocusOnKeystroke(true);
      // By default, JTable's tooltips are enabled, and it prepares the
      // renderer componenet to ask if for its tooltip every time the mouse is
      // moved over it. Turning this off can increase performance.
      ToolTipManager.sharedInstance().unregisterComponent(paramTable);
      
      // Row Header
      JTable rowHeaderTable = new JTable(paramTModel.getRowHeader());
      rowHeaderTable.setRowHeight(22);
      rowHeaderTable.setEnabled(false);
      rowHeaderTable.getColumnModel().getColumn(0).setPreferredWidth(90);
      rowHeaderTable.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      rowHeaderTable.setBackground(paramTable.getTableHeader().getBackground());
      rowHeaderTable.setPreferredScrollableViewportSize(
            rowHeaderTable.getPreferredSize());
    
      // Scrollpane
      JScrollPane paramScrollPane = new JScrollPane(paramTable,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      paramScrollPane.setRowHeaderView(rowHeaderTable);
      paramScrollPane.setBounds(5,140,580,380);

      //--- Main ---
      setLayout(null);
      add(addColumnBt);
      add(cbPanel);
      add(paramScrollPane);
   }

   /**
     * Method called when tha record is closed.
     * Saves the state to the database.
     */
   public void savePane()
   {
      paramTModel.saveTable();
   }

   /**
     * Method that adds new column to the parameters table.
     */
   public void addColumn()
   {
      paramTModel.addColumn();
   }

   /**
    * Method that adds all parameter rows in the table.
    */
   public void showAll()
   {
      if (!pulsCB.isSelected ())
      {
         pulsCB.setSelected(true);
         pulsCh();
      }
      if (!bpCB.isSelected ())
      {
         bpCB.setSelected(true);
         bpCh();
      }
      if (!breathCB.isSelected ())
      {
         breathCB.setSelected(true);
         breathCh();
      }
      if (!oxyCB.isSelected ())
      {
         oxyCB.setSelected(true);
         oxyCh();
      }
      if (!satCB.isSelected ())
      {
         satCB.setSelected(true);
         satCh();
      }
      if (!glukosCB.isSelected ())
      {
         glukosCB.setSelected(true);
         glukosCh();
      }
      if (!vasCB.isSelected ())
      {
         vasCB.setSelected(true);
         vasCh();
      }
      if (!rlsCB.isSelected ())
      {
         rlsCB.setSelected(true);
         rlsCh();
      }
      if (!gcsCB.isSelected ())
      {
         gcsCB.setSelected(true);
         gcsCh();
      }
      if (!eyeCB.isSelected ())
      {
         eyeCB.setSelected(true);
         eyeCh();
      }
      if (!bodyposCB.isSelected ())
      {
         bodyposCB.setSelected(true);
         bodyposCh();
      }
   }

   /**
     * Method that hides all parameter rows in the table.
     */
   public void hideAll()
   {
      if (pulsCB.isSelected ())
      {
         pulsCB.setSelected(false);
         pulsCh();
      }
      if (bpCB.isSelected ())
      {
         bpCB.setSelected(false);
         bpCh();
      }
      if (breathCB.isSelected ())
      {
         breathCB.setSelected(false);
         breathCh();
      }
      if (oxyCB.isSelected ())
      {
         oxyCB.setSelected(false);
         oxyCh();
      }
      if (satCB.isSelected ())
      {
         satCB.setSelected(false);
         satCh();
      }
      if (glukosCB.isSelected ())
      {
         glukosCB.setSelected(false);
         glukosCh();
      }
      if (vasCB.isSelected ())
      {
         vasCB.setSelected(false);
         vasCh();
      }
      if (rlsCB.isSelected ())
      {
         rlsCB.setSelected(false);
         rlsCh();
      }
      if (gcsCB.isSelected ())
      {
         gcsCB.setSelected(false);
         gcsCh();
      }
      if (eyeCB.isSelected ())
      {
         eyeCB.setSelected(false);
         eyeCh();
      }
      if (bodyposCB.isSelected ())
      {
         bodyposCB.setSelected(false);
         bodyposCh();
      }
  }
 
   /**
    * Method that adds or removes the Puls row from the observation table.
    * The row is added if the Puls checkbox is selected.
    */  
   public void pulsCh()
   {
      if (pulsCB.isSelected())
      {
         if (pulsRow == null)
            pulsRow = new StdObservationTableRow(recordId, 1,
                  dbcon, lg, "Puls", 0, 200, paramTModel.getTimes(),
                  paramTModel);
         paramTModel.insertRow(pulsRow, 1);
      }
      else
      {
         paramTModel.removeRow(pulsRow);
      }
   }
   
   /**
    * Method that adds or removes the Blood Pressure row from the observation
    * table. The row is added if the Blood Pressure checkbox is selected.
    */ 
   public void bpCh()
   {
      if (bpCB.isSelected())
      {
         if (bpRow == null)
            bpRow = new BpObservationTableRow(recordId, dbcon, lg,
                  paramTModel.getTimes(), paramTModel);
         paramTModel.insertRow(bpRow, 2);
      }
      else
      {
         paramTModel.removeRow(bpRow);
      }
   }

   /**
     * Method that adds or removes the Breath row from the observation table.
     * The row is added if the Breath checkbox is selected.
     */
   public void breathCh()
   {
      if (breathCB.isSelected())
      {
         if (breathRow == null)
            breathRow = new StdObservationTableRow(recordId, 2,
                  dbcon, lg, "Andning", 0, 200, paramTModel.getTimes(),
                  paramTModel);
         paramTModel.insertRow(breathRow, 3);
      }
      else
      {
         paramTModel.removeRow(breathRow);
      }
   }

   /**
     * Method that adds or removes the Oxygen row from the observation table.
     * The row is added if the Oxygen checkbox is selected.
     */ 
   public void oxyCh()
   {
      if (oxyCB.isSelected())
      {
         if (oxyRow == null)
            oxyRow = new OxyObservationTableRow(recordId, 3,
                  dbcon, lg, "Syre (l/min)", 0, 10, paramTModel.getTimes(),
                  paramTModel);
         paramTModel.insertRow(oxyRow, 4);
      }
      else
      {
         paramTModel.removeRow(oxyRow);
      }
   }

   /**
     * Method that adds or removes the Saturation row from the observation
     * table. The row is added if the Saturation checkbox is selected.
     */
   public void satCh()
   {
      if (satCB.isSelected())
      {
         if (satRow == null)
            satRow = new StdObservationTableRow(recordId, 4,
                  dbcon, lg, "Saturation", 0, 100, paramTModel.getTimes(),
                  paramTModel);
         paramTModel.insertRow(satRow, 5);
      }
      else
      {
         paramTModel.removeRow(satRow);
      }
   }

   /**
     * Method that adds or removes the B-Glukos row from the observation table.
     * The row is added if the B-Glukos checkbox is selected.
     */
   public void glukosCh()
   {
      if (glukosCB.isSelected())
      {
         if (glukosRow == null)
            glukosRow = new FloatObservationTableRow(recordId, 5,
                  dbcon, lg, "B-Glukos", 0, 30, paramTModel.getTimes(),
                  paramTModel);
         paramTModel.insertRow(glukosRow, 6);
      }
      else
      {
         paramTModel.removeRow(glukosRow);
      }
   }

   /**
     * Method that adds or removes the VAS row from the observation table.
     * The row is added if the VAS checkbox is selected.
     */
   public void vasCh()
   {
      if (vasCB.isSelected())
      {
         if (vasRow == null)
            vasRow = new StdObservationTableRow(recordId, 6,
                  dbcon, lg, "VAS", 0, 10, paramTModel.getTimes(), paramTModel);
         paramTModel.insertRow(vasRow, 7);
      }
      else
      {
         paramTModel.removeRow(vasRow);
      }
   }

   /**
     * Methtod that adds or removes the RLS row from the observation table.
     * The row is added if the RLS checkbox is selected.
     */
   public void rlsCh()
   {
      if (rlsCB.isSelected())
      {
         if (rlsRow == null)
            rlsRow = new RLSObservationTableRow(recordId, 7, dbcon, lg,
               paramTModel.getTimes());
         paramTModel.insertRow(rlsRow, 8);
      }
      else
      {
         paramTModel.removeRow(rlsRow);
      }
   }
 
   /**
     * Method that adds or removes the Glascow Coma Scale rows from the
     * observation table. The rows are added if the GCS checkbox is selected.
     */
   public void gcsCh()
   {
      if (gcsCB.isSelected())
      {
         if (gcsModel == null)
            gcsModel = new GCSModel(recordId, dbcon, lg,paramTModel.getTimes());
         paramTModel.insertRow(gcsModel.getRowModel(0), 9);
         paramTModel.insertRow(gcsModel.getRowModel(1), 10);
         paramTModel.insertRow(gcsModel.getRowModel(2), 11);
      }
      else
      {
         if (gcsModel == null)
            return;
         paramTModel.removeRow(gcsModel.getRowModel(0));
         paramTModel.removeRow(gcsModel.getRowModel(1));
         paramTModel.removeRow(gcsModel.getRowModel(2));
      }
   }

   /**
     * Method that adds or removes the Pupil rows from the observation tale.
     * The rows are added if the Pupil checkbox is selected.
     */
   public void eyeCh()
   {
      if (eyeCB.isSelected())
      {
         if (eyeModel == null)
            eyeModel = new EyeModel(recordId, dbcon, lg,paramTModel.getTimes());
         paramTModel.insertRow(eyeModel.getRowModel(0), 12);
         paramTModel.insertRow(eyeModel.getRowModel(1), 13);
      }
      else
      {
         paramTModel.removeRow(eyeModel.getRowModel(0));
         paramTModel.removeRow(eyeModel.getRowModel(1));
      }
   }

   /**
     * Method that adds or removes the Body position row from the observation
     * table. The row is added if the Body position checkbox is selected.
     */
   public void bodyposCh()
   {
      if (bodyposCB.isSelected())
      {
         if (bodyposRow == null)
            bodyposRow = new BodyObservationTableRow(recordId, dbcon, lg,
                  paramTModel.getTimes());
         paramTModel.insertRow(bodyposRow, 14);
      }
      else
      {
         paramTModel.removeRow(bodyposRow);
      }
   }

} 
