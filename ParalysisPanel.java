import javax.swing.*;
import java.awt.*;

/**
  * Class representing the paralysis status panel.
  *
  * @version 20030330
  * @author Oskar Nilsson
  */
public class ParalysisPanel extends JPanel
{
   private ObservationsPane_Listener listener;

   private ImageIcon paralysisImgsLeft[];
   private ImageIcon paralysisImgsRight[];
 
   private JList leftSide, rightSide;

   /**
     * Constructor, initializes the local variables and creates the UI.
     * @param listener Action listener used by the buttons.
     * @param paralysisRight The selected paralysis type on the right side.
     * @param paralysisLeft The selected paralysis type on the left side.
     * @param isEditable True if the panel is editable else false.
     */
   public ParalysisPanel(ObservationsPane_Listener listener,
         int paralysisRight, int paralysisLeft, boolean isEditable)
   {
      super();
      this.listener = listener;

      // Load the eye images
      String[] description = {"Ingen", "Arm", "Ben", "Sida"};
      paralysisImgsLeft = new ImageIcon[description.length];
      paralysisImgsRight = new ImageIcon[description.length];
      for (int i = 0; i < description.length; i++)
      {
         paralysisImgsLeft[i] = new ImageIcon("images/" + description[i] +
              "_left.gif");
         paralysisImgsLeft[i].setDescription("V. " + description[i]);
         paralysisImgsRight[i] = new ImageIcon("images/" + description[i] +
               "_right.gif");
         paralysisImgsRight[i].setDescription("H. " + description[i]);
      }

      JLabel leftLbl = new JLabel("Vänster sida");
      leftLbl.setBounds(130, 15, 90, 25);
      JLabel rightLbl = new JLabel("Höger sida");
      rightLbl.setBounds(10, 15, 90, 25);

      // Create the list
      ListRenderer renderer = new ListRenderer();
      renderer.setPreferredSize(new Dimension(90, 60));
 
      leftSide = new JList(paralysisImgsLeft);
      leftSide.setEnabled(isEditable);
      leftSide.setCellRenderer(renderer);
      leftSide.setSelectedIndex(paralysisLeft);
      JScrollPane leftPane = new JScrollPane(leftSide);
      leftPane.setBounds(130,45,110,245);
      rightSide = new JList(paralysisImgsRight);
      rightSide.setEnabled(isEditable);
      rightSide.setCellRenderer(renderer);
      rightSide.setSelectedIndex(paralysisRight);
      JScrollPane rightPane = new JScrollPane(rightSide);
      rightPane.setBounds(10,45,110,245);

      JButton paralysisCloseBt = new JButton("Ok");
      paralysisCloseBt.setBounds(205,300,35,20);
      paralysisCloseBt.setMargin(new Insets(2,4,2,4));
      paralysisCloseBt.addActionListener(listener);
      paralysisCloseBt.setActionCommand("closeparalysis");

      setLayout(null);
      setSize(250,325);
      setBorder(BorderFactory.createTitledBorder("Förlamning"));

      add(leftLbl);
      add(rightLbl);
      add(leftPane);
      add(rightPane);
      add(paralysisCloseBt);
   }

   /**
     * Method to get the selected index of the left side list.
     * @return Selected index of the left side list.
     */
   public int getLeftSide()
   {
      return leftSide.getSelectedIndex();
   }

   /**
     * Method to get the selected index of the right side list.
     * @return Selected index of the right side list.
     */
   public int getRightSide()
   {
      return rightSide.getSelectedIndex();
   }

   /**
     * Method to check if the user has selected a paralyzed area.
     * @return true if the patient is paralyzed.
     */
   public boolean isNotNormal()
   {
      return leftSide.getSelectedIndex() != 0 ||
         rightSide.getSelectedIndex() != 0;
   }

}
