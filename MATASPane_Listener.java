import java.awt.event.*;

/**
  * Class implementing an action listener for the MATAS pane.
  *
  * @version 20030113
  * @author Oskar Nilsson
  */
public class MATASPane_Listener implements ActionListener
{

   //-------------------Variables----------------------

   private MATASPane mp;

   //------------------Constructors--------------------

   public MATASPane_Listener(MATASPane mp)
   {
      this.mp = mp;
   }

   //--------------------Methods-----------------------

   public void actionPerformed(ActionEvent action)
   {
      String command = action.getActionCommand();
      if (command.equals("groupchange")) {
         mp.diagnosisGroupChanged();
      }
   }

} //class
