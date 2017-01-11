import java.io.*;

/**
 * ExaminationDescriptionPacket contains the observations that 
 * are made for a examintation field such as remarks and description fields
 *
 * @author Kane Neman
 * @version 030118
 */ 
public class ExaminationDescriptionPacket implements Serializable {

    private int examinationTypeId;
    private String descriptionField;
    private int withoutRemark;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public ExaminationDescriptionPacket() {}

    /**
     * Constructor used by ambulance client to create this packet
     * @param examinationTypeId is the id for examination type 
     * @param descriptionField description for this examination
     * @param withoutRemark Remark along with this examination type
     */
    public ExaminationDescriptionPacket(int examinationTypeId, 
            String descriptionField, int withoutRemark) {
        
        this.examinationTypeId = examinationTypeId;
        this.descriptionField = descriptionField;
        this.withoutRemark = withoutRemark;
    }

    /**
     * Method used to get the examination type id
     * @return The examination type id
     */ 
    public int getExaminationTypeId() {
        return examinationTypeId;
    }

    /**
     * Method used to get the description of the examination
     * @return The description
     */
    public String getDescriptionField() {
        return descriptionField;
    }

    /**
     * Method used to get the remark indication
     * @return The value of the remark
     */ 
    public int getWithoutRemark() {
        return withoutRemark;
    }
}
