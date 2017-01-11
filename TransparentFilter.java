import java.awt.image.*;
import java.awt.*;

/**
  * Class that converts an image to make the specified color transparent.
  *
  * @version 021030
  * @author Oskar Nilsson
  */
public class TransparentFilter extends RGBImageFilter
{

   //Filter don't depend on the position of the pixel (the filter works faster)
   private final boolean canFilterIndexColorModel = true;

   /// The color that is filtered.
   private Color colorToFilter;

   /**
     * Creates a standard TransparentFilter that makes one color transparent.
     * \param c The color to make transparent.
     */
   public TransparentFilter(Color c)
   {
      colorToFilter = c;
   }
   
   /**
     * Method that filters an image.
     * \param x X-position of the pixel (not used).
     * \param y Y-position of the pixel (not used).
     * \param rgb RGB-value of the pixel.
     * \return The new RGB-value of the pixel.
     */
   public int filterRGB(int x, int y, int rgb)
   {
      if ( colorToFilter.getRGB() == rgb )
      {
         return( 0x00000000 ); //It is of the color that should be transparent
      }
      return rgb; //Not transparent return unchanged color
   }

} //class
