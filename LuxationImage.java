import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.sql.*;

/**
  * Class that constructs an image with clickable areas.
  *
  * @version 20030904
  * @author Oskar Nilsson
  */
class LuxationImage extends JComponent implements MouseListener
{
   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;

   private boolean isInDb = false;

   private final String baseName = "images/skeleton.gif";
   private final String mapName = "images/skeleton_lux.map";

   private LuxationPanel parent;

   private Image baseImage;
   private int baseImageXsize, baseImageYsize;
   private double basemapXscale, basemapYscale;

   private ImageMap imap;
   private boolean selected[] = {
      false, false, false, false, false, false, false, false, false, false,
      false
   };

   /**
     * Creates a luxation clickable image.
     * @param parent A reference to the parent panel that conatins the image.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param recordId Id of the current record.
     * @param isEditable True if the record is editable, false if it isn't.
     */
   public LuxationImage(LuxationPanel parent, DB2Connect dbcon, LogHandler lg,
         int recordId, boolean isEditable)
   {
      super();
      this.parent = parent;
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;

      imap = new ImageMap(mapName, lg);

      Image tempImage;  // Used to store the image that is sent to the filter.
      Color transColor = new Color(51,255,51); // The color to make transparent

      // Create a filter to make a color transparent
      TransparentFilter filter = new TransparentFilter(transColor);

      // Create a tracker that makes sure all images are loaded.
      MediaTracker mt = new MediaTracker(this);

      tempImage = Toolkit.getDefaultToolkit().getImage(baseName);
      baseImage = createImage(new FilteredImageSource(tempImage.getSource(),
              filter));
      mt.addImage(baseImage,1);
    
      ResultSet rs;
      byte res;
      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.LUXATION WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            isInDb = true;
            for (int i=1 ; i <= 10 ; i++)
            {
               res = rs.getByte(i+1);
               if (res == 116)   // 't'
                  selected[i] = true;
            }
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "LuxationImage/LuxationImage",
                  "Read luxation"));
      }

      // Wait for all images to load  
      try {
         mt.waitForAll();
      } catch(InterruptedException e) {
         lg.addLog(new Log(e.getMessage(),
                  "LuxationImage/LuxationImage",
                  "Loading imges"));
      }

      baseImageXsize = baseImage.getWidth(this);
      baseImageYsize = baseImage.getHeight(this);
      basemapXscale = baseImageXsize / imap.getWidth();
      basemapYscale = baseImageYsize / imap.getHeight();
      
      setSize(baseImageXsize, baseImageYsize);
      if (isEditable)
         addMouseListener(this);
   }

   /**
     * Method called when the compenent is painted.
     */
   public void paint(Graphics page)
   {
      page.drawImage(baseImage, 0, 0, 125, 350, this);

      page.setColor(Color.RED);

      if (selected[1])  // Right sholder
         page.fillOval(5, 55, 15, 15);
      if (selected[2])  // Right elbow
         page.fillOval(1, 120, 15, 15);
      if (selected[3])  // Left sholder
         page.fillOval(75, 55, 15, 15);
      if (selected[4])  // Left elbow
         page.fillOval(80, 122, 15, 15);
      if (selected[5])  // Right hipp
         page.fillOval(25, 170, 15, 15);
      if (selected[6])  // Right knee
         page.fillOval(25, 240, 15, 15);
      if (selected[7])  // Right foot
         page.fillOval(30, 315, 15, 15);
      if (selected[8])  // Left hipp
         page.fillOval(65, 170, 15, 15);
      if (selected[9])  // Left knee
         page.fillOval(60, 240, 15, 15);
      if (selected[10]) // Left foot
         page.fillOval(60, 315, 15, 15);
   }

   /**
     * Method to check if any areas has been selected.
     * @return True if an area has been selected. False if no area has been
     * seleced.
     */
   public boolean hasMarked()
   {
      for(int i=0 ; i < selected.length ; i++)
      {
         if (selected[i])
            return true;
      }

      return false;
   }
 
   /**
     * Method that saves the data in the database.
     */
   public void save()
   {
      if (isInDb)
      {
         StringBuffer strBuf = new StringBuffer(200);
         strBuf.append("UPDATE EPR.LUXATION SET RSH = ");
         strBuf.append(selected[1] ? "'t'" : "'f'");
         strBuf.append(", REL = ").append(selected[2] ? "'t'" : "'f'");
         strBuf.append(", LSH = ").append(selected[3] ? "'t'" : "'f'");
         strBuf.append(", LEL = ").append(selected[4] ? "'t'" : "'f'");
         strBuf.append(", RHI = ").append(selected[5] ? "'t'" : "'f'");
         strBuf.append(", RNE = ").append(selected[6] ? "'t'" : "'f'");
         strBuf.append(", RFO = ").append(selected[7] ? "'t'" : "'f'");
         strBuf.append(", LHI = ").append(selected[8] ? "'t'" : "'f'");
         strBuf.append(", LNE = ").append(selected[9] ? "'t'" : "'f'");
         strBuf.append(", LFO = ").append(selected[10] ? "'t'" : "'f'");
         strBuf.append(" WHERE RECORD_ID = ").append(recordId);
         try {
            dbcon.dbQueryUpdate (strBuf.toString());
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "LuxationImage/save",
                     "Update luxation"));
         }
      }
      else  // Not in database
      {
         StringBuffer strBuf = new StringBuffer(200);
         strBuf.append("INSERT INTO EPR.LUXATION (RECORD_ID, RSH, REL, " +
               "LSH, LEL, RHI, RNE, RFO, LHI, LNE, LFO) VALUES(");
         strBuf.append(recordId);
         strBuf.append(", ").append(selected[1] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[2] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[3] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[4] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[5] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[6] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[7] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[8] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[9] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[10] ? "'t'" : "'f'");
         strBuf.append(")");
         try {
            dbcon.dbQueryUpdate (strBuf.toString());
            isInDb = true;
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "LuxationImage/save",
                     "Insert luxation"));
         }
      }
   }

   // ---- Mouse Listener ----

   /**
     * Method called by the system when a mouse button is pressed it
     * check which area of the image the click is in and update the state
     * of that area.
     * @param e The mouse pressed event.
     */
   public void mousePressed(MouseEvent e)
   {
      int value = 0;
      try {
         value = (int) imap.getValue((int)(e.getX() / basemapXscale),
            (int)(e.getY() / basemapYscale)) ;
      } catch (ArrayIndexOutOfBoundsException f) {
         value = 0;
      }
      if (value != 0)
      {
         selected[value] = !selected[value];
         repaint();
      }
   }

   /// Empty method
   public void mouseReleased(MouseEvent e) { }
   /// Empty method
   public void mouseEntered(MouseEvent e) { }
   /// Empty method
   public void mouseExited(MouseEvent e) { }
   /// Empty method
   public void mouseClicked(MouseEvent e) { }

}
