import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;

/**
  * Class representing the delegation dialog.
  *
  * @author Oskar Nilsson
  * @version 20030118
  */
public class DelegationDialog extends JDialog implements AdminDialog
{
   private JTextField nameFld;

   private String result = null;
   
   private AmbulanceRecord ar;
 
   /**
     * Constuctor, creates the add ambulance dialog.
     * @param ar A reference to AmbulanceRecord.
     * @param medVec A Vector containing the names of the medicines that need a
     * delegation.
     */
   public DelegationDialog(AmbulanceRecord ar, Vector medVec)
   {
      super((Frame)ar, "Ange delegerande läkare", true);
      setSize(220, 240);
      setResizable(false);
      
      this.ar = ar;
 
      int numTypes = 0;
      ListEntry types[] = null;

      Dialog_Listener listener = new Dialog_Listener(this);

      JLabel infoLbl = new JLabel("Följande mediciner kräver en delegation:");
      infoLbl.setFont(new Font("Default", Font.PLAIN, 10));
      infoLbl.setBounds(10,10,200,25);

      JList medList = new JList(medVec);

      JScrollPane medScrollPane = new JScrollPane(medList);
      medScrollPane.setBounds(10,40,200,100);
            
      JLabel nameLbl = new JLabel("Läkare");
      nameLbl.setBounds(10, 150, 70, 25);
      nameFld = new JTextField(10);
      nameFld.setBounds(80, 150, 115, 25);

      JButton okBt = new JButton("OK");
      okBt.setBounds(30, 185, 70, 25);
      okBt.setMargin(new Insets(2,4,2,4));
      okBt.addActionListener(listener);
      okBt.setActionCommand("ok");

      JButton cancelBt = new JButton("Avbryt");
      cancelBt.setBounds(110, 185, 70, 25);
      cancelBt.addActionListener(listener);
      cancelBt.setActionCommand("quit");

      JPanel panel = new JPanel();
      panel.setLayout(null);
      panel.setBounds(0, 0, 200, 85);

      panel.add(infoLbl);
      panel.add(medScrollPane);
      panel.add(nameLbl);
      panel.add(nameFld);
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
      if (nameFld.getText().length() < 1)
      {
         JOptionPane.showMessageDialog(this, "En läkare måste anges");
         return;
      }
      result = nameFld.getText();
      this.hide();
   }

   /**
     * Method called by the action listener when cancel is pressed.
     */
   public void cancelPressed()
   {
      result = null;
      this.hide();
   }

   /**
     * Method called to get the name that was entered into the text field.
     * @return The contents of the doctor text field.
     */
   public String getResult()
   {
      return result;
   }
}
