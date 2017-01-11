import java.io.*;
import java.sql.*;

/**
 * This class represent a record in the CURRENT_UPDATES or
 * HISTORY table in the server database.
 *
 * @author Kane Neman
 * @version 030228
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class Update implements Serializable {

    private Timestamp insertTime;
    private String query;

    /**
     * Constructor for creating an Update object
     * @param insertTime is the time the record was inserted into the 
     *                   server database
     * @param query is the SQL query stored in the server database
     */
    public Update(Timestamp insertTime, String query) {
        this.insertTime = insertTime;
        this.query = query;
    }

    /**
     * Method to get the query insert time 
     * @return the insert time for this query
     */
    public Timestamp getInsertTime() {
        return insertTime;
    }

    /**
     * Method to get the SQL query 
     * @return the SQL query
     */
    public String getQuery() {
        return query;
    }
}
