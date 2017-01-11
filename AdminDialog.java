/**
  * Interface implemented by the dialog classes in the admin database client.
  *
  * @version 021207
  * @author Oskar Nilsson
  */
public interface AdminDialog
{
   /**
     * Method called by the listener when the user pressed ok.
     */
   public void okPressed();

   /**
     * Method called by the listener when the user pressed cancel.
     */
   public void cancelPressed();
}
