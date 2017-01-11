import javax.swing.*;
import javax.swing.border.*;
import java.awt.print.*; 
import java.awt.*;
import java.sql.*;
import java.text.*;
import java.util.*;

/**
  * Class implementing the panel that is printed.
  *
  * @implements MessageInterface needed to be able to use ParameterTableModel.
  *
  * @version 20030716
  * @author Oskar Nilsson
  */
public class PrintPanel extends JPanel implements Printable, MessageInterface
{
   private DB2Connect dbcon;
   private LogHandler lg;
   private ParameterTableModel paramTModel;
   private int recordId;
 
   private int width = 451, height = 697;

   private static final Font bold15 = new Font(null, Font.BOLD, 15);
   private static final Font bold12 = new Font(null, Font.BOLD, 12);
   private static final Font norm10 = new Font(null, 0 ,10);
   private static final FontMetrics fm10 = 
      Toolkit.getDefaultToolkit().getFontMetrics(norm10);
   private static final Font bold10 = new Font(null, Font.BOLD, 10);
   private static final FontMetrics fmb10 =
      Toolkit.getDefaultToolkit().getFontMetrics(bold10);
   private static final Font norm8 = new Font(null, 0, 8);
 
   private static final int STD_ROW_HEIGHT = 11;
   private static final int EXT_ROW_HEIGHT = 13;
   private static final int EXX_ROW_HEIGHT = 16;

   private static final int ROW_WIDTH = 50;
   private static final int ROW_HEAD_WIDHT = 80;

   private static final int[] EXAMINATION_HEADER_WIDTH = {
      0, 90, 90, 60, 145, 20, 80, 90, 55 };

   private static final int[] FRACTURE_X_POS = {
      0, 56, 34, 27, 22,  16,  78, 82, 85,  91,  45, 67, 159, 54, 44,  43,
      45,  65,  63,  61};
   private static final int[] FRACTURE_Y_POS = {
      0, 22, 59, 82, 113, 150, 59, 82, 113, 147, 77, 77, 79, 134, 173, 224,
      259, 173, 224, 259};
 
   private static final int[] LUXATION_X_POS = {
      0, 135, 128, 183, 188, 147, 151, 155, 173, 171, 170};
   private static final int[] LUXATION_Y_POS = {
      0, 57,  101, 57,  101, 131, 198, 250, 131, 198, 250};
     
   private static final String VLLIMAGE = "images/vll.gif";
   private static final String BODYIMAGE = "images/figure.gif";

   private Image vllImage;
   private Image bodyImage;

   private String station = null;
   private String recordName = null;

   private String pNumber;
   private String name;
   private String address;
   private String relative;

   private String alarmTime;
   private String pickupTime;
   private String dropofTime;
   
   private Vector medVec = null; 
   private Vector allergyVec = null;
   private Vector anamnesisVec = null;
   private Vector diagnosisVec = null;
   private Vector foodVec = null;

   private Print_Observation[] examinationGroups;
   private boolean examinationExists = false;

   private Vector actionVec = null;
   private Vector actionDescVec = null;

   private Vector medicinesVec;
   private String doctorStr;

   private StringBuffer carerStr;
   private StringBuffer driverStr;
   private java.util.Date signtime;

   private StringBuffer diagnosisStr;
   private String alarmCouseStr;
   
   private SimpleDateFormat dateFormat;
   private SimpleDateFormat timeFormat;

   private Vector bodyTextVec = null;
   private int perfPuls[] = {0, 0, 0, 0};

   private Vector miscTextVec = null;

   private int currentPage = 1;
   private int numOfPages;
   
   private int examinationPage;
   private int actionPage;
   private int medicinePage;
   private int parameterPage;
   private int parameterPage2;
   private int bodyPage;
   private int miscPage;

