import javax.swing.*;
import java.awt.*;

/**
  * Class defining the contents of the Fracture PopupPanel.
  *
  * @version 20030314
  * @author Oskar Nilsson
  */
public class FracturePanel extends JPanel
{
   private boolean isEditable;
   
   private Insets ins;

   private FractureImage fractureImage = null;
   
   private JLabel leftArmLbl;
   private JToggleButton leftArmYesBt;
   private JToggleButton leftArmNoBt;

   private JLabel rightArmLbl;
   private JToggleButton rightArmYesBt;
   private JToggleButton rightArmNoBt;

   private JLabel leftLegLbl;
   private JToggleButton leftLegYesBt;
   private JToggleButton leftLegNoBt;

   private JLabel rightLegLbl;
   private JToggleButton rightLegYesBt;
   private JToggleButton rightLegNoBt;

   /**
     * Constructor, creates the GUI.
     * @param op A reference to the ObservationsPane.
     * @param op_l A reference to the ObservationsPane action listener.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param recordId Id of the currrent record.
     * @param isEditable True if the record is editable, false if it isn't.
     */
   public FracturePanel (ObservationsPane op, ObservationsPane_Listener op_l,
         DB2Connect dbcon, LogHandler lg, int recordId, boolean isEditable)
   {
      super();
      this.isEditable = isEditable;
      
      ins = new Insets(2,4,2,4);

      leftArmLbl = new JLabel("perf. puls");
      leftArmLbl.setBounds(215,80,60,25);
      leftArmLbl.setVisible(false);
 
      leftArmYesBt = new JToggleButton("ja");
      leftArmYesBt.setBounds(215,105,35,25);
      leftArmYesBt.setMargin(ins);
      leftArmYesBt.setVisible(false);

      leftArmNoBt = new JToggleButton("nej");
      leftArmNoBt.setBounds(250,105,35,25);
      leftArmNoBt.setMargin(ins);
      leftArmNoBt.setVisible(false);

      rightArmLbl = new JLabel("perf. puls");
      rightArmLbl.setBounds(5,80,60,25);
      rightArmLbl.setVisible(false);
 
      rightArmYesBt = new JToggleButton("ja");
      rightArmYesBt.setBounds(5,105,35,25);
      rightArmYesBt.setMargin(ins);
      rightArmYesBt.setVisible(false);

      rightArmNoBt = new JToggleButton("nej");
      rightArmNoBt.setBounds(40,105,35,25);
      rightArmNoBt.setMargin(ins);
      rightArmNoBt.setVisible(false);

      leftLegLbl = new JLabel("perf. puls");
      leftLegLbl.setBounds(215,250,60,25);
      leftLegLbl.setVisible(false);
 
      leftLegYesBt = new JToggleButton("ja");
      leftLegYesBt.setBounds(215,275,35,25);
      leftLegYesBt.setMargin(ins);
      leftLegYesBt.setVisible(false);

      leftLegNoBt = new JToggleButton("nej");
      leftLegNoBt.setBounds(250,275,35,25);
      leftLegNoBt.setMargin(ins);
      leftLegNoBt.setVisible(false);

      rightLegLbl = new JLabel("perf. puls");
      rightLegLbl.setBounds(5,250,60,25);
      rightLegLbl.setVisible(false);
 
      rightLegYesBt = new JToggleButton("ja");
      rightLegYesBt.setBounds(5,275,35,25);
      rightLegYesBt.setMargin(ins);
      rightLegYesBt.setVisible(false);

      rightLegNoBt = new JToggleButton("nej");
      rightLegNoBt.setBounds(40,275,35,25);
      rightLegNoBt.setMargin(ins);
      rightLegNoBt.setVisible(false);

      JButton closeBt = new JButton("OK");
      closeBt.setBounds(260,365,35,20);
      closeBt.setMargin(ins);
      closeBt.addActionListener(op_l);
      closeBt.setActionCommand("closefracturebt");

      fractureImage = new FractureImage(this, dbcon, lg, recordId, isEditable);
      fractureImage.setBounds(90,15,124,350);

      setLayout(null);
      setSize(305,390);
      setBorder(BorderFactory.createTitledBorder("Markera fraktur"));

      add(fractureImage);
      add(leftArmLbl);
      add(leftArmYesBt);
      add(leftArmNoBt);
      add(rightArmLbl);
      add(rightArmYesBt);
      add(rightArmNoBt);
      add(leftLegLbl);
      add(leftLegYesBt);
      add(leftLegNoBt);
      add(rightLegLbl);
      add(rightLegYesBt);
      add(rightLegNoBt);
      add(closeBt);
   }

