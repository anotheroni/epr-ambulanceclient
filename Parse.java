import java.io.*;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.JDialog;

/**
 * Class for parsing the Mobitex data
 *
 * @author Kane Neman
 * @version 20030918
 */
public class Parse {

    private DB2Connect db2;
    private LogHandler lgHandler;
    private JDialog dialog;
    private ImportDialogInterface importInterface;
    
    private String param[] = {" : ", "MOTTAGEN TEXT", "FRÅN: ", "TILL: ", 
        "Ärendenummer", "Ht-förklaring", "PRIO", "Adress", "Zon-beskrivning",
        "Till adress", "Namn", "Personnummer", "Planerad hämtningstid",
        "Planerad avlämningstid", "Skapande tid", "IB nummer", "Hemadress",
        "Hemlandsting", "Besvär", "Anhörig", "K", "K1", "Koordinat", "X", "Y",
        "Slutrapport GPS SV", "Ärende", "Inkom", "Uppdrag", "Framme", "Lastat",
        "Lämnar", "Datum", "Prio"};

    //For simulation only
    /*private int fileCount = 0;
    private String file[] = {"info1.txt", "info2.txt", "info3.txt", 
        "time1.txt", "time2.txt", "time3.txt"};*/
    
    /**
     * Constructor used to initialize this object
     * @param db2 Reference to client database connection
     * @param lgHandler Reference to Error log handler
     */
    public Parse(DB2Connect db2, LogHandler lgHandler) {
        this.db2 = db2;
        this.lgHandler = lgHandler;

        //Load the library
        System.loadLibrary("mobitex");
    }

    /**
     * Method used to communicate with the USB hardware in C++
     */
    public native void communicate();

    /**
     * Method used to invoke import function in C++ side
     * @param dialog Reference to parent dialog 
     */
    public void importMessage(ImportInfoDialog dialog) {
        
        this.dialog = dialog;
        importInterface = dialog;
        communicate();
        
        //For simulation only
       /*String line, message = "";
        try { 
            BufferedReader reader = new BufferedReader(
                    new FileReader(file[fileCount]));
            fileCount++;
            while ((line = reader.readLine()) != null)
                message = message + line + "\n";

            parseMessage(message);
        } catch (IOException ie) {
            System.err.println("ERROR: " + ie.getMessage());
        }*/
    }
 
    /**
     * Method used to invoke import function in C++ side
     * @param dialog Reference to parent dialog 
     */
    public void importMessage(ImportTimeDialog dialog)
    {
        this.dialog = dialog;
        importInterface = dialog;
        //communicate();
    }
    
    /**
     * Method that will be invoked from the native side
     * @param message The received message from the hardware
     * @param error A boolean for detecting errors
     */
    public void parseMessage(String message, boolean error) {

       int index;

        //System.out.println("MESSAGE: " + message);

        //Error
        if (error) {
            JOptionPane.showMessageDialog(dialog,
                    //message,
                    "Kunde inte hitta Mobitex enheten",
                    "Fel vid import av meddelande",
                    JOptionPane.ERROR_MESSAGE);
            importInterface.returnState(false);
            return;
        }

        //Queue empty
        if (!error && message == null) {
            /*JOptionPane.showMessageDialog(dialog,
                    "Inga nya meddelanden");*/
            importInterface.returnState(false);
            return;
        }
        
        //If message doesn't contain anything 
        if (message == null) {
            importInterface.returnState(false);
            return;
        }

        //If an information report is recieved
        if ((index = message.indexOf(param[1])) != -1) {
           parseInfoMessage(message, index + param[1].length());
           importInterface.returnState(true);
        }
        //If a time message is recieved
        else if ((index = message.indexOf(param[25])) != -1) {
           parseMissionMessage(message);
           importInterface.returnState(true);
           return;
        } else {  //Any othe messages will be ignored
           importInterface.returnState(true);
           return; 
        }
    }
    