   /**
     * Constructor, reads patient record information from the database and
     * prepares for the paint method.
     * @param dbcon A reference to the database.
     * @param lg The log handler to report errors to.
     * @param recordId ID of the record to print.
     */
   public PrintPanel(DB2Connect dbcon, LogHandler lg, int recordId)
   {
      super();
      this.dbcon = dbcon;
      this.lg = lg;
      this.recordId = recordId;

      int temp = 0;
      int rows;
      ResultSet rs;
      Print_Observation po;

      setBackground(Color.white);
 
      vllImage = Toolkit.getDefaultToolkit().getImage(VLLIMAGE);
      bodyImage = Toolkit.getDefaultToolkit().getImage(BODYIMAGE);

      DecimalFormatSymbols dfs = 
         new DecimalFormatSymbols(new Locale("sv", "sw"));
      dfs.setDecimalSeparator('.');
      DecimalFormat df = new DecimalFormat("0.00", dfs);

      carerStr = new StringBuffer(80);
      carerStr.append("Vårdare: ");
      driverStr = new StringBuffer(80);
      driverStr.append("Förare: ");
      diagnosisStr = new StringBuffer(60);

      dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      timeFormat = new SimpleDateFormat("HH:mm:ss");
      try {
         rs = dbcon.dbQuery("SELECT ar.DATE, ar.ALARM_TIME, " +
               "ar.ARRIVAL_ACCIDENT_TIME, ar.HAND_OVER_TIME, " +
               "ar.DELEGATING_DOCTOR, s.STATION_NAME, " +
               "s.PHONE_NUMBER, drv.FIRST_NAME, drv.LAST_NAME, " +
               "car.FIRST_NAME, car.LAST_NAME, ar.SIGN_TIME, " +
               "ar.ALARM_CAUSE, dt.DIAGNOSIS_TYPE_NAME, " +
               "dg.DIAGNOSIS_GROUP_NAME " +
               "FROM EPR.AMBULANCE_RECORD AS ar, EPR.STAFF AS car, " +
               "EPR.STAFF AS drv, EPR.STATION AS s, " +
               "EPR.DIAGNOSIS_TYPE AS dt, EPR.DIAGNOSIS_GROUP AS dg " +
               "WHERE ar.CARER_ID = car.STAFF_ID AND " +
               "ar.DRIVER_ID = drv.STAFF_ID AND " +
               "s.STATION_ID = ar.STATION_ID AND " +
               "ar.DIAGNOSIS_ID = dt.DIAGNOSIS_TYPE_ID AND " +
               "dt.DIAGNOSIS_GROUP_ID = dg.DIAGNOSIS_GROUP_ID AND " +
               "ar.RECORD_ID = " + recordId);
         rs.next();
         recordName = dateFormat.format(rs.getDate(1)) + " " + // DATE
            timeFormat.format(rs.getTime(2));   // ALARM_TIME
         station = "Station: " + rs.getString(6) + " tel: " +  // STATION_NAME
            rs.getString(7);  // PHONE_NUMBER
         alarmTime = (rs.getTime(2) == null) ?
            "" : rs.getString(2);   // ALARM_TIME
         pickupTime = (rs.getTime(3) == null) ?
            "" : rs.getString(3);   // ARRIVAL_ACCIDENT_TIME
         dropofTime = (rs.getTime(4) == null) ?
            "" : rs.getString(4);   // HAND_OVER_TIME
         driverStr.append(rs.getString(8)).append(" ").append(rs.getString(9));
         carerStr.append(rs.getString(10)).append(" ").append(rs.getString(11));
         signtime = (java.util.Date) rs.getTime(12);    // SIGN_TIME
         alarmCouseStr = rs.getString(13);   // ALARM_CAUSE
         diagnosisStr.append(rs.getString(15)).append(" / "). // DIAG_GROUP_N
            append(rs.getString(14));  // DIAGNOSIS_TYPE_NAME
         doctorStr = rs.getString(5);  // DELEGATING_DOCTOR
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read record information"));
         if (station == null)
            station = "Okänd station";
      }

      // Patient info
      try {
         rs = dbcon.dbQuery("SELECT PERSON_ID, FIRST_NAME, LAST_NAME, " +
               "ADDRESS, RELATIVE_INFORMATION FROM EPR.PATIENT WHERE " +
               "RECORD_ID = " + recordId);
         rs.next();
         pNumber = rs.getString(1); // PERSON_ID
         name = rs.getString(2) +   // FIRST_NAME
            " " + rs.getString(3);  // LAST_NAME
         address = rs.getString(4); // ADDRESS
         relative = rs.getString(5);// RELATIVE_INFORMATION
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read patient information"));
      }
 
      // MATAS
      try {
         rs = dbcon.dbQuery("SELECT MEDICINE, ALLERGY, ANAMNESIS, DIAGNOSIS, " +
               "LAST_MEAL FROM EPR.MATAS WHERE RECORD_ID = " + recordId);
         rs.next();
         medVec = parseString(rs.getString(1), width-10, fm10);   // MEDICINE
         allergyVec = parseString(rs.getString(2), width-10, fm10);  // ALLERGY
         anamnesisVec = parseString(rs.getString(3), width-10, fm10);//ANAMNESIS
         diagnosisVec = parseString(rs.getString(4), width-10, fm10);//DIAGNOSIS
         foodVec = parseString(rs.getString(5), width-10, fm10);  // LAST_MEAL
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read matas"));
      }

      // Examination Description (group)
      examinationGroups = new Print_Observation[10];  // TODO fix real size
      try {
         rs = dbcon.dbQuery("SELECT ED.EXAMINATION_TYPE_ID, " +
               "ED.DESCRIPTION_FIELD, ED.WITHOUT_REMARK, " +
               "ET.EXAMINATION_TYPE_NAME FROM " +
               "EPR.EXAMINATION_DESCRIPTION AS ED, " +
               "EPR.EXAMINATION_TYPES AS ET " +
               "WHERE RECORD_ID = " + recordId +
               " AND ED.EXAMINATION_TYPE_ID = ET.EXAMINATION_TYPE_ID " +
               "ORDER BY EXAMINATION_TYPE_ID");
         while (rs.next())
         {
            examinationExists = true;
            examinationGroups[rs.getInt(1)] = // EXAMINATION_TYPE_ID
                  new Print_Observation(
                     rs.getInt(1),  // EXAMINATION_TYPE_ID
                     rs.getString(4),  // EXAMINATION_TYPE_NAME
                     rs.getInt(3) == 1,   // WITHOUT_REMARK
                     rs.getString(2)); // DESCRIPTION_FIELD
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read examination"));
      }
      // Examination (attributes)
      try {
         rs = dbcon.dbQuery("SELECT EA.ATTRIBUTE_NAME, " +
               "EA.EXAMINATION_TYPE_ID, ET.EXAMINATION_TYPE_NAME "+
               "FROM EPR.EXAMINATION AS E, EPR.EXAMINATION_ATTRIBUTES AS EA, " +
               "EPR.EXAMINATION_TYPES AS ET " +
               "WHERE RECORD_ID = " + recordId +
               " AND E.EXAMINATION_ATTRIBUTE = EA.ATTRIBUTE_ID" +
               " AND ET.EXAMINATION_TYPE_ID = EA.EXAMINATION_TYPE_ID");
         while (rs.next())
         {
            examinationExists = true;
            po = examinationGroups[rs.getInt(2)];  // EXAMINATION_TYPE_ID
            if (po == null)   // The group doesn't exist, create it
            {
               po = new Print_Observation(rs.getInt(2),  //EXAMINATION_TYPE_ID
                     rs.getString(3), false, null); // EXAMINATION_TYPE_NAME
               examinationGroups[po.type] = po;
            }
            po.addExamination(rs.getString(1)); // ATTRIBUTE_NAME
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read examination attributes"));
      }

      // Examination - Paralysis
      try {
         rs = dbcon.dbQuery("SELECT RIGHT_SIDE, LEFT_SIDE FROM EPR.PARALYSIS " +
               "WHERE RECORD_ID = " + recordId);
         if (rs.next())
         {
            po = examinationGroups[8];  // Neurology EXAMINATION_TYPE_ID
            if (po == null)   // The group doesn't exist, create it
            {
               po = new Print_Observation(8,  // Neurology EXAMINATION_TYPE_ID
                     "Neurologi", false, null);
               examinationGroups[po.type] = po;
            }
            int right = rs.getInt(1);
            int left = rs.getInt(2);
            switch (right)
            {
               case 1:
                  po.addExamination("Förlamad höger arm");
                  break;
               case 2:
                  po.addExamination("Förlamad höger ben");
                  break;
               case 3:
                  po.addExamination("Förlamad höger sida");
                  break;
            }
            switch (left)
            {
               case 1:
                  po.addExamination("Förlamad vänster arm");
                  break;
               case 2:
                  po.addExamination("Förlamad vänster ben");
                  break;
               case 3:
                  po.addExamination("Förlamad vänster sida");
                  break;
            }
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read paralysis"));
      }

      // Actions
      try {
         rs = dbcon.dbQuery("SELECT AA.ATTRIBUTE_NAME FROM " +
               "EPR.ACTION_ATTRIBUTES AS AA, " +
               "EPR.ACTION_PERFORMED AS AP WHERE AP.RECORD_ID = " +
               recordId + " AND AP.ACTION_ATTRIBUTE_ID = AA.ATTRIBUTE_ID " +
               "ORDER BY AA.ACTION_TYPE_ID");
         StringBuffer strBuf = new StringBuffer(200);
         while (rs.next())
         {
            strBuf.append(rs.getString(1));
            strBuf.append(", ");
         }
         actionVec = parseString(strBuf.toString(), width-10, fm10);
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read actions"));
      }

      // Action description
      try {
         rs = dbcon.dbQuery("SELECT DESCRIPTION FROM EPR.ACTION_DESCRIPTION " +
               "WHERE RECORD_ID = " + recordId);
         if (rs.next())
            actionDescVec = parseString(rs.getString(1), width-10,
                  fm10);
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read action description"));
      }

      // Medicines
      medicinesVec = new Vector();
      try {
         rs = dbcon.dbQuery("SELECT S.USER_NAME, M.MEDICINE_NAME, " +
               "M.MEDICINE_CONCENTRATION, M.MEDICINE_CONC_UNIT, GM.DOZAGE, " +
               "GM.GIVEN_TIME, GM.GIVEN_MEDICATION_ID, GM.GIVEN_BY " +
               "FROM EPR.GIVEN_MEDICATIONS AS GM, EPR.MEDICINE AS M, " +
               "EPR.STAFF AS S " +
               "WHERE GM.RECORD_ID = " + recordId +
               " AND GM.MEDICINE_ID = M.MEDICINE_ID" +
               " AND GM.GIVEN_BY = S.STAFF_ID");
         while (rs.next())
         {
            if (rs.getInt(8) == 0) // Given by annan
            {
               ResultSet mgb = dbcon.dbQuery (
                     "SELECT NAME FROM EPR.MEDICATION_GIVEN_BY " +
                     "WHERE GIVEN_MEDICATION_ID = " + rs.getInt(7));
               if (mgb.next ())
                  medicinesVec.add(new Print_Medicine(
                     rs.getInt(7),  // GIVEN_MEDICATION_ID
                     rs.getString(2),  // MEDICINE_NAME
                     df.format(rs.getDouble(3) *   // MEDICINE_CONCENTRATION
                        rs.getInt(5)) + " " +   // DOZAGE
                     rs.getString(4),  // MEDICINE_CONC_UNIT
                     mgb.getString(1),  // NAME
                     rs.getString(6)));   // GIVEN_TIME
               else
                  medicinesVec.add(new Print_Medicine(
                     rs.getInt(7),  // GIVEN_MEDICATION_ID
                     rs.getString(2),  // MEDICINE_NAME
                     df.format(rs.getDouble(3) *   // MEDICINE_CONCENTRATION
                        rs.getInt(5)) + " " +   // DOZAGE
                     rs.getString(4),  // MEDICINE_CONC_UNIT
                     rs.getString(1),  // USER_NAME
                     rs.getString(6)));   // GIVEN_TIME
               mgb.close ();
            }
            else  // Given by user in system
            {
               medicinesVec.add(new Print_Medicine(
                     rs.getInt(7),  // GIVEN_MEDICATION_ID
                     rs.getString(2),  // MEDICINE_NAME
                     df.format(rs.getDouble(3) *   // MEDICINE_CONCENTRATION
                        rs.getInt(5)) + " " +   // DOZAGE
                     rs.getString(4),  // MEDICINE_CONC_UNIT
                     rs.getString(1),  // USER_NAME
                     rs.getString(6)));   // GIVEN_TIME
            }
         }
         rs.close();
      } catch (SQLException e) {
          lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read medicines"));
      }
      
      // Medicine comments
      Print_Medicine pm;
      for (int i=0 ; i < medicinesVec.size() ; i++)
      {
         pm = (Print_Medicine) medicinesVec.elementAt(i);
         try {
            rs = dbcon.dbQuery("SELECT EFFECT, EFFECT_TIME FROM " +
                  "EPR.MEDICINE_EFFECTS WHERE GIVEN_MEDICINE_ID = " +
                  pm.medId);
            while (rs.next())
               pm.addComment(rs.getString(2) + " " +  // EFFECT_TIME
                     rs.getString(1));    // EFFECT
            rs.close();
         } catch (SQLException e) {
            lg.addLog(new Log(e.getMessage(),
                     "PrintPanel/PrintPanel",
                     "Read medicine effects"));
         }
         // Delegating doctor in first db call 
      }

      // - Patient parameters -
      paramTModel = new ParameterTableModel(this, dbcon, false);

      StdObservationTableRow pulsRow = new StdObservationTableRow(
            recordId, 1, dbcon, lg, "Puls", 0, 200, paramTModel.getTimes(),
            paramTModel);
      if (pulsRow.hasValues())
         paramTModel.insertRow(pulsRow, 1);
      BpObservationTableRow bpRow = new BpObservationTableRow(
            recordId, dbcon, lg, paramTModel.getTimes(), paramTModel);
      if (bpRow.hasValues())
         paramTModel.insertRow(bpRow, 2);
      StdObservationTableRow breathRow = new StdObservationTableRow(
            recordId, 2, dbcon, lg, "Andning", 0, 200, paramTModel.getTimes(),
            paramTModel);
      if(breathRow.hasValues())
         paramTModel.insertRow(breathRow, 3);
      StdObservationTableRow oxyRow = new StdObservationTableRow(
            recordId, 3, dbcon, lg, "Syre (l/min)", 0, 10,
            paramTModel.getTimes(), paramTModel);
      if (oxyRow.hasValues())
         paramTModel.insertRow(oxyRow, 4);
      StdObservationTableRow satRow = new StdObservationTableRow(
            recordId, 4, dbcon, lg, "Saturation", 0, 100,
            paramTModel.getTimes(), paramTModel);
      if (satRow.hasValues())   
         paramTModel.insertRow(satRow, 5);
      FloatObservationTableRow glukosRow = new FloatObservationTableRow(
            recordId, 5, dbcon, lg, "B-Glukos", 0, 30, paramTModel.getTimes(),
            paramTModel);
      if (glukosRow.hasValues())
         paramTModel.insertRow(glukosRow, 6);
      StdObservationTableRow vasRow = new StdObservationTableRow(
            recordId, 6, dbcon, lg, "VAS", 0, 10, paramTModel.getTimes(),
            paramTModel);
      if (vasRow.hasValues())
         paramTModel.insertRow(vasRow, 7);
      RLSObservationTableRow rlsRow = new RLSObservationTableRow(
            recordId, 7, dbcon, lg, paramTModel.getTimes());
      if (rlsRow.hasValues())
         paramTModel.insertRow(rlsRow, 8);
      GCSModel gcsModel = new GCSModel(
            recordId, dbcon, lg, paramTModel.getTimes());
      if (gcsModel.hasValues())
      {
         paramTModel.insertRow(gcsModel.getRowModel(0), 9);
         paramTModel.insertRow(gcsModel.getRowModel(1), 10);
         paramTModel.insertRow(gcsModel.getRowModel(2), 11);
      }
      EyeModel eyeModel = new EyeModel(
            recordId, dbcon, lg, paramTModel.getTimes());
      if (eyeModel.hasValues())
      {
         paramTModel.insertRow(eyeModel.getRowModel(0), 12);
         paramTModel.insertRow(eyeModel.getRowModel(1), 13);
      }
      BodyObservationTableRow bodyposRow = new BodyObservationTableRow(
            recordId, dbcon, lg, paramTModel.getTimes());
      if (bodyposRow.hasValues())
         paramTModel.insertRow(bodyposRow, 14);

      // - Body image -

      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.FRACTURE WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            bodyTextVec = new Vector();
            for (int i=1 ; i <= 19 ; i++)
            {
               if (rs.getByte(i+1) == 116)   // 't'
                  bodyTextVec.add(new Text(FRACTURE_X_POS[i],
                           FRACTURE_Y_POS[i], "F"));
            }
            byte res;
            // Perf puls
            for (int i=0 ; i < 4 ; i++)
            {
               res = rs.getByte(i+21);
               if (res == 116)   // 't'
                  perfPuls[i] = 1;
               else if (res == 102) // 'f'
                  perfPuls[i] = 2;
            }
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read fracture"));
      }

      try {
         rs = dbcon.dbQuery("SELECT * FROM EPR.LUXATION WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            if (bodyTextVec == null)
               bodyTextVec = new Vector();
            for (int i=1 ; i <= 10 ; i++)
            {
               if (rs.getByte(i+1) == 116)   // 't'
                  bodyTextVec.add(new Text(LUXATION_X_POS[i],
                           LUXATION_Y_POS[i], "L"));
            }
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read luxation"));
      }

      // Misc Text
      try {
         rs = dbcon.dbQuery("SELECT MISCTEXT FROM EPR.MISC WHERE RECORD_ID = " +
               recordId);
         if (rs.next())
         {
            miscTextVec = parseString(rs.getString(1), width-10, fm10);
         }
         rs.close();
      } catch (SQLException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Read misc"));
      }

      // --- Calculate page numbers ---
      numOfPages = 1;

      int tmpPos;
      int yPos = 32;                // Header
      yPos += 7 * EXT_ROW_HEIGHT + 4;   // Patient info

      if (alarmCouseStr != null && alarmCouseStr.length() > 0)
         yPos += EXT_ROW_HEIGHT;
      
      if (medVec != null)           // Medication
      {
         yPos += EXT_ROW_HEIGHT;
         yPos += medVec.size() * STD_ROW_HEIGHT;
      }

      if (allergyVec != null)       // Allergy
      {
         yPos += EXT_ROW_HEIGHT;
         yPos += allergyVec.size() * STD_ROW_HEIGHT;
      }

      if (anamnesisVec != null)     // Anamnesis
      {
         yPos += EXT_ROW_HEIGHT;
         yPos += anamnesisVec.size() * STD_ROW_HEIGHT;
      }

      if (diagnosisVec != null)     // Diagnosis
      {
         yPos += EXT_ROW_HEIGHT;
         yPos += diagnosisVec.size() * STD_ROW_HEIGHT;
      }

      if (foodVec != null)          // Food
      {
         yPos += EXT_ROW_HEIGHT;
         yPos += foodVec.size() * STD_ROW_HEIGHT;
      }

      yPos += 2;

      tmpPos = yPos;
      if (examinationExists)        // Examination
      {
         yPos += EXX_ROW_HEIGHT;
         for (int i=0 ; i < examinationGroups.length ; i++)
         {
            if (examinationGroups[i] == null)
               continue;
            yPos += STD_ROW_HEIGHT;
            if (!examinationGroups[i].withoutRemark &&
                  examinationGroups[i].description != null &&
                  examinationGroups[i].description.length() > 0)
               yPos += STD_ROW_HEIGHT;
         }
         yPos += 2;
      }
      if (yPos > height)
      {
         numOfPages++;
         yPos = yPos - tmpPos + 32;
      }
      examinationPage = numOfPages;

      tmpPos = yPos;
      if (actionVec != null || actionDescVec != null) // Actions
      {
         yPos += EXX_ROW_HEIGHT;
         if (actionVec != null)
            yPos += actionVec.size() * STD_ROW_HEIGHT;
         if (actionDescVec != null)
            yPos += actionDescVec.size() * STD_ROW_HEIGHT;
         yPos += 2;
      }
      if (yPos > height)
      {
         numOfPages++;
         yPos = yPos - tmpPos + 32;
      }
      actionPage = numOfPages;

      tmpPos = yPos;
      if (medicinesVec.size() > 0)     // Medication
      {
         yPos += EXX_ROW_HEIGHT;
         if (doctorStr != null && doctorStr.length() > 0)
            yPos += STD_ROW_HEIGHT;
         for (int i=0 ; i < medicinesVec.size() ; i++)
         {
            pm = (Print_Medicine) medicinesVec.elementAt(i);
            yPos += STD_ROW_HEIGHT;
            if (pm.comments != null)
               yPos += pm.comments.size() * STD_ROW_HEIGHT;
         }
         yPos += 2;
      }
      if (yPos > height)
      {
         numOfPages++;
         yPos = yPos - tmpPos + 32;
      }
      medicinePage = numOfPages;

      tmpPos = yPos;
      if (paramTModel.getRowCount() > 1)  // Patient parameters
      {
         yPos += EXX_ROW_HEIGHT;
         yPos += paramTModel.getRowCount() * STD_ROW_HEIGHT;
         yPos += 2;
      }
      if (yPos > height)
      {
         numOfPages++;
         yPos = yPos - tmpPos + 32;
      }
      parameterPage = numOfPages;

      tmpPos = yPos;
      if (paramTModel.getColumnCount() > 7)  // Second patient parameters table
      {
         yPos += paramTModel.getRowCount() * STD_ROW_HEIGHT;
         yPos += 4;
      }
      if (yPos > height)
      {
         numOfPages++;
         yPos = yPos - tmpPos + 32;
      }
      parameterPage2 = numOfPages;

      // Bodyimage
      if (bodyTextVec != null)
      {
         yPos += 272;
         yPos += EXX_ROW_HEIGHT;
         if (yPos > height)
         {
            numOfPages++;
            yPos = 272 + 32;
         }
      }
      bodyPage = numOfPages;

      // Misc
      if (miscTextVec != null)
      {
         tmpPos = yPos;
         yPos += EXX_ROW_HEIGHT;
         yPos += miscTextVec.size() * STD_ROW_HEIGHT;
         yPos += 2;
         if (yPos > height)
         {
            numOfPages++;
            yPos = yPos - tmpPos + 32;
         }
      }
      miscPage = numOfPages;

      // Create a tracker that makes sure all images are loaded.
      MediaTracker mt = new MediaTracker(this);

      mt.addImage(vllImage, 1);
      mt.addImage(bodyImage, 2);
 
      try {
         mt.waitForAll();
      } catch(InterruptedException e) {
         lg.addLog(new Log(e.getMessage(),
                  "PrintPanel/PrintPanel",
                  "Loading images"));
      }
   }

   /**
     * Method that splits a string into parts shorter than the max length.
     * @param text The string to split.
     * @param rowLength The maximum lenght of the string in pixels.
     * @param fm Information about the size of the font.
     * @return A vector containing the parts, null if there is no text.
     */
   private Vector parseString(String text, int rowLength, FontMetrics fm)
   {
      int start = 0, end = 0, rows = 1, tmp = 0, tmp2;

      if (text.length() == 0)
         return null;

      int approxWidth = rowLength / fm.charWidth('i');
      
      Vector result = new Vector();
      
      for (int i = 0 ; end < text.length() ; i++, rows++)
      {
         tmp = start;
 
         do {  // Build a string that is approximatly as long as possible.
            end = tmp;
            tmp = text.indexOf(" ", end + 1);
            tmp2 = text.indexOf("\n", end + 1);
            if (tmp2 != -1 && (tmp2 < tmp || tmp == -1))
            {
               end = tmp2;  
               break;
            }
            if (tmp == -1)
            {
               end = text.length();
               break;
            }
         } while ((tmp - start) < approxWidth);
 
         result.insertElementAt(text.substring(start, end), i);
 
         // Check length of the row and step back if it is too long.
         while (fm.stringWidth((String)result.get(i)) > rowLength)
         {
            int curLength;
            end = text.lastIndexOf(" ", end - 1);
            // If the word is longer than the line split the word
            if (end <= start)
            {
               tmp = rowLength;
               curLength = rowLength * 2;
               do {
                  curLength = (int) (curLength * 
                        ((double)rowLength / (double)tmp));
                  end = start + curLength;
                  try {
                     result.setElementAt(
                        text.substring(start,end) + "-",i);
                  } catch (StringIndexOutOfBoundsException e) {
                     result.setElementAt(
                        text.substring(start, text.length()) + "-", i);
                     curLength = text.length() - start;
                  }
               } while ((tmp = fm.stringWidth((String)result.get(i))) > 
                     rowLength);
              break;
            }
            result.setElementAt(text.substring(start, end), i);
         }
         start = end + 1;
      }
 
      return result;
   }

   /**
     * Method called by the system to paint the panel.
     * @param g The context to paint in.
     */
   public void paint(Graphics g)
   {
      super.paint(g);

      int yStart, yPos = 36;

      g.drawImage(vllImage, 5, 0, 166, 34, this);
 
      g.setFont(bold15);
      g.drawString("AMBULANSJOURNAL", 175, 15);

      // Station name and phone number
      g.setFont(norm10);
      g.drawString(station, 173, 28);

      // Record name
      g.drawString(recordName, 348, 13);
 
      // Page number
      g.drawLine(343, 0, 343, 35);
      g.drawLine(343, 17, 451, 17);
      g.drawString("Sida " + currentPage + " av " + numOfPages, 348, 31);

      // Split line
      g.drawLine(0, 35, 451, 35);

      if (currentPage == 1)
      {
         // Staff
         yStart = yPos;
         yPos += EXT_ROW_HEIGHT;
         g.drawString(carerStr.toString(), 2, yPos-2);
         if (signtime == null)
         {
            g.drawString("Sign: OSIGNERAD", 322/*357*/, yPos-2);
            //g.drawLine(355, yStart, 355, yPos);
         }
         else
         {
            g.drawString("Sign: " + dateFormat.format(signtime) + " " +
                  timeFormat.format(signtime), 322, yPos-2);
            //g.drawLine(320, yStart, 320, yPos);
            //g.drawLine(320, yPos, 355, yPos);
         }
         //g.drawLine(1, yPos, width, yPos);

         //yStart = yPos;
         yPos += EXT_ROW_HEIGHT;
         g.drawString(driverStr.toString(), 2, yPos-2);
         g.drawString("Larm: " + alarmTime, 322/*357*/, yPos-2);
         g.drawLine(1, yPos, 320/*355*/, yPos);
          
         // Patient information
         //yPos += 2;
         int yStart2 = yPos;
         g.drawLine(1, yPos, 320/*353*/, yPos);
         yPos += EXT_ROW_HEIGHT;
         g.drawString("Personnummer: " + pNumber, 2, yPos-2);
         g.drawString("Hämtning: " + pickupTime, 322/*357*/, yPos-2);
         //g.drawLine(1, yPos, 353, yPos);
         //g.drawLine(355, yPos-2, width, yPos-2);
 
         yPos += EXT_ROW_HEIGHT;
         g.drawString("Namn: " + name, 2, yPos-2);
         g.drawString("Ankomst: " + dropofTime, 322/*357*/, yPos-2);
         g.drawLine(320/*355*/, yPos, width, yPos);
         //g.drawLine(355, yPos-2, width, yPos-2);
         g.drawLine(320/*355*/, yStart, 320/*355*/, yPos);
         //g.drawLine(355, yStart, 355, yPos-2);
 
         yPos += EXT_ROW_HEIGHT;
         g.drawString("Adress: " + address, 2, yPos-2);
         //g.drawLine(1, yPos, width, yPos);
 
         yPos += EXT_ROW_HEIGHT;
         g.drawString("Anhörig: " + relative, 2, yPos-2);
         g.drawLine(1, yPos, width, yPos);

         // Alarm cause
         if (alarmCouseStr != null && alarmCouseStr.length() > 0)
         {
            yPos += EXT_ROW_HEIGHT;
            g.setFont(bold10);
            g.drawString("Larmorsak", 3, yPos);
            g.setFont(norm10);
            g.drawString(alarmCouseStr, 65, yPos);
         }

         // Diagnosis
         yPos += EXT_ROW_HEIGHT;
         g.setFont(bold10);
         g.drawString("Diagnos ", 3, yPos);
         g.setFont(norm10);
         g.drawString(diagnosisStr.toString(), 65, yPos);

         yPos += 2;
         g.drawLine(1, yPos, width, yPos);

         // Medication
         if (medVec != null)
         {
            yPos += EXT_ROW_HEIGHT;
            g.setFont(bold10);
            g.drawString("Medicin", 3, yPos);
            g.setFont(norm10);
            for (int i=0 ; i < medVec.size() ; i++)
            {
               yPos += STD_ROW_HEIGHT;
               g.drawString((String)medVec.get(i), 5, yPos);
            }
         }

         // Allergy
         if (allergyVec != null)
         {
            yPos += EXT_ROW_HEIGHT;
            g.setFont(bold10);
            g.drawString("Allergi", 3, yPos);
            g.setFont(norm10);
            for (int i=0 ; i < allergyVec.size() ; i++)
            {
               yPos += STD_ROW_HEIGHT;
               g.drawString((String)allergyVec.get(i), 5, yPos);
            }
         }

         // Anamnesis
         if (anamnesisVec != null)
         {
            yPos += EXT_ROW_HEIGHT;
            g.setFont(bold10);
            g.drawString("Tidigare sjukdomar", 3, yPos);
            g.setFont(norm10);
            for (int i=0 ; i < anamnesisVec.size() ; i++)
            {
               yPos += STD_ROW_HEIGHT;
               g.drawString((String)anamnesisVec.get(i), 5, yPos);
            }
         }

         // Diagnosis
         if (diagnosisVec != null)
         {
            yPos += EXT_ROW_HEIGHT;
            g.setFont(bold10);
            g.drawString("Aktuell skada", 3, yPos);
            g.setFont(norm10);
            for (int i=0 ; i < diagnosisVec.size() ; i++)
            {
               yPos += STD_ROW_HEIGHT;
               g.drawString((String)diagnosisVec.get(i), 5, yPos);
            }
         }

         // Food
         if (foodVec != null)
         {
            yPos += EXT_ROW_HEIGHT;
            g.setFont(bold10);
            g.drawString("Senaste måltid", 3, yPos);
            g.setFont(norm10);
            for (int i=0 ; i < foodVec.size() ; i++)
            {
               yPos += STD_ROW_HEIGHT;
               g.drawString((String)foodVec.get(i), 5, yPos);
            }
         }
         yPos += 2;
         g.drawLine(1, yPos, width, yPos);
      }

      // Examination
      if (currentPage == examinationPage) 
      {
         if (examinationExists)
         {
            int wdt;
            String exattr;

            yPos += EXX_ROW_HEIGHT;
            g.setFont(bold12);
            g.drawString("Observationer", 3, yPos - 1);
            for (int i=0 ; i < examinationGroups.length ; i++)
            {
               if (examinationGroups[i] == null)
                  continue;
               yPos += STD_ROW_HEIGHT;
               g.setFont(bold10);
               g.drawString(examinationGroups[i].typeName, 3, yPos-1);
               wdt = EXAMINATION_HEADER_WIDTH[i];
               g.setFont(norm10);
               if (examinationGroups[i].withoutRemark)
                  g.drawString("UA", wdt + 10, yPos-1);
               else
               {
                  exattr = "";
                  if (examinationGroups[i].examinations != null)
                  {
                     for(int j=0;j<examinationGroups[i].examinations.size();j++)
                     {
                        exattr = exattr + " ," +
                           examinationGroups[i].examinations.elementAt(j);
                     }
                     g.drawString(exattr, wdt, yPos-1);
                  }
                  if (examinationGroups[i].description != null && 
                        examinationGroups[i].description.length() > 0)
                  {
                     yPos += STD_ROW_HEIGHT;
                     g.drawString(examinationGroups[i].description, 5, yPos-1);
                  }
               }
            }
            yPos += 2;
            g.drawLine(1,yPos,width,yPos);
         }
      }

      if (currentPage == actionPage)
      {
         // Actions, only print if there's a desc or action performed.
         if (actionVec != null || actionDescVec != null)
         {

            yPos += EXX_ROW_HEIGHT;
            g.setFont(bold12);
            g.drawString("Åtgärder", 3, yPos - 1);
            g.setFont(norm10);
            if (actionVec != null)
            {
               for (int i=0 ; i < actionVec.size() ; i++)
               {
                  yPos += STD_ROW_HEIGHT;
                  g.drawString((String)actionVec.get(i), 5, yPos-1);
               }
            }
            if (actionDescVec != null)
            {
               for (int i=0 ; i < actionDescVec.size() ; i++)
               {
                  yPos += STD_ROW_HEIGHT;
                  g.drawString((String)actionDescVec.get(i), 5, yPos-1);
               }
            }
            yPos += 2;
            g.drawLine(1,yPos,width,yPos);
        }
      }

      // Medicine
      if (currentPage == medicinePage)
      {
         if (medicinesVec.size() > 0)
         {
            yPos += EXX_ROW_HEIGHT;
            g.setFont(bold12);
            g.drawString("Läkemedel", 3, yPos - 1);
            g.setFont(norm10);
            if (doctorStr != null && doctorStr.length() > 0)
            {
               yPos += STD_ROW_HEIGHT;
               g.drawString("Delegerande läkare: " + doctorStr, 3, yPos - 1);
            }
            Print_Medicine pm;
            for (int i=0 ; i < medicinesVec.size() ; i++)
            {
               pm = (Print_Medicine) medicinesVec.elementAt(i);
               yPos += STD_ROW_HEIGHT;
               g.drawString(pm.medName + " " + pm.medAmount + " " + pm.medTime +
                     " " + pm.giverName, 3, yPos-1);
               if (pm.comments != null)
               {
                  for (int j=0 ; j < pm.comments.size() ; j++)
                  {
                     yPos += STD_ROW_HEIGHT;
                     g.drawString(pm.comments.elementAt(j).toString(),7,yPos-1);
                  }
               }
            }
            yPos += 2;
            g.drawLine(1,yPos,width,yPos);
         }
      }

      // Patient Parameters
      if (currentPage == parameterPage && paramTModel.getRowCount() > 1)
      {
         int columns = paramTModel.getColumnCount();
         columns = (columns > 7 ? 7 : columns);
         int xPos;
         int rowWidth = columns * ROW_WIDTH + ROW_HEAD_WIDHT;
         String str;
         yPos += EXX_ROW_HEIGHT;
         g.setFont(bold12);
         g.drawString("Patient Parametrar", 3, yPos - 1);
         g.setFont(norm10);
         yStart = yPos;
         g.drawLine(2, yPos, rowWidth, yPos);
         for (int i=0 ; i < paramTModel.getRowCount() ; i++)
         {
            yPos += STD_ROW_HEIGHT;
            g.drawLine(2, yPos, rowWidth, yPos);
            g.drawString(paramTModel.getRowName(i), 5, yPos-1);
            xPos = ROW_HEAD_WIDHT;
            for (int j=0 ; j < columns ; j++)
            {
               if ((str = (String)paramTModel.getValueAt(i, j)) != null)
                  g.drawString(str, xPos+2, yPos-1);
               xPos += ROW_WIDTH;
            }
         }
         g.drawLine(2, yStart, 2, yPos);
         xPos = ROW_HEAD_WIDHT;
         for (int i=0 ; i <= columns ; i++)
         {
            g.drawLine(xPos, yStart, xPos, yPos);
            xPos += ROW_WIDTH;
         }
         yPos += 2;
         if (paramTModel.getColumnCount() <= 7)
            g.drawLine(1, yPos, width, yPos);
      }
      // Patient parameters 2
      if (currentPage == parameterPage2 && paramTModel.getColumnCount() > 7)
      {
         int columns = paramTModel.getColumnCount() - 7;
         int xPos;
         int rowWidth = columns * ROW_WIDTH + ROW_HEAD_WIDHT;
         String str;
         g.setFont(norm10);
         yStart = yPos;
         g.drawLine(2, yPos, rowWidth, yPos);
         for (int i=0 ; i < paramTModel.getRowCount() ; i++)
         {
            yPos += STD_ROW_HEIGHT;
            g.drawLine(2, yPos, rowWidth, yPos);
            g.drawString(paramTModel.getRowName(i), 5, yPos-1);
            xPos = ROW_HEAD_WIDHT;
            for (int j=0 ; j < columns ; j++)
            {
               if ((str = (String)paramTModel.getValueAt(i, j+7)) != null)
                  g.drawString(str, xPos+2, yPos-1);
               xPos += ROW_WIDTH;
            }
         }
         g.drawLine(2, yStart, 2, yPos);
         xPos = ROW_HEAD_WIDHT;
         for (int i=0 ; i <= columns ; i++)
         {
            g.drawLine(xPos, yStart, xPos, yPos);
            xPos += ROW_WIDTH;
         }
         yPos += 2;
         g.drawLine(1, yPos, width, yPos);
      }

      // Body image
      if (currentPage == bodyPage && bodyTextVec != null)
      {
         yPos += EXX_ROW_HEIGHT;
         g.setFont(norm10);
         g.drawImage(bodyImage, 5, yPos, 213, 272, this);
         Text t;
         for (int i=0 ; i < bodyTextVec.size() ; i++)
         {
            t = (Text) bodyTextVec.elementAt(i);
            g.drawString(t.text, t.x + 5, t.y + yPos);
         }
         g.setFont(bold12);
         g.drawString("Skade lokalisering", 3, yPos - 1);
         g.setFont(norm10);
         g.drawString("F = Fraktur", 220, yPos);
         g.drawString("L = Luxation", 220, yPos + EXT_ROW_HEIGHT);

         if (perfPuls[0] == 1)   // right arm, yes
            g.drawString("Perferiell puls i höger arm", 220,
                 yPos + (4 * EXT_ROW_HEIGHT));
         else if (perfPuls[0] == 2) // right arm, no
            g.drawString("IGEN perferiell puls i höger arm", 220,
                  yPos + (4 * EXT_ROW_HEIGHT));
         if (perfPuls[1] == 1)   // left arm, yes
            g.drawString("Perferiell puls i vänster arm", 220,
                  yPos + (5 * EXT_ROW_HEIGHT));
         else if (perfPuls[1] == 2) // left arm, no
            g.drawString("IGEN perferiell puls i vänster arm", 220,
                  yPos + (5 * EXT_ROW_HEIGHT));
         if (perfPuls[2] == 1)   // right leg, yes
            g.drawString("Perferiell puls i höger ben", 220,
                  yPos + (6 * EXT_ROW_HEIGHT));
         else if (perfPuls[2] == 2) // right leg, no
            g.drawString("IGEN perferiell puls i höger ben", 220,
                  yPos + (6 * EXT_ROW_HEIGHT));
         if (perfPuls[3] == 1)   // left leg, yes
            g.drawString("Perferiell puls i vänster ben", 220,
                  yPos + (7 * EXT_ROW_HEIGHT));
         else if (perfPuls[3] == 2) // left leg, no
            g.drawString("IGEN perferiell puls i vänster ben", 220,
                 yPos + (7 * EXT_ROW_HEIGHT));
         yPos += 272;
         g.drawLine(1, yPos, width, yPos);
      }

      // Misc text
      if (currentPage == miscPage && miscTextVec != null)
      {
         yPos += EXX_ROW_HEIGHT;
         g.setFont(bold12);
         g.drawString("Övrigt", 3, yPos - 1);
         g.setFont(norm10);
         for (int i=0 ; i < miscTextVec.size() ; i++)
         {
            yPos += STD_ROW_HEIGHT;
            g.drawString((String)miscTextVec.elementAt(i), 5, yPos); 
         }
         yPos += 2;
         g.drawLine(1, yPos, width, yPos);
      }
 
      // Main rectangle
      g.drawRect(0, 0, width, height);
   }

   /**
    * Method called to print the panel.
    * @param g The context to print on.
    * @param page The formating of the page.
    * @param pageIndex The page of the print job to print.
    */
   public int print(Graphics g, PageFormat pageFormat, int pageIndex)
   {
      int x = (int)pageFormat.getImageableX();
      int y = (int)pageFormat.getImageableY();
      g.translate(x, y);

      width = (int) pageFormat.getImageableWidth();
      height =(int) pageFormat.getImageableHeight();

      if (pageIndex < numOfPages)
      {
         currentPage = pageIndex + 1;
         disableDoubleBuffering(this);
         paint(g);
         enableDoubleBuffering(this);
         return Printable.PAGE_EXISTS;
      }
      else
      {
         return Printable.NO_SUCH_PAGE;
      }
   }

   /**
    * The speed and quality of printing suffers dramatically if
    *  any of the containers have double buffering turned on.
    *  So this turns if off globally.
    *  @see enableDoubleBuffering
    */
   public static void disableDoubleBuffering(Component c) {
      RepaintManager currentManager = RepaintManager.currentManager(c);
      currentManager.setDoubleBufferingEnabled(false);
   }

   /**
    * Re-enables double buffering globally.
    */
   public static void enableDoubleBuffering(Component c) {
      RepaintManager currentManager = RepaintManager.currentManager(c);
      currentManager.setDoubleBufferingEnabled(true);
   }

   /**
     * Method that sets the current painted page.
     * @param page The page number to paint.
     */
   public void setCurrentPage(int page)
   {
      if (page > 0 && page <= numOfPages)
      {
         currentPage = page;
         repaint();
      }
   }

   /**
     * Method that returns the number of pages in the document.
     * @return The number of pages.
     */
   public int getNumOfPages()
   {
      return numOfPages;
   }

   /**
     * Interface method that displays a message. Member method of the
     * MessageInterface.
     * @param msg The message.
     */
   public void setMessage(String msg)
   {
      ;
   }
}
