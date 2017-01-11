import java.sql.*;
import java.io.*;

/**
 * ParameterPacket contains the regular patient parameter observations such 
 * as saturation, breathing frequency, diabetes, Visuell Analog Scale (VAS),
 * RLS and pulse
 *
 * @author Kane Neman
 * @version 030309
 *
 */
public class ParameterPacket implements Serializable {

    private Time obsTime;
    private int obsParamId;
    private String paramValue;

    /**
     * Constructor used to re-initialize this object at server side
     */ 
    public ParameterPacket() { }

    /**
     * Constuctor used by ambulance client to create this packet
     * @param obsTime Time for this observation
     * @param obsParamId Observation parameter id
     * @param paramValue The value for the observation
     */ 
    public ParameterPacket(Time obsTime, int obsParamId, String paramValue) {

        this.obsTime = obsTime;
        this.obsParamId = obsParamId;
        this.paramValue = paramValue;
    }

    /**
     * Method used to get the observed time
     * @return The observation time
     */ 
    public Time getObsTime() {
        return obsTime;
    }

    /**
     * Method used to get the parameter id
     * @return The parameter id
     */
    public int getParamId() {
        return obsParamId;
    }

    /**
     * Method used to get the value of this observation
     * @return The value of observed parameter
     */
    public String getParamValue() {
        return paramValue;
    }
}
