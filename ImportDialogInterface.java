/**
  * Interface for the import dialogs.
  *
  * @version 20030701
  * @author Oskar Nilsson
  */
public interface ImportDialogInterface
{

   /**
     * Method called by the Parser to return the state of the last import.
     * @param state True if there was a message, False if the message queue
     * is empty (no more messages).
     */
   public void returnState (boolean state);   

}
