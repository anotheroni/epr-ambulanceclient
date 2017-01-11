import java.sql.*;
import java.io.*;
import java.util.Vector;

/**
 * MedicineEffectPacket represent a medicine and its effects
 *
 * @author Kane Neman
 * @version 030409
 */ 
public class MedicineEffectPacket implements Serializable {

    private int medicineId;
    private int dozage;
    private Time givenTime;
    private int givenById;
    private Vector effects;
    private String userName;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public MedicineEffectPacket() { }


    /**
     * Constructor used by ambulance client to create this object
     * @param medicineId The medicine id
     * @param dozage The given amount of this medicine
     * @param givenTime The time that the medicine has been given
     * @param givenById The id of the person that gave the medicine
     * @param effects A Vector of the related effects for this medicine
     */ 
    public MedicineEffectPacket (int medicineId, int dozage, Time givenTime, 
            int givenById, Vector effects, String userName) {

        this.medicineId = medicineId;
        this.dozage = dozage;
        this.givenTime = givenTime;
        this.givenById = givenById;
        this.effects = effects;
        this.userName = userName;
    }

    /**
     * Method used to get the medicine id
     * @return The medicine id
     */
    public int getMedicineId() {
        return medicineId;
    }

    /**
     * Method used to get the medicine dozage
     * @return The medicine dozage
     */
    public int getDozage() {
        return dozage;
    }

    /**
     * Method used to get the time that this medicine has been given
     * @return The time that this medicine has been given
     */ 
    public Time getGivenTime() {
        return givenTime;
    }

    /**
     * Method used to get the id of the person who gave this medicine
     * @return The person id
     */
    public int getTheGiverId() {
        return givenById;
    }

    /**
     * Method used to get the related effects to this given medication
     * @return The related effects to this given medication
     */
    public Vector getEffects() {
        return effects;
    }

    public String getUserName() {
        return userName;
    }
}
