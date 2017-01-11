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
class FractureImage extends JComponent implements MouseListener
{

   private DB2Connect dbcon;
   private LogHandler lg;
   private int recordId;
   private boolean isInDb = false;

   private final String baseName = "images/skeleton.gif";
   private final String redName = "images/skeleton_red.gif";
   private final String mapName = "images/skeleton.map";

   private FracturePanel parent;

   private Image baseImage;
   private int baseImageXsize, baseImageYsize;
   private double basemapXscale, basemapYscale;
   private Image redImage;

   private ImageMap imap;
   private boolean selected[] = {
      false, false, false, false, false, false, false, false, false, false,
      false, false, false, false, false, false, false, false, false, false
   };

   private boolean extr[] = {false, false, false, false};
 
   /**
     * Creates a standard clickimage object.
     * @param parent A reference to the parent panel.
     * @param dbcon A reference to the database.
     * @param recordId ID if the current record.
     * @param lg The log handler to report errors to.
     * @param isEditable True if the record is editable, false if it isn't.
    */
   public FractureImage(FracturePanel parent, DB2Connect dbcon, LogHandler lg,
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

      tempImage = Toolkit.getDefaultToolkit().getImage(redName);
      redImage = createImage(new FilteredImageSource(tempImage.getSource(),
              filter));
      mt.addImage(redImage,1);

      ResultSet rs;
      byte res;
      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.FRACTURE WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            isInDb = true;
            for (int i=1 ; i <= 19 ; i++)
            {
               res = rs.getByte(i+1);
               if (res == 116)   // 't'
                  selected[i] = true;
            }
            for (int i=0 ; i < 4 ; i++)
            {
               res = rs.getByte(i+21);
               if (res == 116)   // 't'
               {
                  extr[i] = true;
                  parent.setExtr(i, true);
               }
               else if (res == 102) // 'f'
               {
                  extr[i] = true;
                  parent.setExtr(i, false);
               }
               // false is default no need to set
            }
         }
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "FractureImage/FractureImage",
                  "Read fracture"));
      }

      try {    // Wait for images to load
         mt.waitForAll();
      } catch(InterruptedException e) {
           lg.addLog(new Log(e.getMessage(),
                  "FractureImage/FractureImage",
                  "Loading images"));
      }

      baseImageXsize = baseImage.getWidth(this);
      baseImageYsize = baseImage.getHeight(this);
      basemapXscale = baseImageXsize / imap.getWidth();
      basemapYscale = baseImageYsize / imap.getHeight();

      setSize(baseImageXsize, baseImageYsize);
  
      // Only add the mouse listener if the image is editable    
      if (isEditable)
         addMouseListener(this);
   }

   /**
     * Method called when the compenent is painted.
     */
   public void paint(Graphics page)
   {
      page.drawImage(baseImage, 0, 0, 125, 350, this);

      if (selected[1])  // Head
         page.drawImage(redImage, 34, 4, 64, 47,   // Destination
               34, 4, 64, 47, this); // Source
      if (selected[2])  // Right sholder
         page.drawImage(redImage, 9, 56, 31, 79,   // Destination
               4, 31, 26, 54, this);   // Source
      if (selected[3])  // Right upperarm
          page.drawImage(redImage, 3, 66, 15, 127, // Destination
               3, 66, 15, 127, this); // Source
      if (selected[4])  // Right underarm
         page.drawImage(redImage, 3, 127, 10, 186, // Destination
               3, 127, 10, 186, this);  // Source
      if (selected[5])  // Right hand
         page.drawImage(redImage, 2, 186, 19, 220, // Destination
               2, 186, 19, 220, this); // Source
      if (selected[6])  // Left shoulder
         page.drawImage(redImage, 66, 57, 88, 77,  // Destination
               70, 32, 92, 52, this);  // Source
      if (selected[7])  // Left upperarm
         page.drawImage(redImage, 82, 68, 91, 123, // Destination
               82, 68, 91, 123, this); // Source
      if (selected[8])  // Left underarm
         page.drawImage(redImage, 83, 123, 105, 186,  // Destination
               83, 123, 105, 186, this); // Source
      if (selected[9])  // Left hand
         page.drawImage(redImage, 98, 186, 121, 221, // Destination
               98, 186, 121, 221, this); // Source
      if (selected[10]) // Right rib
         page.drawImage(redImage, 20, 56, 49, 127,    // Destination
               20, 56, 49, 127, this);    // Source
      if (selected[11]) // Left rib
         page.drawImage(redImage, 49, 56, 78, 128,    // Destination
               49, 56, 78, 128, this);    // Source
      if (selected[12]) // Spine
      {
         page.drawImage(redImage, 43, 47, 52, 56,     // Destination
               43, 47, 52, 56, this);     // Source
         page.drawImage(redImage, 43, 110, 58, 154,   // Destination
               43, 188, 58, 232, this);   // Source
      }
      if (selected[13]) // Pelvis
         page.drawImage(redImage, 20, 142, 80, 186,   // Destination
               20, 128, 80, 172, this);   // Source
      if (selected[14]) // Right Thigh
         page.drawImage(redImage, 20, 173, 37, 237,   // Destination
               20, 173, 37, 237, this);   // Source
      if (selected[15]) // Right 
         page.drawImage(redImage, 27, 237, 41, 309,   // Destination
               27, 237, 41, 312, this);   // Source
      if (selected[16]) // Right foot
         page.drawImage(redImage, 20, 309, 42, 345,   // Destination
               20, 312, 42, 345, this);
      if (selected[17])  // Left Thigh
         page.drawImage(redImage, 63, 172, 78, 236, // Destination
               63, 172, 78, 236, this);
      if (selected[18]) // Left
         page.drawImage(redImage, 60, 236, 72, 309,    // Destination
               60, 236, 72, 309, this);
      if (selected[19]) // Left foot
         page.drawImage(redImage, 60, 309, 80, 347,   // Destination
               60, 309, 80, 347, this);
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
         StringBuffer strBuf = new StringBuffer(300);
         strBuf.append("UPDATE EPR.FRACTURE SET HED = ");
         strBuf.append(selected[1] ? "'t'" : "'f'");
         strBuf.append(", RSH = ").append(selected[2] ? "'t'" : "'f'");
         strBuf.append(", RUA = ").append(selected[3] ? "'t'" : "'f'");
         strBuf.append(", RLA = ").append(selected[4] ? "'t'" : "'f'");
         strBuf.append(", RHA = ").append(selected[5] ? "'t'" : "'f'");
         strBuf.append(", LSH = ").append(selected[6] ? "'t'" : "'f'");
         strBuf.append(", LUA = ").append(selected[7] ? "'t'" : "'f'");
         strBuf.append(", LLA = ").append(selected[8] ? "'t'" : "'f'");
         strBuf.append(", LHA = ").append(selected[9] ? "'t'" : "'f'");
         strBuf.append(", RRI = ").append(selected[10] ? "'t'" : "'f'");
         strBuf.append(", LRI = ").append(selected[11] ? "'t'" : "'f'");
         strBuf.append(", SPI = ").append(selected[12] ? "'t'" : "'f'");
         strBuf.append(", PEL = ").append(selected[13] ? "'t'" : "'f'");
         strBuf.append(", RTH = ").append(selected[14] ? "'t'" : "'f'");
         strBuf.append(", RLL = ").append(selected[15] ? "'t'" : "'f'");
         strBuf.append(", RFO = ").append(selected[16] ? "'t'" : "'f'");
         strBuf.append(", LTH = ").append(selected[17] ? "'t'" : "'f'");
         strBuf.append(", LLL = ").append(selected[18] ? "'t'" : "'f'");
         strBuf.append(", LFO = ").append(selected[19] ? "'t'" : "'f'");
         strBuf.append(", PP_RA = ").append(extr[0] ? parent.getExtr(0) : null);
         strBuf.append(", PP_LA = ").append(extr[1] ? parent.getExtr(1) : null);
         strBuf.append(", PP_RL = ").append(extr[2] ? parent.getExtr(2) : null);
         strBuf.append(", PP_LL = ").append(extr[3] ? parent.getExtr(3) : null);
         strBuf.append(" WHERE RECORD_ID = ").append(recordId);
         try {
            dbcon.dbQueryUpdate (strBuf.toString());
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "FractureImage/save",
                     "Update fracture"));
         }
      }
      else  // Not in database
      {
         StringBuffer strBuf = new StringBuffer(300);
         strBuf.append("INSERT INTO EPR.FRACTURE (RECORD_ID, HED, RSH, " +
               "RUA, RLA, RHA, LSH, LUA, LLA, LHA, RRI, LRI, SPI, PEL, " +
               "RTH, RLL, RFO, LTH, LLL, LFO, PP_RA, PP_LA, PP_RL, PP_LL)" +
               " VALUES("); 
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
         strBuf.append(", ").append(selected[11] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[12] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[13] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[14] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[15] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[16] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[17] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[18] ? "'t'" : "'f'");
         strBuf.append(", ").append(selected[19] ? "'t'" : "'f'");
         strBuf.append(", ").append(extr[0] ? parent.getExtr(0) : null);
         strBuf.append(", ").append(extr[1] ? parent.getExtr(1) : null);
         strBuf.append(", ").append(extr[2] ? parent.getExtr(2) : null);
         strBuf.append(", ").append(extr[3] ? parent.getExtr(3) : null);
         strBuf.append(")");
         try {
            dbcon.dbQueryUpdate (strBuf.toString());
            isInDb = true;
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "FractureImage/save",
                     "Insert fracture"));
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
      if (value >=3 && value <= 5)   // Right arm
      {
         selected[value] = !selected[value];

         if (!extr[0]  && (selected[3] || selected[4] || selected[5]))
         {
            extr[0] = true;
            parent.enableExtr(0, true);

         }
         else if (extr[0] && !selected[3] && !selected[4] && !selected[5])
         {
            extr[0] = false;
            parent.enableExtr(0, false);
         }
         repaint();
      }
      else if (value >= 7 && value <= 9)  // Left arm
      {
         selected[value] = !selected[value];

         if (!extr[1]  && (selected[7] || selected[8] || selected[9]))
         {
            extr[1] = true;
            parent.enableExtr(1, true);

         }
         else if (extr[1] && !selected[7] && !selected[8] && !selected[9])
         {
            extr[1] = false;
            parent.enableExtr(1, false);
         }
         repaint();
      }
      else if (value >= 14 && value <= 16)   // Right leg
      {
         selected[value] = !selected[value];

         if (!extr[2]  && (selected[14] || selected[15] || selected[16]))
         {
            extr[2] = true;
            parent.enableExtr(2, true);

         }
         else if (extr[2] && !selected[14] && !selected[15] && !selected[16])
         {
            extr[2] = false;
            parent.enableExtr(2, false);
         }
         repaint();
      }
      else if (value >= 17 && value <= 19)
      {
         selected[value] = !selected[value];

         if (!extr[3]  && (selected[17] || selected[18] || selected[19]))
         {
            extr[3] = true;
            parent.enableExtr(3, true);

         }
         else if (extr[3] && !selected[17] && !selected[18] && !selected[19])
         {
            extr[3] = false;
            parent.enableExtr(3, false);
         }
         repaint();
      }        
      else if (value != 0)    // Background
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
