import java.sql.*;

/**
  * Class implementing a word list. The words are stored in a database.
  *
  * @version 20030314
  * @author Oskar Nilsson
  */
public class WordList
{
 
   private DB2Connect dbcon;
   private LogHandler lg;
   
   private ResultSet rs = null;
 
   /**
     * Constructor, sets the reference to the database.
     * @param dbcon A reference to the database containing the wordlist.
     * @param lg The loghandler to report errors to.
     */
   public WordList(DB2Connect dbcon, LogHandler lg)
   {
      this.dbcon = dbcon;
      this.lg = lg;
   }

   /**
     * Method used to get the favorite associated with the key.
     * @param c The key pressed.
     * @param userid ID of the user.
     * @return The string associated with the key. null if no string exists.
     */
   public String getFavorite(char c, int userid)
   {
      try {
         if (rs != null)
            rs.close();
         rs = dbcon.dbQuery("SELECT STRING FROM EPR.AUTO_COMPLETION WHERE " +
               "KEY = '" + c + "' AND USER_ID = " + userid);
         if (rs.next())
            return rs.getString("STRING");
         else
            return null;
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "WordList/getFavorite",
                  "Read auto completion"));
      }
      return null;
   }

   /**
     * Method that finds words that begin with word.
     * @param word The string that the word should start with.
     * @return The first word that match, null if no word exists.
     */
   public String getWord(String word)
   {
      try {
         if (rs != null)
            rs.close();
         rs = dbcon.dbQuery("SELECT * FROM EPR.MEDICAL_LEXICON " +
               "WHERE WORD LIKE '" + word + "%'");
         if (rs.next())
            return rs.getString("WORD");
         else
            return null;
      } catch(SQLException e) {
           lg.addLog(new Log(e.getMessage(),
                  "WordList/getWord",
                  "Read medical lexicon"));
      }
      return null;
   }

   /**
     * Method that returns the next matching string from the last call to
     * getWord.
     * @return The next suggestion, null if no word exits.
     */
   public String getNextWord()
   {
      try {
         if (rs.next())
            return rs.getString("WORD");
         else
            return null;
      } catch(SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "WordList/getNextWord",
                  "Read next word"));
      }
      return null;
   }

}
