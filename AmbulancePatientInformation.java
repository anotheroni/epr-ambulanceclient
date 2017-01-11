import java.io.*;

/**
 * This object is used for serialization of patient information.
 *
 * @author Kane Neman
 * @version 030204
 */ 
public class AmbulancePatientInformation implements Serializable { 

    private String personNumber = null;
    private String[] personInformation;
    private String message;
    private boolean failed = false;

    /**
     * contructor used to re-initialize this object
     */  
    public AmbulancePatientInformation() {}

    /**
     * This constructor is used by Ambulance client to create
     * this packet
     * @param personNumber is the patient's person number
     */
    public AmbulancePatientInformation(String personNumber) {

        this.personNumber = personNumber;
    }

    /** 
     * Method called by server to get the person information
     * for the required patient
     */ 
    public void getPatientInformation() {

        //Create a reference to PersonInformation object
        PersonInformation pI = new PersonInformation();

        try {
            pI.personSearch(personNumber);
            personInformation = pI.getInformation();
            message = "PNR sökningen lyckades.";
        } catch(IllegalArgumentException g) {
            failed = true;
            message = "Kunde inte hitta personnumret.";
                //g.getMessage();
        }
    }

    /**
     * Method used to indicate the information retrieval status
     * @return True if the server failed with the information
     * retrieval otherwise false
     */ 
    public boolean informationFailed() {
        return failed;
    }        

    /**
     * Method used to get the status message
     * @return String containing the status message
     */ 
    public String getMessage() {
        return message;
    }

    /**
     * Method used to get the patient first name 
     * @return String containing the first name
     */
    public String getFirstName() {
        return personInformation[0];
    }

    /**
     * Method used to get the patient last name
     * @return String containing the last name
     */
    public String getLastName() {
        return personInformation[1];
    }

    /**
     * Method used to get the patient address
     * @return String containing the address
     */ 
    public String getAddress() {
        return personInformation[2];
    }

    /**
     * Method used to get the patient zip code
     * @return String containing the zip code
     */ 
    public String getZipCode() {
        return personInformation[3];
    }

    /**
     * Method used to get the patient city
     * @return String containing the city
     */ 
    public String getCity() {
        return personInformation[4];
    }

    /**
     * Method used to get the patient county
     * @return String containing the county
     */ 
    public String getCounty() {
        return personInformation[5];
    }
}


