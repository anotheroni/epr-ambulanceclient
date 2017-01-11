import java.awt.event.*;
import javax.swing.text.JTextComponent;

/**
  * Class extending a focus listener to listen for lost focus from GUI
  * components.
  *
  * @version 20030716
  * @author Oskar Nilsson
  */
public class ComponentFocusListener extends FocusAdapter
{
   private FocusEventReceiver o;
   private JTextComponent c;

   /**
     * Constructor, sets the local variables.
     * @param o A reference to the row that the component is a part of.
     * @param c The component to listen to.
     */
   public ComponentFocusListener (FocusEventReceiver o,
         JTextComponent c)
   {
      this.o = o;
      this.c = c;
   }

   /**
     * Method called by the system when component c looses focus.
     * @param e Information about the event.
    */
   public void focusLost (FocusEvent e)
   {
      String res;
      res = o.saveCurrentCell (c.getText ());
      if (res != null)
         c.setText ("");
   }

   public void focusGained (FocusEvent e)
   {
      o.setCurrentCell ();
   }
}
