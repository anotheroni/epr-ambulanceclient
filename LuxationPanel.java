import javax.swing.*;
import java.awt.*;

/**
  * Class defining the contents of the Luxation PopupPanel.
  *
  * @version 20030315
  * @author Oskar Nilsson
  */
public class LuxationPanel extends JPanel
{
   private Insets ins;

   private LuxationImage luxationImg = null;
   
   /**
     * Constructor, creates the GUI.
     * @param op A reference to the ObservationsPane.
     * @param op_l A reference to the ObservationsPane action listener.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param recordId Id of the current record.
     * @param isEditable True if the record is editable, false if it isn't.
     */
   public LuxationPanel (ObservationsPane op, ObservationsPane_Listener op_l,
         DB2Connect dbcon, LogHandler lg, int recordId, boolean isEditable)
   {
      super();

      ins = new Insets(2, 4, 2, 4);
      
      luxationImg = new LuxationImage(this, dbcon, lg, recordId, isEditable);
      luxationImg.setBounds(15,15,124,350);

      JButton closeBt = new JButton("OK");
      closeBt.setBounds(115,365,35,20);
      closeBt.setMargin(ins);
      closeBt.addActionListener(op_l);
      closeBt.setActionCommand("closeluxationbt");

      setLayout(null);
      setSize(155,390);
      setBorder(BorderFactory.createTitledBorder("Markera Luxation"));

      add(luxationImg);
      add(closeBt);
   }

   /**
     * Method to check if the user has selected any area in the popup.
     * @return true if an area is selected else false.
     */
   public boolean getSelected()
   {
      return luxationImg.hasMarked();
   }

   /**
     * Method that saves that data to the database.
     */
   public void save()
   {
      luxationImg.save();
   }
}
