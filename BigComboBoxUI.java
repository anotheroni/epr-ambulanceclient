import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;

/**
  * Class implementing a new show method for the BigComboBox. It renders
  * the popup as wide as the comboBox wants it to be.
  *
  * @version 20030217
  * @author Oskar Nilsson
  */
public class BigComboBoxUI extends MetalComboBoxUI
{

   /**
     * Overrided factory method that creates the popup part of a combobox.
     * @return A popup for the comobox.
     */
   protected ComboPopup createPopup()
   {
      // comboBox is a protected JComboBox in BasicComboPopup
      BasicComboPopup popup = new BasicComboPopup(comboBox)
      {
         /**
           * Overrided show method that draws a popup with the size specified
           * in the combobox.
           */
         public void show()
         {
            Dimension popupSize = ((BigComboBox)comboBox).getPopupSize();
            popupSize.setSize(popupSize.width,
                  getPopupHeightForRowCount(comboBox.getMaximumRowCount()));
            Rectangle popupBounds = computePopupBounds(0,
                 comboBox.getBounds().height, popupSize.width,
                 popupSize.height);

            // protected JScrollPane in BasicComboPopup
            scroller.setMaximumSize(popupBounds.getSize());
            scroller.setPreferredSize(popupBounds.getSize());
            scroller.setMinimumSize(popupBounds.getSize());
            list.invalidate();
 
            // protected JScrollPane in BasicComboPopup
            int selectedIndex = comboBox.getSelectedIndex();
            if (selectedIndex == -1)
               list.clearSelection();
            else
               list.setSelectedIndex(selectedIndex);
            list.ensureIndexIsVisible(list.getSelectedIndex());
            
            setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

            show(comboBox, popupBounds.x, popupBounds.y);
         }
      };

      popup.getAccessibleContext().setAccessibleParent(comboBox);
      return popup;
   }
}
