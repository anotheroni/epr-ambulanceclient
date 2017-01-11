import javax.swing.text.*;

/**
  * Class implementing a document filter that limits the lenght of a
  * textfield and only allows numbers and %-signs.
  *
  * @version 20030702
  * @author Oskar Nilsson
  */
public class LimitedValueUnitFilter extends DocumentFilter
{
   /// The maximum number of signs in the document.
   private int maxSize;

   /// The unit that is allowed in the text field.
   private char unit;

   /**
     * Constructor, sets the local variables.
     * @param maxSize The maximum number of signs in the document.
     * @param unit The unit-sign that is allowed, '%' e.g.
     */
   public LimitedValueUnitFilter(int maxSize, char unit)
   {
      this.maxSize = maxSize;
      this.unit = unit;
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

      if (newLength <= maxSize)
      {
         char c[] = str.toCharArray();
         for (int i = 0 ; i < insLen ; i++)
         {
            if (!Character.isDigit(c[i]) && c[i] != unit)
               return;
         }
         fb.replace (offset, length, str, attr);
      }
   }
}
