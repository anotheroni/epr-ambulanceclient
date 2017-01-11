import java.sql.*;
import java.io.*;

/**
 * BloodPressurePacket contains the blood pressure observation at a certain
 * time
 *
 * @author Kane Neman
 * @version 030309
 *
 */
public class BloodPressurePacket implements Serializable {

    private Time obsTime;
    private int diastol;
    private int systol;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public BloodPressurePacket() { }

    /**
     * Constuctor used by ambulance client to create this packet
     * @param obsTime Time for this observation
     * @param diastol The diastol part of the blood pressure
     * @param systol  The systol part of the blood pressure
     */ 
    public BloodPressurePacket(Time obsTime, int diastol, int systol) {

        this.obsTime = obsTime;
        this.diastol = diastol;
        this.systol = systol;
    }

    /**
     * Method used to get the observed time
     * @return The observation time
     */ 
    public Time getObsTime() {
        return obsTime;
    }

    /**
     * Method used to get the diastol value of blood pressure
     * @return The diastol value of blood pressure
     */
    public int getDiastol() {
        return diastol;
    }

    /**
     * Method used to get the systol value of blood pressure
     * @return The systol value of blood pressure
     */
    public int getSystol() {
        return systol;
    }
}
