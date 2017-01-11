import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
  * Class implementing the misc panel in a patient record.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class MiscPane extends JPanel
{

   private PatientRecord pr;
   private DB2Connect dbcon;
   private WordList wl;
   private LogHandler lg;
   private int recordId;
   private boolean isEditable;

   private boolean dbRecordExists;
   
   private JTextArea miscTA = null;

   private JButton statisticsBt;

   /**
     * Constructor, builds the panel.
     * @param pr A reference to the current patient record.
     * @param dbcon A refernece to the database.
     * @param wl The word list used by auto complete.
     * @param lg The log handler to report errors to.
     * @param dFont The font used to display labels.
     * @param recordId The id number of the current record.
     * @param isEditable True if the patient record is editable.
     */
   public MiscPane(PatientRecord pr, DB2Connect dbcon, WordList wl,
        LogHandler lg, Font dFont, int recordId, boolean isEditable)
   {
      super();

      this.pr = pr;
      this.dbcon = dbcon;
      this.wl = wl;
      this.lg = lg;
      this.recordId = recordId;
      this.isEditable = isEditable;

      MiscPane_Listener listener = new MiscPane_Listener(this, pr, dbcon,
           lg, isEditable);

      ResultSet rs = null;

      try {
         rs = dbcon.dbQuery("SELECT MISCTEXT FROM EPR.MISC WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            miscTA = new JTextArea(rs.getString("MISCTEXT"));
            dbRecordExists = true;
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "MiscPane/MiscPane",
                  "Read misc text"));
      }

      //--- Misc Textarea
      if (miscTA == null)
         miscTA = new JTextArea();

      miscTA.setLineWrap(true);
      miscTA.setWrapStyleWord(true);
      miscTA.setEnabled(isEditable);

      // Focus
      miscTA.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      miscTA.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      AutoComplete_Listener miscTAListener =
         new AutoComplete_Listener(miscTA, wl, pr.getUserId());
      miscTA.addKeyListener(miscTAListener);

      // Scroll
      JScrollPane miscScrollPane = new JScrollPane(miscTA);
      miscScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      miscScrollPane.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Övriga komentarer"),
               miscScrollPane.getBorder()));
      miscScrollPane.setBounds(5, 10, 580, 100);
      miscScrollPane.getVerticalScrollBar().setFocusable(false);

      //--- Traffic accident statistics
      statisticsBt = new JButton("Olycksstatistik");
      statisticsBt.setFont(dFont);
      statisticsBt.setBounds(5,120,120,25);
      statisticsBt.addActionListener(listener);
      statisticsBt.setActionCommand("statistics");

      setLayout(null);
      add(miscScrollPane);
      add(statisticsBt);
   }

   /**
     * Method called when tha record is closed.
     * Saves the state to the database.
     */
   public void savePane() //TODO only save when needed, add flag in listener...
   {
      if (!isEditable)  // No reason to save if the pane isn't editable
         return;
 
      if (dbRecordExists)
      {
         try {
            dbcon.dbQueryUpdate ("UPDATE EPR.MISC SET MISCTEXT = '" +
                  miscTA.getText() + "' WHERE RECORD_ID = " + recordId);
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "MiscPane/savePane",
                     "update misc text"));
         }
      }
      else if (miscTA.getText().length() > 0)   // Only save if there is text
      {
         try {
            dbcon.dbQueryUpdate ("INSERT INTO EPR.MISC (RECORD_ID, MISCTEXT) " +
                  "VALUES (" + recordId + ", '" + miscTA.getText() + "')"); 
            dbRecordExists = true;
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "MiscPane/savePane",
                     "Insert misc text"));
         }
      }

   }

   /**
     * Method that adds a comment to the end of the comments textarea.
     * @param comment The comment to add.
     */
   public void addComment (String comment)
   {
      if (miscTA.getCaretPosition () > 0)
         miscTA.append ("\n");
      miscTA.append (comment);
   }

}
