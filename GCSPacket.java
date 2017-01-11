import java.sql.*;
import java.io.*;

/**
 * GCSPacket contains the Glascow Coma Scale observations 
 *
 * @author Kane Neman
 * @version 030309
 *
 */
public class GCSPacket implements Serializable {

    private Time obsTime;
    private int eyes;
    private int motor;
    private int verbal;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public GCSPacket() { }

    /**
     * Constuctor used by ambulance client to create this packet
     * @param obsTime Time for this observation
     * @param eyes The GCS value for eye observation
     * @param motor The GCS value for motor activity observation
     * @param verbal The GCS value for verbal observation
     */
    public GCSPacket(Time obsTime, int eyes, int motor, int verbal) {

        this.obsTime = obsTime;
        this.eyes = eyes;
        this.motor = motor;
        this.verbal = verbal;
    }

    /**
     * Method used to get the observed time
     * @return The observation time
     */ 
    public Time getObsTime() {
        return obsTime;
    }

    /**
     * Method used to get the eye parameter value of this observation
     * @return The eye parameter value
     */
    public int getEyeValue() {
        return eyes;
    }

    /**
     * Method used to get the motor activity value of this observation
     * @return The motor activity parameter value
     */
    public int getMotorValue() {
        return motor;
    }

    /**
     * Method used to get the verbal value of this observation
     * @return The verbal parameter value
     */ 
    public int getVerbalValue() {
        return verbal;
    }
}
