import java.sql.*;
import java.io.*;

/**
 * EffectPacket contains the effects for a given medication
 *
 * @author Kane Neman
 * @version 030113
 */ 
public class EffectPacket implements Serializable {

    private Time effectTime;
    private String effect;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public EffectPacket() { }

    /**
     * Constuctor used by ambulance client to create an EffectPacket object
     * @param effectTime is the observed time for this effect
     * @param effect is the effect's description
     */ 
    public EffectPacket(Time effectTime, String effect) {

        this.effectTime = effectTime;
        this.effect = effect;
    }

    /**
     * Method used to get the observed effect time
     * @return The observed effect time
     */ 
    public Time getEffectTime() {
        return effectTime;
    }

    /**
     * Method used to get this effect's description
     * @return The effect's description
     */
    public String getEffectDescription() {
        return effect;
    }
}
