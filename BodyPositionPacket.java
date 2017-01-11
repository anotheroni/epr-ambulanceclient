import java.sql.*;
import java.io.*;

/**
 * BodyPositionPacket contains the body position
 *
 * @author Kane Neman
 * @version 030310
 *
 */
public class BodyPositionPacket implements Serializable {

    private Time obsTime;
    private int position;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public BodyPositionPacket() { }

    /**
     * Constuctor used by ambulance client to create this packet
     * @param obsTime Time for this observation
     * @param position Body position id
     */ 
    public BodyPositionPacket(Time obsTime, int position) {

        this.obsTime = obsTime;
        this.position = position;
    }

    /**
     * Method used to get the observed time
     * @return The observation time
     */ 
    public Time getObsTime() {
        return obsTime;
    }

    /**
     * Method used to get the position id
     * @return The position id
     */
    public int getPosition() {
        return position;
    }
}

