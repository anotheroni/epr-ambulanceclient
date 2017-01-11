import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/**
  * Class implementing an active image.
  *
  * @version 20030315
  * @author  Oskar Nilsson
  */
public class AntennaPanel extends JPanel implements Runnable
{
   private LogHandler lg;

   private Thread t;
   private boolean go_on = true;

   private String imageFile = "./images/Antenna.gif";
   private Image image;

   private int step = 0;

   /**
    * Constructor, loades the image and starts the image update thread.
    */
   public AntennaPanel(LogHandler lg)
   {
      image = Toolkit.getDefaultToolkit().getImage(imageFile);

      // Create a tracker that makes sure all images are loaded.
      MediaTracker mt = new MediaTracker(this);

      mt.addImage(image, 1);

      try {
         mt.waitForAll();
      } catch(InterruptedException e) {
         lg.addLog(new Log(e.getMessage(),
                  "AntennaPanel/AntennaPanel",
                  "Loading images"));
      }

      t = new Thread(this);
      t.start();
   }

   /**
    * Method that paints the panel.
    * @param g The context to paint in.
    */
   public void paint(Graphics g)
   {
      g.setColor(this.getBackground());
      g.fillRect(0, 0, 48, 48);
      g.drawImage(image, 0, 0, this);
      g.setColor(Color.black);
      if (step > 0)
         g.drawArc(26, 5, 5, 15, 45, -90);
      if (step > 1)
         g.drawArc(32, 3, 6, 19, 45, -90);
      if (step > 2)
         g.drawArc(40, 1, 7, 23, 45, -90);
   }

   /**
    * Thread method that runs the tread. Repaints the image periodicaly.
    */
   public void run()
   {
      try {
         while(go_on) {
            step++;
            if (step > 3)
               step = 0;
            repaint();
            t.sleep(500);
         }
      } catch (Exception e) {
         lg.addLog(new Log(e.getMessage(),
                  "AntennaPanel/run",
                  "Run"));
      }
   }

   /**
    * Method that stops the thread.
    */
   public void destroy()
   {
      go_on = false;
   }
}
