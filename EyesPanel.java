import javax.swing.*;
import java.awt.*;

/**
  * Class representing the eye status panel.
  *
  * @version 20030125
  * @author Oskar Nilsson
  */
public class EyesPanel extends JPanel
{
   private ObservationsPane_Listener listener;

   private ImageIcon pupilImgs[];
 
   private JList leftEye, rightEye;

   public EyesPanel(ObservationsPane_Listener listener)
   {
      super();
      this.listener = listener;

      // Load the eye images
      String[] pupilSizes = {"Liten", "Normal", "Stor"};
      pupilImgs = new ImageIcon[pupilSizes.length];
      for (int i = 0; i < pupilSizes.length; i++)
      {
         pupilImgs[i] = new ImageIcon("images/" + pupilSizes[i] + ".gif");
         pupilImgs[i].setDescription(pupilSizes[i]);
      }

      JLabel leftLbl = new JLabel("Vänster");
      leftLbl.setBounds(10, 15, 70, 25);
      JLabel rightLbl = new JLabel("Höger");
      rightLbl.setBounds(130, 15, 70, 25);

      // Create the combo box
      ListRenderer renderer = new ListRenderer();
      renderer.setPreferredSize(new Dimension(80, 30));
 
      leftEye = new JList(pupilImgs);
      leftEye.setCellRenderer(renderer);
      leftEye.setSelectedIndex(1);
      JScrollPane leftPane = new JScrollPane(leftEye);
      leftPane.setBounds(10,45,100,95);
      rightEye = new JList(pupilImgs);
      rightEye.setCellRenderer(renderer);
      rightEye.setSelectedIndex(1);
      JScrollPane rightPane = new JScrollPane(rightEye);
      rightPane.setBounds(130,45,100,95);

      JButton eyeCloseBt = new JButton("Ok");
      eyeCloseBt.setBounds(205,155,35,20);
      eyeCloseBt.setMargin(new Insets(2,4,2,4));
      eyeCloseBt.addActionListener(listener);
      eyeCloseBt.setActionCommand("closeeyesbt");

      setLayout(null);
      setSize(250,180);
      setBorder(BorderFactory.createTitledBorder("Ögon status"));

      add(leftLbl);
      add(rightLbl);
      add(leftPane);
      add(rightPane);
      add(eyeCloseBt);
   }

   /**
     * Method to get the selected index of the left eye combo box.
     * @return Selected index of the left eye combo box.
     */
   public int getLeftEyeSize()
   {
      return leftEye.getSelectedIndex();
   }

   /**
     * Method to get the selected index of the right eye combo box.
     * @return Selected index of the right eye combo box.
     */
   public int getRightEyeSize()
   {
      return rightEye.getSelectedIndex();
   }

   /**
     * Method to check if the user has selected an eye size other than normal.
     * @return true if the pupil size is not normal.
     */
   public boolean isNotNormal()
   {
      return leftEye.getSelectedIndex() != 1 ||
         rightEye.getSelectedIndex() != 1;
   }

}
