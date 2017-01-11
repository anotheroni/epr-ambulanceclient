import javax.swing.*;
import java.awt.event.*;

/**
  * Class representing the synchronization dialog.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
public class SynchronizeDialog extends JDialog implements AdminDialog
{

   private AmbulanceRecord ar;
   private DB2Connect dbcon;
   private AmbulanceClientUpdate acp = null;
   private LogHandler lg;
  
   private JButton okBt; 
   private JTextArea msgTA;

   /**
     * Constructor, creates the dialog and initializes the local variables.
     * @param ar A reference to the main frame.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     */
   public SynchronizeDialog (AmbulanceRecord ar, DB2Connect dbcon,
         LogHandler lg)
   {
      super(ar, "Synkronisera med servern");
      setSize(350, 180);
      setResizable(false);
      setLocation(ar.getX()+25, ar.getY()+50);
      getContentPane().setLayout(null);
 
      this.ar = ar;
      this.dbcon = dbcon;
      this.lg = lg;

      Dialog_Listener listener = new Dialog_Listener(this);
 
      msgTA = new JTextArea();
      msgTA.setEditable(false);
      JScrollPane msgScrollPane = new JScrollPane(msgTA);
      msgScrollPane.setBounds(5,5,335,120);

      okBt = new JButton("Avbryt");
      okBt.setBounds(235,130,100,25);
      okBt.setActionCommand("ok");
      okBt.addActionListener(listener);

      getContentPane().add(msgScrollPane);
      getContentPane().add(okBt);
      
      try {
         acp = new AmbulanceClientUpdate(lg, this, dbcon);
      } catch (Exception e) {
         msgTA.append(e.getMessage());
      }

      show();
 
   }

   /**
     * Method that adds text to the text area in the dialog.
     * @param msg The text to add.
     */
   public void setMessage(String msg)
   {
      msgTA.append(msg + "\n");
   }

   /**
     * Method called by AmbulanceClientUpdate when synchronization is complete.
     */
   public void syncComplete()
   {
      okBt.setText("Ok");
   }
   
   /**
     * Method that starts the sybchronization again. Shows the dialog again
     * and tries to synchronize. You only need to call this method when
     * synchronizing a second time with the same SynchronizeDialog object.
     */
   public void startSynchronization()
   {
      msgTA.setText("");
      okBt.setText("Avbryt");
      show();
      if (acp == null || acp.getTerminateStatus())
      {
         try {
            acp = new AmbulanceClientUpdate(lg, this, dbcon);
         } catch (Exception e) {
            msgTA.append(e.getMessage());
            lg.addLog (new Log (e.getMessage (),
                     "SynchronizeDialog/startSynchronization", ""));
         }
      }
   }

   /**
     * Method called by the system when the dialog is closed.
     */
   protected void processWindowEvent(WindowEvent e)
   {
      if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING)
      {
         if (acp != null)
            acp.terminate();
         hide();
      }
   }

   /**
     * Method called by the action listener when the ok button is pressed.
     */
   public void okPressed()
   {
      if (acp != null)
         acp.terminate();
      hide();
   }

   /**
     * Method called by the action listener when the cancel button is pressed.
     * Not used, needed when implementing the AdminDialog interface.
     */
   public void cancelPressed()
   {

   }
   
}
