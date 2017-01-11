import javax.swing.*;
import javax.swing.table.TableColumn;
import java.util.Vector;
import java.sql.*;
import javax.swing.text.AbstractDocument;

/**
  * Class implementing the verify signature dialog that prompts the user for
  * names of the persons that has given medication.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class VerifySignatureDialog extends JDialog implements AdminDialog
{
   private AmbulanceRecord ar;
   private int recordId;
   private DB2Connect dbcon;
   private LogHandler lg;

   private boolean result = false;

   private VerifySignatureTableModel medTModel;
   private JTable medTable;
   
   private JTextField nameFld;

   /**
     * Constructor, initializes the local variables and creates the GUI.
     * @param ar The parent frame.
     * @param recordId The id of the record that is verified.
     */
   public VerifySignatureDialog(AmbulanceRecord ar, int recordId,
         DB2Connect dbcon, LogHandler lg)
   {
      super(ar, "Verifiera signaturer", true);
      setSize(300,300);
      setLocation(ar.getX()+50, ar.getY()+50);

      this.ar = ar;
      this.recordId = recordId;
      this.dbcon = dbcon;
      this.lg = lg;

      Dialog_Listener listener = new Dialog_Listener(this);
      TableColumn col;

      getContentPane().setLayout(null);

      medTModel = new VerifySignatureTableModel(recordId, dbcon, lg);
      medTable = new JTable(medTModel);
      JScrollPane medScrollPane = new JScrollPane(medTable);
      medScrollPane.setBounds(8, 10, 280, 200);

      // Time column
      col = medTable.getColumnModel().getColumn(0);
      col.setCellRenderer(new DateTableCellRenderer(lg));

      JLabel nameLbl = new JLabel("Namn");
      nameLbl.setBounds(10, 215, 40, 25);

      nameFld = new JTextField();
      nameFld.setBounds(50, 215, 240, 25);
      ((AbstractDocument)nameFld.getDocument()).
         setDocumentFilter(new LimitedTextFilter(64));

      JButton okBt = new JButton("Ok");
      okBt.setBounds(100, 245, 80, 25);
      okBt.setActionCommand("ok");
      okBt.addActionListener(listener);

      JButton cancelBt = new JButton("Avbryt");
      cancelBt.setBounds(200, 245, 80, 25);
      cancelBt.setActionCommand("quit");
      cancelBt.addActionListener(listener);

      getContentPane().add(medScrollPane);
      getContentPane().add(nameLbl);
      getContentPane().add(nameFld);
      getContentPane().add(okBt);
      getContentPane().add(cancelBt);
   }

   /**
     * Method that shows the dialog. When the dialog is closed the result is
     * returned.
     * @return true if all medications are signed and everything is ok.
     * false if not all medications are signed or if something went wrong.
     */
   public boolean showDialog()
   {
      show();
      return result;
   }

   /**
     * Method called when the ok button is pressed.
     */
   public void okPressed()
   {
      if (medTModel.getRowCount() == 0)
      {
         dispose();
         result = true;
         return;
      }
      
      if (nameFld.getText().length() <= 0)
      {
         JOptionPane.showMessageDialog(this,
                     "Ett namn måste anges",
                     "Inget namn", JOptionPane.ERROR_MESSAGE);
         return;
      }

      Vector selected;
      selected = medTModel.getSelectedRows();
      if (selected.size() == 0)
      {
         JOptionPane.showMessageDialog(this,
                     "Du måste välja minst ett medicinerings tillfälle",
                     "Inga mediciner valda", JOptionPane.ERROR_MESSAGE);
         return;
      }

      for (int i=0 ; i < selected.size() ; i++)
      {
         try {
            dbcon.dbQueryUpdate ("INSERT INTO EPR.MEDICATION_GIVEN_BY (NAME, " +
                  "GIVEN_MEDICATION_ID) VALUES ('" + nameFld.getText() +
                  "', " + selected.elementAt(i) + ")");
         } catch (SQLException e) {
             lg.addLog(new Log(e.getMessage(),
                    "VerifySignatureDialog/okPressed",
                    "Insert into medication_given_by"));
         }
      }
      medTModel.updateTable();

      // If the table is empty close the dialog
      if (medTModel.getRowCount() <= 0)
      {
         result = true;
         dispose();
      }
   }

   /**
     * Method called when the cancel button is pressed. Closes the dialog.
     */
   public void cancelPressed()
   {
      result = false;
      dispose();
   }
}
