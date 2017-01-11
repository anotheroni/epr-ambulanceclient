import java.util.Vector;

/**
  * Struct used to store observations.
  *
  * @version 20030128
  * @author Oskar Nilsson
  */
public class Print_Observation
{
   /// The group id of the examination type.
   public int type;
   /// Flag that signals that there are no remarks for the examination type.
   public boolean withoutRemark;
   /// A free text description for the examination type.
   public String description;
   /// The name of the examination type.
   public String typeName;
   /** Vector containing all examination attributes that belong to the
       examination type. */
   public Vector examinations = null;

   /**
     * Constructor initsialices the local struct variables.
     * @param type The group id of the examination type.
     * @param typeName The name of the group.
     * @param withoutRemark True if the without remark box is selected.
     * @param description The constents of the description text field.
     */
   public Print_Observation(int type, String typeName, boolean withoutRemark,
        String description)
   {
      this.type = type;
      this.typeName = typeName;
      this.withoutRemark = withoutRemark;
      this.description = description;
   }

   /**
     * Method that adds a new attribute to the examination type.
     * @param name The name of the attribute.
     */
   public void addExamination(String name)
   {
      if (examinations == null)
         examinations = new Vector();
      examinations.add(name);
   }
}
