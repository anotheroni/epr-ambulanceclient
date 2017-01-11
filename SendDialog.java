import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
  * Dialog that shows an active image.
  *
  * @version 20030422
  * @author Oskar Nilsson
 */
public class SendDialog extends JDialog
{
    private AntennaPanel antennaPanel;

    private JLabel messageLbl;
    private JButton cancelBt;

    /**
      * Constructor, creates the panel and starts the image thread.
      * @param parent A reference to the parent dialog.
      * @param title Title string for the dialog.
      * @param message Message string to show in the dialog.
      */
    public SendDialog(AmbulanceRecord parent, String title, String message,
          LogHandler lg, DB2Connect dbcon, int[] t)
    {
        super(parent, title, true);
        setSize(300,110);
        setResizable(false);

        ActionListener listener = new SendDialog_Listener(this);
        
        setLocation(parent.getX() + 50, parent.getY() + 50);
        
        getContentPane().setLayout(null);
        
        messageLbl = new JLabel(message);
        
        JPanel labelPanel = new JPanel();
        labelPanel.setBounds(70, 15, 220, 25);
        labelPanel.add(messageLbl);

        cancelBt = new JButton("Avbryt");
        cancelBt.setBounds(140, 45, 80, 25);
        cancelBt.addActionListener(listener);
        cancelBt.setActionCommand("cancel");

        getContentPane().add(labelPanel);
        getContentPane().add(cancelBt);

        antennaPanel = new AntennaPanel(lg);
        antennaPanel.setBounds(20, 20, 48, 48);

        getContentPane().add(antennaPanel);
        parent.setSendDialog(this);

        try {
           new AmbulanceClient(lg, parent, dbcon, t);
        } catch (Exception e) {
           setMessage("Sändning misslyckades");
           addOk();
        }

        show();
   }

    /**
      * Method that closes the dialog ans stops the image thread.
      */
    public void destroy()
    {
        antennaPanel.destroy();
        this.dispose();
    }

    /**
      * Method that updates the message in the dialog.
      * @param msg The new message.
      */
    public void setMessage(String msg)
    {
        messageLbl.setText(msg);
    }

    /**
      * Method that changes the canel button to an ok button.
      */
    public void addOk()
    {
        cancelBt.setText("OK");
        antennaPanel.destroy();
    }
}
