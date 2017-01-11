import java.io.*;
import java.util.StringTokenizer;
import java.awt.Point;

/**
  * Class representing an image map, ie the image is divided into areas.
  * The class knows which area a pixel belongs to.
  * The map is stored as a byte array, so the valid area numbers are 0-255.
  * The map is constructed from a .map file.
  *
  * @version 20030315
  * @author Oskar Nilsson
  */
public class ImageMap
{
   private byte[][] map =  null;

   private int xSize = 0;
   private int ySize = 0;

   /**
     * Constructor that builds an image map from a file.
     * The file has to be must be formated as follows:<BR>
     * -The first line contains comments.<BR>
     * -The second line contains the width and eight of the map speparated
     * by a space.<BR>
     * -The following lines contains the values (0-255) of the map.
     * The values are separated by space.<BR>
     * @param fileName The name of the file that contains the map.
     */
   public ImageMap(String fileName, LogHandler lg)
   {
      BufferedReader in = null;
      String buf;
      StringTokenizer st;
      boolean ok = false;

      try {
         in = new BufferedReader(new FileReader(fileName));
      } catch(FileNotFoundException e) {
         lg.addLog(new Log(e.getMessage(),
                  "ImageMap/ImageMap",
                  "Opening file " + fileName));
         return;
      }

      // Continue to read until entire map is read
      for(int lineNum = 1; true ; lineNum++)
      {
         try {
            buf = in.readLine();
         } catch (IOException e) {
            lg.addLog(new Log(e.getMessage(),
                     "ImageMap/ImageMap",
                     " Bad formating line " +lineNum+ " in file " +fileName));
            break;
         }

         if (lineNum == 1) // Comment
         {
            continue;      
         }
         else if (lineNum == 2) // status line
         {
            st = new StringTokenizer(buf);
            try {
               xSize = java.lang.Integer.parseInt(st.nextToken());
               ySize = java.lang.Integer.parseInt(st.nextToken());
            } catch (Exception e) {
               lg.addLog(new Log(e.getMessage(),
                        "ImageMap/ImageMap",
                        "Bad formating on line "+lineNum+" in file "+fileName));
               break;
            }
            map = new byte[xSize][ySize];

         }
         else if (lineNum < ySize + 2)   // data
         {
            st = new StringTokenizer(buf);
            for (int i=0 ; i < xSize ; i++)
            {
               try {
                  map[i][lineNum-2] = java.lang.Byte.parseByte(st.nextToken());
               } catch (Exception e) {
                  lg.addLog(new Log(e.getMessage(),
                           "ImageMap/ImageMap",
                           "Bad formating on line " + lineNum + " in file " +
                           fileName));
                  break;
               }
            }
         }
         else
         {
            ok = true;
            break;
         }
      } // for loop

      try {
         in.close();  //Close the BufferedReader
      }catch(IOException e) {
         lg.addLog(new Log(e.getMessage(),
                  "ImageMap/ImageMap",
                  "Closing file " + fileName));
      }

      if (!ok)
      {
         lg.addLog(new Log("!ok",
                  "ImageMap/ImageMap",
                  "Problems when reading ImageMap"));
      }

   } // Constructor

   /**
     * Method to get the value of a position in the map.
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @return Value of position (x,y) in map. 0 if the map failed to load.
     */
   public byte getValue(int x, int y)
   {
      if (map == null)
         return 0;
      
      return map[x][y];
   }

   /**
     * Method to get the map width.
     * @return The map width.
     */
   public int getWidth()
   {
      return xSize;
   }

    /**
     * Method to get the map height.
     * @return The map height.
     */
   public int getHeight()
   {
      return ySize;
   }

}