    /**
     * Method used to parse parameters and values in a 
     * Mobitex information message
     * @param index The last index of the "MOTTAGEN TEXT" in the 
     * received message
     * @param message The information message that will be parsed
     */
    public void parseInfoMessage(String message, int index) {
        
        String alarmDate = "", alarmTime = "", senderId = "";
        String receiverId = "";
        String commissionNumber = "", alarmCause = "";
        String address = "", zone = "";
        String toAddress = "", lastName = "", firstName = "";
        String personId = "", pickupTime = "";
        String dropOffTime = "", creationTime = "", ibNumber = "";
        String homeAddress = "";
        String homeCounty = "", anamnesis = "", relativeInfo = "";
        String k = "", k1 = "";
        String coordinate = "", xCord = "", yCord = "";
        int endIndex;
        int prio = 0, x = 0, y = 0;
        
        //Parse the alarm date and time
        index++;
        //Ignore the space and tab characters
        while ((message.charAt(index) == ' ') || 
                                            (message.charAt(index) == '\t'))
            index++;
        
        endIndex = index;
        
        while (message.charAt(endIndex) != ' ')
            endIndex++;

        //parse alarm date
        alarmDate = message.substring(index, endIndex);
        
        //skip space
        endIndex++;
        
        //parse alarm time
        alarmTime = message.substring(endIndex, endIndex + 5);

//        System.out.println("Date:" + alarmDate);
//        System.out.println("Time:" + alarmTime);

        //Parse sender id
        if ((index = message.indexOf(param[2])) != -1) {
            index = index + param[2].length();
            senderId = message.substring(index, index + 6);
//            System.out.println("SenderId:" + senderId);
        }

        //Parse receiver id
        if ((index = message.indexOf(param[3])) != -1) {
            index = index + param[3].length();
            receiverId = message.substring(index, index + 6);
//            System.out.println("ReceiverId:" + receiverId);
        }
                
        //Split the message according to the new line character
        String splitNewLine[] = message.split("\n");
        String tempMessage;
        String splitColon[];
        for (int i = 0; i < splitNewLine.length; i++) {
            //If the message contains the string " : "
            if ((splitNewLine[i].indexOf(param[0])) != -1) {
                tempMessage = splitNewLine[i];
                //Search for parameter names and values 
                if ((i + 1) < splitNewLine.length) {
                    /*If a parameter value is divided into several lines,
                      concatenate these lines together*/
                    for (int j = i + 1; j < splitNewLine.length; j++) {
                        if ((splitNewLine[j].indexOf(param[0])) != -1)
                            break;
                        else
                            tempMessage = tempMessage + splitNewLine[j];
                    }
                }
                //Split the parameter name and value
                splitColon = tempMessage.split(param[0]);
                if (param[4].equals(splitColon[0])) {//Ärendenummer
                    commissionNumber = splitColon[1].trim();
                    /*If commission number is more than 12 charracters
                      store only the 12 one*/
                    if (commissionNumber.length() > 12)
                        commissionNumber = commissionNumber.substring(0, 13);
//                    System.out.println("Commission:" + commissionNumber);
                } else if (param[5].equals(splitColon[0])) {//Ht-förklaring
                    splitColon[1] = parseTabAndSpace(splitColon[1], false);
                    //Find the first index of "PRIO"
                    if ((index = splitColon[1].indexOf(param[6])) != -1) {
                        //Find the prio number
                        String p = splitColon[1].substring(
                                index + param[6].length() + 1, 
                                index + param[6].length() + 2);
                        //store the last index
                        endIndex = index + param[6].length() + 1;
                        try {
                            prio = Integer.parseInt(p);
                            //Find the alarm cause, if any 
                            if ((index = splitColon[1].indexOf(" ", endIndex)) 
                                                                       != -1) {
                                alarmCause = splitColon[1].substring(
                                        index + 1,
                                        splitColon[1].length());
                            }
                        } catch (NumberFormatException nfe) {
                            alarmCause = 
                                parseTabAndSpace(splitColon[1].trim(), true);
                        }
                    } else
                        alarmCause = 
                            parseTabAndSpace(splitColon[1].trim(), true);
                    
                        //If alarm cause is more than 64, cut it
                        if (alarmCause.length() > 64)
                                alarmCause = alarmCause.substring(0, 64);
                    
//                    System.out.println("PRIO:" + prio);
//                    System.out.println("Ht-förklaring:" + alarmCause);
                } else if (param[7].equals(splitColon[0])) {//Adress
                    address = parseTabAndSpace(splitColon[1].trim(), true);
                    //If address is longer than 64 characters, cut it
                    if (address.length() > 64)
                        address = address.substring(0, 64);
//                    System.out.println("Address:" + address);
                } else if (param[8].equals(splitColon[0])) {//Zon-beskrivning
                    zone = parseTabAndSpace(splitColon[1].trim(), true);
                    //If zone is longer than 64, cut it
                    if (zone.length() > 64)
                        zone = zone.substring(0, 64);
//                    System.out.println("Zon-beskrivning:" + zone);
                } else if (param[9].equals(splitColon[0])) {//Till address
                    toAddress = parseTabAndSpace(splitColon[1].trim(), true);
                    if (toAddress.length() > 64)
                        toAddress = toAddress.substring(0, 64);
//                    System.out.println("Till adress:" + toAddress);
                } else if (param[10].equals(splitColon[0])) {//Namn
                    String temp = parseTabAndSpace(splitColon[1].trim(), true);
                    //If there is a first and last name
                    if ((index = temp.indexOf(" ")) != -1) {
                        lastName = temp.substring(0, index);
                        firstName = temp.substring(index + 1, temp.length());
                        //Cut first name to proper length
                        if (firstName.length() > 32)
                            firstName = firstName.substring(0, 32);
//                        System.out.println("Last name:" + lastName);
//                        System.out.println("First name:" + firstName);
                    } else { //If only first or last name exists
                        lastName = temp;
//                        System.out.println("Last or first name:" + lastName);
                    }
                     //Cut last name to proper length
                        if (lastName.length() > 32)
                            lastName = lastName.substring(0, 32);
                        
                } else if (param[11].equals(splitColon[0])) {//Personnummer
                    personId = parseTabAndSpace(splitColon[1].trim(), true);
                    //If person number contains "-" character, remove it
                    if ((index = personId.indexOf("-")) != -1) {
                        if (index != 0) {
                            personId = "19" + personId.substring(0, index) 
                                       + personId.substring(index + 1, 
                                                          personId.length());
                        }
                    }
                    //Cut the person id
                    if (personId.length() > 12)
                        personId = personId.substring(0, 12);
//                    System.out.println("Personnummer:" + personId);
                } else if (param[12].equals(splitColon[0])) {//hämtningstid
                    pickupTime = splitColon[1].trim();
                    if(pickupTime.length() > 5)
                        pickupTime = pickupTime.substring(0, 5);
//                    System.out.println("Planerad hämtningstid:" + pickupTime);
                } else if (param[13].equals(splitColon[0])) {//avlämningstid
                    dropOffTime = splitColon[1].trim();
                    if (dropOffTime.length() > 5)
                        dropOffTime = dropOffTime.substring(0, 5);
//                    System.out.println("Planerad avlämningstid:" + 
//                            dropOffTime);
                } else if (param[14].equals(splitColon[0])) {//Skapande tid
                    creationTime = splitColon[1].trim();
                    if (creationTime.length() > 8)
                        creationTime = creationTime.substring(0, 8);
//                    System.out.println("Skapande tid:" + creationTime);
                } else if (param[15].equals(splitColon[0])) {//IB nummer
                    ibNumber = splitColon[1].trim();
                    if (ibNumber.length() > 3)
                        ibNumber = ibNumber.substring(0, 3);
//                    System.out.println("IB number:" + ibNumber);
                } else if (param[16].equals(splitColon[0])) {//Hemadress
                    homeAddress = parseTabAndSpace(splitColon[1].trim(), true);
                    if (homeAddress.length() > 64)
                        homeAddress = homeAddress.substring(0, 64);
//                    System.out.println("Hemadress:" + homeAddress);
                } else if (param[17].equals(splitColon[0])) {//Hemlandsting
                    homeCounty = splitColon[1].trim();
                    if (homeCounty.length() > 2)
                        homeCounty = homeCounty.substring(0, 2);
//                    System.out.println("Hemlandsting:" + homeCounty);
                } else if (param[18].equals(splitColon[0])) {//Besvär
                    anamnesis = parseTabAndSpace(splitColon[1].trim(), true);
                    if (anamnesis.length() > 64)
                        anamnesis = anamnesis.substring(0, 64);
//                    System.out.println("Besvär:" + anamnesis);
                } else if (param[19].equals(splitColon[0])) {//Anhörig
                    relativeInfo = 
                        parseTabAndSpace(splitColon[1].trim(), true);
                    if (relativeInfo.length() > 64)
                        relativeInfo = relativeInfo.substring(0, 64);
//                    System.out.println("Anhörig:" + relativeInfo);
                } else if (param[20].equals(splitColon[0])) { //K
                    k = parseTabAndSpace(splitColon[1].trim(), false);
                    if (k.length() > 64)
                        k = k.substring(0, 64);
//                    System.out.println("K:" + k);
                } else if (param[21].equals(splitColon[0])) { //K1
                    k1 = parseTabAndSpace(splitColon[1].trim(), false);
                    if (k1.length() > 64)
                        k1 = k1.substring(0, 64);
//                    System.out.println("K1:" + k1);
                } else if (param[22].equals(splitColon[0])) { //Koordinat
                    coordinate = parseTabAndSpace(splitColon[1].trim(), true);
//                    System.out.println("Koordinat:" + coordinate);
                    //X-coordinate
                    if ((index = coordinate.indexOf(param[23])) != -1) {
                        endIndex = coordinate.indexOf(" ");
                        if (endIndex != -1)
                            xCord = coordinate.substring(index + 2, endIndex);
                        else
                            xCord = coordinate.substring(index + 2, 
                                                         coordinate.length());
                        try {
                            x = Integer.parseInt(xCord);
                        } catch (NumberFormatException nfe) {}
                    }

                    //Y-coordinate
                    if ((index = coordinate.indexOf(param[24])) != -1) {
                        endIndex = coordinate.indexOf(" ", index);
                        if (endIndex != -1)
                            yCord = coordinate.substring(index + 2, endIndex);
                        else
                            yCord = coordinate.substring(index + 2, 
                                                         coordinate.length());
                        try {
                            y = Integer.parseInt(yCord);
                        } catch (NumberFormatException nfe) {}
                    }
//                    System.out.println("X:" + x);
//                    System.out.println("Y:" + y);
                }
            }//If
        }//for

        //Insert the parameters into the client database
        String query = "INSERT INTO EPR.MOBITEX_INFORMATION " +
            "(COMMISSION_NUMBER, ALARM_CAUSE, PRIORITY_OUT, " +
            "ACCIDENT_ADDRESS, ZONE, DROPOFF_ADDRESS, FIRST_NAME, " +
            "LAST_NAME, PERSON_ID, PLANED_PICKUP_TIME, PLANED_DROPOFF_TIME, " +
            "CREATION_TIME, IB_NUMBER, HOME_ADDRESS, HOME_COUNTY, " +
            "ANAMNESIS, RELATIVE_INFORMATION, COMMENT_ONE, COMMENT_TWO, " +
            "X_COORDINATE, Y_COORDINATE) VALUES ('" + commissionNumber + 
            "', '" + alarmCause + "', " + prio + ", '" + address + "', '" +
            zone + "', '" + toAddress + "', '" + firstName + "', '" + 
            lastName + "', '" + personId + "', '" + pickupTime + "', '" +
            dropOffTime + "', '" + creationTime + "', '" + ibNumber + "', '" +
            homeAddress + "', '" + homeCounty + "', '" + anamnesis + "', '" +
            relativeInfo + "', '" + k + "', '" + k1 + "', " + x + ", " + y + 
            ")";
        
        try {
            db2.dbQueryUpdate (query);
        } catch (SQLException sql) {
            JOptionPane.showMessageDialog(dialog,
                    "Misslyckades med att lagra meddelandet i databasen",
                    "DatabasFel",
                    JOptionPane.ERROR_MESSAGE);
            
            lgHandler.addLog(new Log(sql.getMessage(), 
                        "Parse/parseInfoMessage", 
                        "Failed to insert Mobitex information " +
                        "message into database"));
            return;
        }
        
        /*JOptionPane.showMessageDialog(dialog,
                "Meddelandet är lagrad i databasen");*/
    }

