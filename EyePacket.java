import java.sql.*;
import java.io.*;

/**
 * EyePacket contains the pupil observation for a certain time
 *
 * @author Kane Neman
 * @version 030309
 *
 */
public class EyePacket implements Serializable {

    private Time obsTime;
    private String left;
    private String right;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public EyePacket() { }

    /**
     * Constuctor used by ambulance client to create this packet
     * @param obsTime Time for this observation
     * @param left The pupil value for left eye
     * @param right  The pupil value for rigth eye
     */ 
    public EyePacket(Time obsTime, String left, String right) {

        this.obsTime = obsTime;
        this.left = left;
        this.right = right;
    }

    /**
     * Method used to get the observed time
     * @return The observation time
     */ 
    public Time getObsTime() {
        return obsTime;
    }

    /**
     * Method used to get the pupil value for left eye
     * @return The observation value for left eye
     */
    public String getLeft() {
        return left;
    }

    /**
     * Method used to get the pupil value for right eye
     * @return The observation value for right eye
     */
    public String getRight() {
        return right;
    }
}
