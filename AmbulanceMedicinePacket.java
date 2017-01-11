import java.sql.*;
import java.io.*;
import java.util.Vector;

/**
 * AmbulanceMedicinePacket is used to serialize the medicical part 
 * of a patient record in the ambulance client to the central server
 *
 * @author Kane Neman
 * @version 20030904
 */ 
public class AmbulanceMedicinePacket implements Serializable {

    private Vector medicines;

    /**
     * Constructor used by Record object to re-initialize this object
     */ 
    public AmbulanceMedicinePacket() {}

    /**
     * Constructor used by ambulance client to create an 
     * AmbulanceMedicinePacket
     * @param recordId is the record id for the patient record that 
     *                 will be send to the server
     * @param db2 is the database connection to the ambulance client database
     */
    public AmbulanceMedicinePacket(int recordId, DB2Connect db2) 
        throws SQLException {

            int givenMedicationId = 0;
            int medicineId;
            int dozage;
            Time givenTime;
            int givenById;
            String query;
            String userName;
            ResultSet medicineResult;
            ResultSet effectResult;
            ResultSet userResult;
            Vector effects;

            medicines = new Vector();

            //Fetch the GIVEN_MEDICATIONS 
            query = "SELECT * FROM EPR.GIVEN_MEDICATIONS " +
                "WHERE RECORD_ID = " + recordId;

            medicineResult = db2.dbQuery(query);

            while (medicineResult.next()) {

                /*givenMedicationId is used to get the related effects
                  to a medicine*/
                givenMedicationId = 
                    medicineResult.getInt("GIVEN_MEDICATION_ID");
                medicineId = medicineResult.getInt("MEDICINE_ID");
                dozage = medicineResult.getInt("DOZAGE");
                givenTime = medicineResult.getTime("GIVEN_TIME");
                givenById = medicineResult.getInt("GIVEN_BY");
                
                //A user not registered in the system
                if (givenById == 0) {
                    givenById = 4; //TODO 
                    userResult = db2.dbQuery(
                            "SELECT NAME FROM EPR.MEDICATION_GIVEN_BY " +
                           "WHERE GIVEN_MEDICATION_ID = " + 
                           givenMedicationId);
                    if (userResult.next())
                        userName = userResult.getString(1); //Fetch the name
                    else 
                        userName = "Annan";
                } else 
                    userName = null;
                
                //Fetch the MEDICINE_EFFECTS
                query = "SELECT * FROM EPR.MEDICINE_EFFECTS WHERE " +
                     "GIVEN_MEDICINE_ID = " + givenMedicationId;

                effectResult = db2.dbQuery(query);

                effects = new Vector();

                while (effectResult.next()) {
                    EffectPacket ep = 
                        new EffectPacket(effectResult.getTime("EFFECT_TIME"),
                                effectResult.getString("EFFECT"));
                    effects.add(ep);
                }

                MedicineEffectPacket mep = 
                    new MedicineEffectPacket(medicineId, dozage, givenTime,
                            givenById, effects, userName);

                medicines.add(mep);
            }
        }

    /**
     * Method used by Record object to write the given medications
     * to the server database
     * @param db2 is the connection to the server database
     * @param recordId is the record id at the server side
     */ 
    public void writeMedicinePacket(DB2Connect db2, int recordId) 
        throws SQLException {

            String query;

            //If no medicine is given don't do anything
            if (medicines.size() == 0)
                return;

            //Otherwise write the given medications to the server database
            for (int i = 0; i < medicines.size(); i++) {
                MedicineEffectPacket mep  = 
                    (MedicineEffectPacket) medicines.elementAt(i);

                //Write the given medication 
                query = "INSERT INTO EPR.GIVEN_MEDICATIONS " +
                    "(MEDICINE_ID, DOZAGE, RECORD_ID, GIVEN_TIME, GIVEN_BY) " +
                    "VALUES (" + mep.getMedicineId() + ", " + mep.getDozage() +
                    ", " + recordId + ", '" + mep.getGivenTime() + "', " +
                    mep.getTheGiverId() + ")";

                db2.dbQueryUpdate (query);

                //Fetch the generated id
                query = "SELECT GIVEN_MEDICATION_ID FROM " +
                    "EPR.GIVEN_MEDICATIONS WHERE RECORD_ID = " +
                    recordId + " AND MEDICINE_ID = " + mep.getMedicineId() + 
                    " AND GIVEN_TIME = '" + mep.getGivenTime() + "'"; 

                ResultSet resultSet = db2.dbQuery(query);
                resultSet.next();

                int givenMedicationId = resultSet.getInt("GIVEN_MEDICATION_ID");
                Vector effects = mep.getEffects();

                //Insert the related effects to this medicine
                for (int j = 0; j < effects.size(); j++) {

                    EffectPacket ep = (EffectPacket) effects.elementAt(j);

                    query ="INSERT INTO EPR.MEDICINE_EFFECTS " +
                        "(GIVEN_MEDICINE_ID, EFFECT_TIME, EFFECT) " 
                        + "VALUES (" + givenMedicationId + ", '" + 
                        ep.getEffectTime() + "', '" + 
                        ep.getEffectDescription() + "')";

                    db2.dbQueryUpdate (query);
                }

                //If the user is not registered in the system
                if (mep.getTheGiverId() == 4) { //TODO change 4 to 0
                    db2.dbQueryUpdate ("INSERT INTO EPR.MEDICATION_GIVEN_BY " +
                            "(NAME, GIVEN_MEDICATION_ID) VALUES ( '" +
                             mep.getUserName() + "', " + givenMedicationId +
                            ")");
                }
            }
        }
}