    /**
     * Method used to remove space, tab or ","  characters
     * @param paramValue String containing the mentioned characters
     * @param filterComa Boolean used to indicate if "," characters 
     * should be filtered or not. True indicates to filter the "," 
     * characters otherwise flase
     * @return An String that is free from mentioned characters
     */
    public String parseTabAndSpace(String paramValue, boolean filterComa) {
        
        String splitArray[];
        String value = "";
      
        //if no characters or tabs founded return the string as it is
        if (((paramValue.indexOf(" ")) == -1) && 
                                    ((paramValue.indexOf("\t")) == -1))
            return paramValue;
        
        //Replace all tabs with spaces
        paramValue = paramValue.replaceAll("\t", " ");
        if (filterComa)
            paramValue = paramValue.replaceAll(",", " ");
        
        //split the string in chunks
        splitArray = paramValue.split(" ");
        
        for (int i = 0; i < splitArray.length; i++) {
            if (splitArray[i].equals(""))
                    continue;
            
            if ((i + 1) < splitArray.length)
                value = value + splitArray[i] + " ";
            else 
                value = value + splitArray[i];
        }
        return value;
    }
    
    /**
     * Method used to parse parameters and values in a 
     * Mobitex time message
     * @param message The time message that will be parsed
     */
    public void parseMissionMessage(String message) {

        String commission = null, alarmDate = null, alarmTime = null;
        String leftStationTime = null;
        String arrivalTime = null, loadTime = null, handoverTime = null;
        String prio = null;
        int prioOut = 0, prioIn = 0, index, endIndex;

        //Remove all newline, tab etc
        message = parseTabAndSpace(message.replaceAll("\n", " "), true);

        //Parse alarm date
        if ((index = message.indexOf(param[32])) != -1) {//Datum
            alarmDate = message.substring(index + param[32].length() + 1,
                    index + param[32].length() + 11);
//            System.out.println("Datum:" + alarmDate);
        }

        //Parse commission number
        if ((index = message.indexOf(param[26])) != -1) {//Ärende
            commission = message.substring(index + param[26].length() + 1,
                    index + param[26].length() + 13);
//            System.out.println("Ärende:" + commission);
        }

        //Parse alarm date and time
        if ((index = message.indexOf(param[27])) != -1) {//Inkom
            alarmDate = message.substring(index + param[27].length() + 1,
                    index + param[27].length() + 11);
//            System.out.println("Inkom datum:" + alarmDate);

            alarmTime = message.substring(index + param[27].length() + 12,
                    index + param[27].length() + 20);
//            System.out.println("Inkom tid:" + alarmTime);
        }

        //Parse left station time
        if ((index = message.indexOf(param[28])) != -1) {//Uppdrag
            endIndex = index + param[28].length() + 9;
            leftStationTime = message.substring(index + param[28].length() + 1,
                    endIndex);
//            System.out.println("Uppdrag:" + leftStationTime);

            //Parse priority out if there is any
            if ((index = message.indexOf(param[33], endIndex)) != -1) {
                prio = message.substring(index + param[33].length() + 1,
                        index + param[33].length() + 2);

                try {
                    prioOut = Integer.parseInt(prio);
                } catch (NumberFormatException nfe) {}
//                System.out.println("Prio ut:" + prioOut);
            }
        }

        //Parse arrival time
        if ((index = message.indexOf(param[29])) != -1) {//Framme
            arrivalTime = message.substring(index + param[29].length() + 1,
                    index + param[29].length() + 9);
//            System.out.println("Framme:" + arrivalTime);
        }

        //Parse load time and priority in
        if ((index = message.indexOf(param[30])) != -1) {//Lastat
            endIndex = index + param[30].length() + 9;
            loadTime = message.substring(index + param[30].length() + 1,
                    endIndex);
//            System.out.println("Lastat:" + loadTime);

            if ((index = message.indexOf(param[33], endIndex)) != -1) {
                prio = message.substring(index + param[33].length() + 1,
                        index + param[33].length() + 2);

                try {
                    prioIn = Integer.parseInt(prio);
                } catch (NumberFormatException nfe) {}

//                System.out.println("Prio in:" + prioIn);
            }
        }

        //Parse handover time
        if ((index = message.indexOf(param[31])) != -1) {//Lämnar
            handoverTime = message.substring(index + param[31].length() + 1,
                    index + param[31].length() + 9);
//            System.out.println("Lämnar:" + handoverTime);
        }

        //Insert the parameters into client database
        String query = "INSERT INTO EPR.MOBITEX_TIME " +
            "(COMMISSION_NUMBER, DATE, ALARM, LEFT_STATION_TIME, " +
            "PRIORITY_OUT, ARRIVAL, LOADED, PRIORITY_IN, HANDOVER) VALUES " + 
            "('" + commission + "', ";
            
        if (alarmDate == null)
            query = query + null + ", ";
        else 
            query = query + "'" + alarmDate + "', ";

        if (alarmTime == null)
            query = query + null + ", ";
        else 
            query = query + "'" + alarmTime + "', ";

        if (leftStationTime == null)
            query = query + null + ", " + prioOut + ", ";
        else 
            query = query + "'" + leftStationTime + "', " + prioOut + ", ";

        if (arrivalTime == null)
            query = query + null + ", ";
        else 
            query = query + "'" + arrivalTime + "', ";

        if (loadTime == null) 
            query = query + null + ", " + prioIn + ", ";
        else 
            query = query + "'" + loadTime + "', " + prioIn + ", ";

        if (handoverTime == null)
            query = query + null + ")";
        else 
            query = query + "'" + handoverTime + "')" ;
        
        try {
            db2.dbQueryUpdate (query);
        } catch (SQLException sql) {
            JOptionPane.showMessageDialog(dialog,
                    "Misslyckades med att lagra meddelandet i databasen",
                    "DatabasFel",
                    JOptionPane.ERROR_MESSAGE);

            lgHandler.addLog(new Log(sql.getMessage(), 
                        "Parse/parseInfoMessage", 
                        "Failed to insert Mobitex time " +
                        "message into database"));
            return;
        }

        /*JOptionPane.showMessageDialog(dialog,
                "Meddelandet är lagrad i databasen");*/
    }
}
