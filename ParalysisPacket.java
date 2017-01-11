import java.sql.*;
import java.io.*;

/**
 * ParalysisPacket contains the paralysis observation
 *
 * @author Kane Neman
 * @version 030310
 *
 */
public class ParalysisPacket implements Serializable {

    private int rightSide;
    private int leftSide;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public ParalysisPacket() { }

    /**
     * Constuctor used by ambulance client to create this packet
     * @param rightSide The right side observation of the patient body
     * @param leftSide The left side observation of the patient body 
     */ 
    public ParalysisPacket(int rightSide, int leftSide) {

        this.rightSide = rightSide;
        this.leftSide = leftSide;
    }

    /**
     * Method used to get the right side observation
     * @return The id for paralysis area
     */ 
    public int getRightSide() {
        return rightSide;
    }

    /**
     * Method used to get the left side observation
     * @return The id for paralysis area
     */
    public int getLeftSide() {
        return leftSide;
    }
}

