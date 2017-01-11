import java.util.*;
import java.io.*;

/**
 * Class that represent an error log object
 * 
 * @author Kane Neman
 * @version 030220
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class Log {

    private String message;
    private String lineSeparator = 
        (String) java.security.AccessController.doPrivileged(
        new sun.security.action.GetPropertyAction("line.separator"));
    private  Date d = new Date();

    /**
     * Constructor used by the server to create a header in the log file
     */ 
    public Log(String title) {
        message = "=============== " + title + " started: " + 
            d.toString() + " ===============" + lineSeparator;
    }

    /**
     * Constructor used to create an error log
     * @param error is the Exception message
     * @param method contains the soure of the exception
     * @param freeText is a description field
     */
    public Log(String error, String method, String freeText) {
        if (freeText == null) 
            message = d.toString() + lineSeparator +
                "\t Method: " + method + lineSeparator +
                "\t Error: " + error + lineSeparator;
        else 
            message = d.toString() + lineSeparator +
                "\t Method: " + method + lineSeparator +
                "\t Description: " + freeText + lineSeparator + 
                "\t Error: " + error + lineSeparator;
    }

    /**
     * Method used to get the String presentation of this object
     */
    public String toString() {
        return message;
    }
}

