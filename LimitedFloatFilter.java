import javax.swing.text.*;

/**
  * Class implementing a document filter that limits the lenght of a
  * textfield and only allows float numbers.
  *
  * @version 20030224
  * @author Oskar Nilsson
  */
public class LimitedFloatFilter extends DocumentFilter
{
   private int maxSize;

   /**
     * Constructor, setting the size limit.
     * @param maxSize The maximim number of characters in the textfield.
     */
   public LimitedFloatFilter(int maxSize)
   {
      this.maxSize = maxSize;
   }

   /**
     * Method called when characters are inserted into the document.
     * @param fb FilterBypass that can be used to mutate Document.
     * @param offset Offset into the document to insert the string.
     * @param str The string to insert.
     * @param attr The attributes to associate with the inserted string.
     */
   public void insertString(DocumentFilter.FilterBypass fb, int offset,
        String str, AttributeSet attr) throws BadLocationException
   {
      replace(fb, offset, 0, str, attr);
   }

   /**
     * Method called when characters in the document are replaced with other
     * characters.
     * @param fb FilterBypass that can be used to mutate Document.
     * @param offset Offset into the document to insert the string.
     * @param length Length of the text to delete.
     * @param str The string to insert.
     * @param attr The attributes to associate with the inserted string.
     */
   public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
         String str, AttributeSet attr) throws BadLocationException
   {
      int insLen = str.length();
      int newLength = fb.getDocument().getLength() - length + insLen;
      if (newLength <= maxSize) {
         try { // Check if the string contains only numbers
            Float.parseFloat(str);
         } catch(NumberFormatException e) {
            // Check for a .
            if (!str.equals("."))
               return;
         }
         fb.replace(offset, length, str, attr);
      }
   }

}
