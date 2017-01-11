import java.util.Date;

/**
  * Struct storing Date pairs and how many times they have occured.
  * Used by GCSModel to keep track of changeTime calls.
  * 
  * @version 20030220
  * @author Oskar Nilsson
  */
public class UpdateEntry
{
   public Date newDate;
   public Date oldDate;
   public int num;

   public UpdateEntry(Date newDate, Date oldDate)
   {
      this.newDate = newDate;
      this.oldDate = oldDate;
      num = 1;
   }

   public boolean Equals(UpdateEntry e)
   {
      return (newDate.equals(e.newDate) && oldDate.equals(e.oldDate));
   }

}
