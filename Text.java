/**
  * Struct used to store the text and the position to print the text at.
  *
  * @version 20030304
  * @author Oskar Nilsson
  */
public class Text
{

   /// X-position of the text.
   public int x;
   /// Y-position of the text.
   public int y;
   /// The text to print
   public String text;

   /**
     * Constructor, initializes the local varibles.
     * @param x X-position of the text.
     * @param y Y-position of the text.
     * @param text The text to print.
     */
   public Text(int x, int y, String text)
   {
      this.x = x;
      this.y = y;
      this.text = text;
   }

}
