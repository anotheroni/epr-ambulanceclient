import javax.swing.*;
import java.awt.*;

/**
  * Class implementing a status field used for displaying messages.
  */
public class StatusField extends JPanel
{

   private JTextField textFld;
   
   private Font font;
   private int state;

   private final int NORMAL = 0;
   private final int WARNING = 1;
   
   /**
     *
     * @param font The font to use, null for default font.
     */
   public StatusField(Font font)
   {
      super();
      this.font = font;
      state = NORMAL;

      textFld = new JTextField();
      textFld.setEditable(false);
      if (font != null)
         textFld.setFont(font);

      setLayout(new BorderLayout());
      
      add(textFld, BorderLayout.CENTER);
   }

   public void setText(String text)
   {
      if (state != NORMAL)
      {
         textFld.setForeground(Color.BLACK);
         state = NORMAL;
      }
      textFld.setText(text);
   }

   public void setWarning(String text)
   {
      if (state != WARNING)
      {
         textFld.setForeground(Color.RED);
         state = WARNING;
      }
      textFld.setText(text);
   }

   public void clear()
   {
      textFld.setText("");
   }
   
}
