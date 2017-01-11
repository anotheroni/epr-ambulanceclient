/**
  * Class to store a name and a number.
  * Used in JComboBoxes.
  * @author Oskar Nilsson
  * @version 021102
  */
public class ListEntry
{
   private String name;
   private int nr;

   /**
     * Consstructor, creates a new list entry with a name and a number.
     * @param name Name of the entry.
     * @param nr Number of the entry.
     */
   public ListEntry(String name, int nr)
   {
      this.name = name;
      this.nr = nr;
   }

   /**
     * Method to get the number of the entry.
     * @return The number of the entry.
     */
   public int getNumber()
   {
      return nr;
   }

   /**
     * Method to get the name of the entry.
     * @return The name of the entry.
     */
   public String toString()
   {
      return name;
   }
}
