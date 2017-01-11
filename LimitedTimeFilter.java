
import javax.swing.text.*;

/**
  * Class implementing a document filter that limits the lenght of a
  * textfield and only allows times.
  *
  * @version 20030721
  * @author Oskar Nilsson
  */
public class LimitedTimeFilter extends DocumentFilter
{
   /// The maximum number of signs in the document.
   private int maxSize;

   /// Flag signaling if remove is allowed.
   private boolean remove;

   /// The text component the filter is attached to (optional).
   private JTextComponent textComponent = null;

   /**
     * Constructor, sets the local variables.
     * Text can not be removed (remove = false).
     * @param maxSize The maximum number of signs in the document.
     */
   public LimitedTimeFilter(int maxSize)
   {
      this.maxSize = maxSize;
      remove = false;
   }

   /**
     * Costructor, initializes the local variables.
     * @param maxSize The maximum number of signs in the document.
     * @param remove true if removing text should be allowed.
     */
   public LimitedTimeFilter (int maxSize, boolean remove)
   {
      this.maxSize = maxSize;
      this.remove = remove;
   }

   /**
     * Constructor, initializes the local variables.
     * Text can not be removed (remove = false) instead the caret is moved.
     * @param maxSize The maximum number of signs in the document.
     * @param textComponent The component that the filter is attached to.
     */
   public LimitedTimeFilter (int maxSize, JTextComponent textComponent)
   {
      this.maxSize = maxSize;
      this.textComponent = textComponent;
      remove = false;
   }

   /**
     * Method called when characters are inserted into the document.
     * @param fb FilterBypass that can be used to mutate Document.
     * @param offset Offset into the document to insert the string.
     * @param str The string to insert.
     * @param attr The attributes to associate with the inserted string.
     */
   public void insertString (DocumentFilter.FilterBypass fb, int offset,
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
   public void replace (DocumentFilter.FilterBypass fb, int offset, int length,
         String str, AttributeSet attr) throws BadLocationException
   {
      int insLen = str.length();
      int len = fb.getDocument().getLength();
      if (offset < maxSize) {
         char c[] = str.toCharArray();
         for (int i = 0 ; i < insLen ; i++)
         {
            switch (offset + i)
            {
               // Only allow : betwen times
               case 2:
               case 5:
                  if (c[i] == ':')
                     continue;
                  return;
               // Lesser hour allow 0-9 if greater hour is 0-1 else only 0-4
               case 1:
                  if (c[i] == '0' || c[i] == '1' || c[i] == '2' || c[i] == '3'
                        || c[i] == '4')
                     continue;
                  // getDocument isn't working when setting the whole value
                  if (i == 1) // both hh values are changed
                  {
                     if ((c[0] == '0' || c[0] == '1') && (c[i] == '5' || 
                              c[i] == '6' || c[i] == '7' || c[i] == '8' ||
                              c[i] == '9'))
                        continue;
                  }
                  else  // Only lesser hour is replaced get the greater from doc
                  {
                     String hr = fb.getDocument().getText (0, 1);
                     if ((hr.equals("0") || hr.equals("1")) && (c[i] == '5' || 
                              c[i] == '6' || c[i] == '7' || c[i] == '8' ||
                              c[i] == '9'))
                        continue;
                  }
                  return;
               // lesser minute and second allow 0-9
               case 4:
               case 7:
                  if (c[i] == '6' || c[i] == '7' || c[i] == '8' || c[i] == '9')
                     continue;
               // greater minute and second allow 0-5
               case 3:
               case 6:
                  if (c[i] == '3' || c[i] == '4' || c[i] == '5')
                     continue;
               // greater hour allow 0-2
               case 0:
                  if (c[i] == '0' || c[i] == '1' || c[i] == '2')
                     continue;
                  return;
           }
         }
         // Automaticly add a ":" after HH and mm
         if (insLen == 1 && (offset == 1 || (offset == 4 && maxSize > 5)))
         {
            if (offset + 2 <= len)
               fb.replace (offset, 2, str + ":", attr);
            else
               fb.replace (offset, offset - len, str + ":", attr);
         }
         else if (offset + insLen <= len)
            fb.replace (offset, insLen, str, attr);
         else
            fb.replace (offset, offset - len, str, attr);
      }
  }

   /**
     * Metohd called when characters are removed in the document.
     * @param fb FilterBypass that can be used to mutate Document.
     * @param offset Offset into the document to insert the string.
     * @param length Length of the text to delete.
     */
   public void remove (DocumentFilter.FilterBypass fb, int offset, int length)
      throws BadLocationException
   {
      if (remove)
         fb.remove (offset, length);
      else if (textComponent != null)  // Move the caret instead of remove
      {
         int pos = textComponent.getCaretPosition () - length;
         if (pos >= 0)
            textComponent.setCaretPosition (pos);
         else
            textComponent.setCaretPosition (0);
      }
   }

}
