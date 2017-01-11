import java.text.*;
import java.util.*;

/**
  * Class storing information about a medicine.
  *
  * @version 20030108
  * @author Oskar Nilsson
  */
public class Medicine
{
   private int id;
   private String name;
   private String form;
   private int size;
   private String unit;
   private double concentration;
   private String concUnit;

   private String longName;
  
   /**
     * Constructor, initilizes a medicine.
     * @param id The id of the medicine.
     * @param name The name of the medicine.
     * @param form The form the medicine is in, pill etc.
     * @param size The size of the medicine, 500 etc.
     * @param unti The unit of the size, mg etc
     */
   public Medicine(int id, String name, String form, int size, String unit,
         double concentration, String concUnit)
   {
      this.id = id;
      this.name = name;
      this.form = form;
      this.size = size;
      this.unit = unit;
      this.concentration = concentration;
      this.concUnit = concUnit;

      longName = name + " " + size + " " + unit + " (" + form + " " +
         concentration + " " + concUnit + ")";
   }

   /**
     * Method used to print medicine objects, prints name, size, unit and form.
     * @return A string containing name, size, unit and form.
     */
   public String toString()
   {
      return longName;
   }

   /**
     * Method that returns the id of the medicine.
     * @return The id of the medicine.
     */
   public int getId()
   {
      return id;
   }

   /**
     * Method that returns a string containing the amount of medicine.
     * @param amount The units of medicine given.
     * @return A string containing the total amount given and the unit.
     */
   public String printConc(int amount)
   {
      DecimalFormatSymbols dfs = 
         new DecimalFormatSymbols(new Locale("sv", "sw"));
      dfs.setDecimalSeparator('.');
      DecimalFormat df = new DecimalFormat("0.00", dfs);

      return df.format(concentration * amount) + " " + concUnit;
   }
}
