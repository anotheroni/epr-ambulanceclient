import java.awt.event.*;
import javax.swing.JTextField;
import javax.swing.text.*;
import javax.swing.event.CaretEvent;

/**
 * Class implementing a key listener for text fields.
 *
 * @version 20030626
 * @author Oskar Nisson
 */
public class AutoComplete_Listener extends KeyAdapter
{

   private WordList wl;
   private int userid;

   private JTextComponent inputFld;
   private Document inputDoc;

   private boolean completeMode = false;
   private int lastSeparatorIdx = 0;
   private int startCaretIdx = 0;
   private int wordLength;
   private int matchLength;
 
   /**
    * Constructor.
    * @param inputFld A reference to the textcomponent the listener is
    * listening to.
    * @param wl A reference to the world list to use for auto complete.
    * @param userid ID of the user.
    */
   public AutoComplete_Listener(JTextComponent inputFld, WordList wl,
         int userid)
   {
      this.wl = wl;
      this.inputFld = inputFld;
      this.userid = userid;
      inputDoc = inputFld.getDocument();
    }

   /**
    * Handle the key pressed event.
    */
   public void keyPressed(KeyEvent e) {
      String result;

      int keyCode = e.getKeyCode();
      int modifiers = e.getModifiers();

      if (e.isAltDown())
      {
         switch (keyCode) {
            case java.awt.event.KeyEvent.VK_Z:
            case java.awt.event.KeyEvent.VK_SPACE:
               findWord();
               break;
            case java.awt.event.KeyEvent.VK_ALT:
               break;
            default:
               if ((result = wl.getFavorite(e.getKeyChar(), userid)) != null)
                  insertFavorite(result);
               else
                  reset();
         }
      }
/*      else if (e.isShiftDown())
      {
         switch (keyCode) {
         }
      }*/
      else
      {
         switch (keyCode) {
            case java.awt.event.KeyEvent.VK_SPACE:
               useWord();
               break;
            case java.awt.event.KeyEvent.VK_ALT:
               break;
            default:
               reset();
         }
      }
   }

   /**
     * Method used to insert a favorite into the text compenent.
     * @param str The string to insert.
     */
   public void insertFavorite(String str)
   {
      if (completeMode) // A word alredy suggested
      {
         try {
            inputDoc.remove(startCaretIdx, wordLength);
         } catch (BadLocationException e) {}
      }
      
      completeMode = true;
      startCaretIdx = inputFld.getCaretPosition();

      try {
         inputDoc.insertString(startCaretIdx, str, null);
      } catch (BadLocationException e) {}

      wordLength = inputFld.getCaretPosition() - startCaretIdx;
      inputFld.select(startCaretIdx, inputFld.getCaretPosition());
   }
   
   /**
     * Method that finds the completion of the word at the caret.
     * If a word already has been found the old completion is removed.
     */
   public void findWord()
   {
      String sug = null;
      String text = null;

      if (completeMode) // A word already suggested
      {
         try {
            inputDoc.remove(startCaretIdx, wordLength);
         } catch (BadLocationException e) {}
         sug = wl.getNextWord();
      }
      else  // The first suggestion
      {
         completeMode = true;
         startCaretIdx = inputFld.getCaretPosition();
         
         // The max length of a word is 32 letters in the database
         int firstIdx = (startCaretIdx < 32 ? 0 : startCaretIdx - 32);
         try {
            text = inputFld.getText(firstIdx, startCaretIdx - firstIdx);
         } catch (BadLocationException e) {}

         int last = text.lastIndexOf(0x0020);  // Find index of the last space
         if (last == -1) // If no space exists, use the whole string
            last = 0;
         else
            last++;    // Remove space from string

         lastSeparatorIdx = firstIdx + last;
         matchLength = startCaretIdx - lastSeparatorIdx;

         sug = wl.getWord((text.substring(last, startCaretIdx - firstIdx)).
               toLowerCase());
      }

      if (sug == null)  // Check if there exists any suggestions
      {
         completeMode = false;
         return;
      }

      try {
         int lastSpace = sug.indexOf(0x0020); // Find index of the first space
         if (lastSpace == -1) // There is no space, get the rest of the word
            inputDoc.insertString(startCaretIdx, sug.substring(matchLength),
                  null);
         else  // There are spaces at the end, remove them
            inputDoc.insertString(inputFld.getCaretPosition(),
                  sug.substring(matchLength, lastSpace), null);
      } catch (BadLocationException e) {}

      wordLength = inputFld.getCaretPosition() - startCaretIdx;
      inputFld.select(startCaretIdx, inputFld.getCaretPosition());
   }

   /**
     * Method that accepts a suggested completion. If a completion has been
     * suggested, move the caret to the end of the word.
     */
   public void useWord()
   {
      if (completeMode)
      {
         int caretIdx = inputFld.getCaretPosition();
         inputFld.select(caretIdx, caretIdx); 
         completeMode = false;
      }
   }

   /**
     * Method that removes a suggested completion.
     */
   public void reset()
   {
      if (completeMode)
      {
         try {
            inputDoc.remove(startCaretIdx, wordLength);
         } catch (BadLocationException e) {}
         completeMode = false;
      }
   }
}
