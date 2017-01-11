import java.util.Vector;

/**
  * Struct used to store given medication.
  *
  * @version 20030128
  * @author Oskar Nilsson
  */
public class Print_Medicine
{
   /// ID of the medicatin event.
   public int medId;
   /// Name of the medicine.
   public String medName;
   /// Amount of medicine.
   public String medAmount;
   /// Name of the person that gave the medicine.
   public String giverName;
   /// Time the medicine was given.
   public String medTime;
   /// Comments for the medication.
   public Vector comments = null;

   /**
     * Constructor, initzialices the local variables.
     * @param medId The id of the medication event.
     * @param medName The name of the medicine given.
     * @param medAmount The amount of medicine given.
     * @param giverName The name of the person that gave the medicine.
     * @param medTime The time that the medicine were given.
     */
   public Print_Medicine(int medId, String medName, String medAmount,
         String giverName, String medTime)
   {
      this.medId = medId;
      this.medName = medName;
      this.medAmount = medAmount;
      this.giverName = giverName;
      this.medTime = medTime;
   }

   /**
     * Method that adds a new comment to the medication event.
     * @param comment The comment to add.
     */
   public void addComment(String comment)
   {
      if (comments == null)
         comments = new Vector();
      comments.add(comment);
   } 
}
