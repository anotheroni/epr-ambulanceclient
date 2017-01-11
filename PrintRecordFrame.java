import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.event.*;
import javax.print.attribute.*;

/**
  * Class implementing the main print preview frame.
  *
  * @version 20030331
  * @author Oskar Nilsson
  */
public class PrintRecordFrame extends JFrame
{

   private PrintPanel pPanel;

   private JButton nextBt;
   private JButton prevBt;
   
   private int currentPage = 1;
   
   /**
     * Constructor, initializes the local variables and build the GUI.
     * @param dbcon A reference to the database.
     * @param lg The log hander to report errors to.
     * @param recordId The id of the patient record to display.
     */
   public PrintRecordFrame(DB2Connect dbcon, LogHandler lg, int recordId)
   {
      super("Granska journal");
      setSize(600,725);

      PrintRecordFrame_Listener listener = new PrintRecordFrame_Listener(this);

      JButton printBt = new JButton("Skriv ut...");
      printBt.setBounds(5, 5, 130, 25);
      printBt.addActionListener(listener);
      printBt.setActionCommand("print");
      
      prevBt = new JButton("<<");
      prevBt.setBounds(5, 35, 60, 25);
      prevBt.addActionListener(listener);
      prevBt.setActionCommand("prev");
 
      nextBt = new JButton(">>");
      nextBt.setBounds(75, 35, 60, 25);
      nextBt.addActionListener(listener);
      nextBt.setActionCommand("next");
      
      JButton quitBt = new JButton("Avbryt");
      quitBt.setBounds(5, 65, 130, 25);
      quitBt.addActionListener(listener);
      quitBt.setActionCommand("quit");

      JPanel ctrlPanel = new JPanel();
      ctrlPanel.setLayout(null);
      ctrlPanel.setBounds(0, 0, 140, 700);
      ctrlPanel.add(printBt);
      ctrlPanel.add(nextBt);
      ctrlPanel.add(prevBt);
      ctrlPanel.add(quitBt);

      ctrlPanel.setMinimumSize(new Dimension(140, 700));
      ctrlPanel.setPreferredSize(new Dimension(140, 700));
      
      pPanel = new PrintPanel(dbcon, lg, recordId);

      if (pPanel.getNumOfPages() <= 1)
         nextBt.setEnabled(false);
      prevBt.setEnabled(false);

      getContentPane().add(pPanel, BorderLayout.CENTER);
      getContentPane().add(ctrlPanel, BorderLayout.WEST);

      show();
   }

   /**
     * Method that show the next page in the document.
     */
   public void nextPage()
   {
      int numOfPages = pPanel.getNumOfPages();
      
      if (currentPage >= numOfPages)
         return;
      else if (currentPage == numOfPages - 1)
         nextBt.setEnabled(false);
 
      currentPage++;

      if (currentPage > 1)
         prevBt.setEnabled(true);
 
      pPanel.setCurrentPage(currentPage);
   }

   /**
     * Method that show the previous page in the document.
     */
   public void prevPage()
   {
      if (currentPage <= 1)
         return;
      else if (currentPage == 2)
         prevBt.setEnabled(false);

      currentPage--;

      if (currentPage < pPanel.getNumOfPages())
         nextBt.setEnabled(true);

      pPanel.setCurrentPage(currentPage);
   }

   /**
     * Method that close the frame.
     */
   public void quit()
   {
      dispose();
   }

   /**
     * Method that prints the center panel (PrintPanel).
     */
   public void print()
   {
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPrintable(pPanel);
      if (printJob.printDialog())
         try {
            printJob.print();
         } catch(PrinterException pe) {
            System.out.println("Error printing: " + pe);
         }
      
      /*
         DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
         PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
         PrintService printService[] = 
         PrintServiceLookup.lookupPrintServices(flavor, pras);
         PrintService defaultService =
         PrintServiceLookup.lookupDefaultPrintService();

         PrintService service = ServiceUI.printDialog(null, 200, 200,
         printService, defaultService, flavor, pras);

         if (service != null)
         {
         DocPrintJob job = service.createPrintJob();

         DocAttributeSet das = new HashDocAttributeSet();
         Doc doc = new SimpleDoc(pPanel, flavor, das);
         try {
         job.print(doc, pras);
         } catch (PrintException pe) {
         pe.printStackTrace();
         }
         }
       */
   }

}
