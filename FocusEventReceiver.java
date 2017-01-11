/**
  * Interface implemented by classed that have a component that uses a focus
  * listener.
  *
  * @version 20030716
  * @author Oskar Nilsson
  */
public interface FocusEventReceiver
{
   /**
     * Method called by the cell editor focus listener when the editor
     * looses focus. Saves the current value.
     * @param val The text in the editor.
     * @return null if all is ok, else an error message.
     */
   public String saveCurrentCell (String val);

   /**
     * Method called when a cell editor receives focus.
     */
   public void setCurrentCell ();
}