   /**
     * Method that displays/hide the extremity chooice buttons.
     * Called by fractureImage when a fracture in an extremity is marked.
     * @param extr The extremity that is choosen.
     * @param show True if the buttons should be displayed,
     * false if they should hidden.
    */
   public void enableExtr(int extr, boolean show)
   {
      switch (extr)
      {
         case 0:  // Right arm
            rightArmLbl.setVisible(show);
            rightArmYesBt.setVisible(show);
            rightArmNoBt.setVisible(show);
            break;
         case 1: // Left arm
            leftArmLbl.setVisible(show);
            leftArmYesBt.setVisible(show);
            leftArmNoBt.setVisible(show);
            break;
         case 2: // Right leg
            rightLegLbl.setVisible(show);
            rightLegYesBt.setVisible(show);
            rightLegNoBt.setVisible(show);
            break;
         case 3:  // Left leg
            leftLegLbl.setVisible(show);
            leftLegYesBt.setVisible(show);
            leftLegNoBt.setVisible(show);
            break;
      }
   }

   /**
     * Method that displays the extremity chooice buttons and sets their value.
     * Called when fractureImage is created.
     * @param extr The extremity that is to be displayed.
     * @param val True if yes is choosen, false if no is choosen.
     */
   public void setExtr(int extr, boolean val)
   {
      switch (extr)
      {
         case 0:  // Right arm
            rightArmLbl.setVisible(true);
            rightArmYesBt.setVisible(true);
            rightArmYesBt.setSelected(val);
            rightArmYesBt.setEnabled(isEditable);
            rightArmNoBt.setVisible(true);
            rightArmNoBt.setSelected(!val);
            rightArmNoBt.setEnabled(isEditable);
            break;
         case 1: // Left arm
            leftArmLbl.setVisible(true);
            leftArmYesBt.setVisible(true);
            leftArmYesBt.setSelected(val);
            leftArmYesBt.setEnabled(isEditable);
            leftArmNoBt.setVisible(true);
            leftArmNoBt.setSelected(!val);
            leftArmNoBt.setEnabled(isEditable);
            break;
         case 2: // Right leg
            rightLegLbl.setVisible(true);
            rightLegYesBt.setVisible(true);
            rightLegYesBt.setSelected(val);
            rightLegYesBt.setEnabled(isEditable);
            rightLegNoBt.setVisible(true);
            rightLegNoBt.setSelected(!val);
            rightLegNoBt.setEnabled(isEditable);
            break;
         case 3:  // Left leg
            leftLegLbl.setVisible(true);
            leftLegYesBt.setVisible(true);
            leftLegYesBt.setSelected(val);
            leftLegYesBt.setEnabled(isEditable);
            leftLegNoBt.setVisible(true);
            leftLegNoBt.setSelected(!val);
            leftLegNoBt.setEnabled(isEditable);
            break;
      }
   }

   /**
     * Method to check the perferial puls buttons.
     * @param extr 0 for right arm, 1 for left arm, 2 for right leg,
     * 3 for left leg.
     * @return y If yes is selected, n if no is selected, null if no button
     * is selected.
     */
   public String getExtr(int extr)
   {
      switch (extr)
      {
         case 0:
            if (rightArmYesBt.isSelected())
               return "'t'";
            if (rightArmNoBt.isSelected()) 
               return "'f'";
            break;
         case 1:
            if (leftArmYesBt.isSelected())
               return "'t'";
            if (leftArmNoBt.isSelected())
               return "'f'";
            break;
         case 2:
            if (rightLegYesBt.isSelected())
               return "'t'";
            if (rightLegNoBt.isSelected())
               return "'f'";
            break;
         case 3:
            if (leftLegYesBt.isSelected())
               return "'t'";
            if (leftLegNoBt.isSelected())
               return "'f'";
            break;
         default:
            return null;
      }
      return null;
   }

   /**
     * Method to check if the user has selected any area in the popup.
     * @return true if an area is selected else false.
     */
   public boolean getSelected()
   {
      return fractureImage.hasMarked();
   }

   /**
     * Method that saves that data to the database.
     */
   public void save()
   {
      fractureImage.save();      
   }

}
