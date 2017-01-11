
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.*;

/**
  * Class representing a oxygen row in the observations table that
  * contains integer values. The value can be given both in % and liter.
  * The value shown in the table is in liters. A new value entered is
  * in liters by default if no unit is given. If a %-sign is entered
  * the value is converted from % to liters.
  * 
  * @version 20030702
  * @author Oskar Nilsson
  */
public class OxyObservationTableRow extends StdObservationTableRow
{
 
   /**
     * Constructor, initialises the local variables.
     * @param recordId The curent record id.
     * @param paramId The id of the parameter in the database.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param rowName The name of the row.
     * @param minVal The minimum value allowed.
     * @param maxVal The maximum value allowed.
     * @param times The rows that exists in the table.
     * @param paramTModel A reference to the table model that the row is a
     * part of.
     */
   public OxyObservationTableRow(int recordId, int paramId,
        DB2Connect dbcon, LogHandler lg, String rowName, int minVal, int maxVal,
        Vector times, ParameterTableModel paramTModel)
   {
      super (recordId, paramId, dbcon, lg, rowName, minVal, maxVal, times,
            paramTModel);

      ((AbstractDocument)cellEditorFld.getDocument()).
         setDocumentFilter(new LimitedValueUnitFilter(4, '%'));
   }
 
   /**
     * Method that updates the value of a column.
     * @param val The new value of col.
     * @param col The column number to update.
     * @return null if the value is valid,
     * an error message if it is in the wrong format.
     */
   public String setValue(String val, int col)
   {
      int iVal;

      // Value given i %
      if (val.endsWith ("%"))
      {
         String numVal = val.substring (0, val.length() - 1);
         
         try { // Check if the string contains only numbers
            iVal = Integer.parseInt (numVal, 10);
         } catch (NumberFormatException e) {
            return "Mängden syre måste anges som ett heltal. " +
               "Anges det i % avsluta med ett %-tecken.";
         }

         if (iVal > 100 || iVal < 21)  // Check that it's a valid number
            return "%-halten syre måste vara mellan 21 och 100";

         // Convert to liters
         int liters = (int) ((iVal - 21) / 3.5);
         val = "" + liters;
      }
      else  // Value given in liters
      {
         try { // Check if the string contains only numbers
            iVal = Integer.parseInt(val, 10);
         } catch(NumberFormatException e) {
            return "Mängden syre måste anges som ett heltal. " +
               "Anges det i % avsluta med ett %-tecken.";
         }

         if (iVal > maxVal || iVal < minVal) // Check that it's a valid number
            return "Mänden syre måste vara mellan " + minVal + " och " +
               maxVal + " liter";
      }

      try {    // Insert the new value
         values.setElementAt(val, col);
      } catch (ArrayIndexOutOfBoundsException e) {
         // Column doesn't exists, create it
         for (int i = values.size() ; i < col ; i++)
         {
            values.add(null);
            inDB.add(FALSE);
         }
         values.add(val);
         inDB.add(FALSE);
      }
      datachanged = true;

      return null;
   }

}
