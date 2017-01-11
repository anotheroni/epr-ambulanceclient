import javax.swing.*;
import java.awt.Dimension;
import java.util.*;

/**
  * Class implementing a combobox were the width of the popup for the
  * combobox is definable.
  *
  * @version 20030217
  * @author Oskar Nilsson
  */
public class BigComboBox extends JComboBox
{
   protected int popupWidth = 0;

   /**
     * Constructor that takes a ComboBoxModel.
     * @param bModel The data model that the combobox should show.
     */
   public BigComboBox(ComboBoxModel bModel)
   {
      super(bModel);
      setUI(new BigComboBoxUI());
   }

   /**
     * Constructor that takes an array of objects.
     * @param items The objects that the combobox should contain.
     */
   public BigComboBox(Object[] items)
   {
      super(items);
      setUI(new BigComboBoxUI());
   }
   
   /**
     * Constructor that takes a vector.
     * @param items A vector containing the objects that the combobox should
     * contain.
     */
   public BigComboBox(Vector items)
   {
      super(items);
      setUI(new BigComboBoxUI());
   }

   /**
     * Method to set the width of the combobox popup.
     * @param width The width of the popup.
     */
   public void setPopupWidth(int width)
   {
      popupWidth = width;
   }

   /**
     * Method that returns the size of the combobox popup.
     * @return Te dimnsion of the popup.
     */
   public Dimension getPopupSize()
   {
      Dimension size = getSize();
      
      if (popupWidth < 1)
         popupWidth = size.width;
      
      return new Dimension(popupWidth, size.height);
   }
}
