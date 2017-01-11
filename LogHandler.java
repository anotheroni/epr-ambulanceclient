import java.io.*;
import java.util.*;

/**
 * Class for handling and printing log objects to a file
 *
 * @author Kane Neman
 * @version 030221
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */
public class LogHandler implements Runnable {

    private PrintStream ps;
    private Thread thread;
    private Vector queue1;
    private Vector queue2;
    private int whichQueue;
    private int count = 0;

    /**
     * Consturctor
     * @param fileName is the name of the file that logs will be
     * printed to
     */
    public LogHandler(String fileName) throws FileNotFoundException,
    IOException {

        //Create the file
        File file = new File(fileName);

        /* true indicates that new logs should be appended into
           the end of the file*/
        ps = new PrintStream(new FileOutputStream(file, true));

        queue1 = new Vector();
        queue2 = new Vector();

        //Activate queue one to save logs
        whichQueue = 1;

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Method used by other threads or objects to add log jobs
     * to the Loghandler's queue
     * @param log is the object that will be add to the end of the
     * queue
     */  
    public void addLog(Object log) {
        //Critical section
        synchronized (this) {
            //If queue one is activated, add the log to this queue
            if (whichQueue == 1) 
                queue1.add(log);
            //Otherwise add the log to queue two
            else if (whichQueue == 2)
                queue2.add(log);
        }
    }

    /**
     * Method used to printing the log objects to the file
     */ 
    public void run() {
       
        while (true) {
            if (queue1.size() == 0 && queue2.size() == 0) {
                try {
                thread.sleep(5000);
                } catch (InterruptedException e) { }
                continue;
            }

            //Critical section, Inactivate the active queue
            synchronized (this) {
                //If queue on is activated, inactivate it
                if (whichQueue == 1)
                    whichQueue = 2;
                //Otherwise if queue two is activated, inactivate it
                else if (whichQueue == 2)
                    whichQueue = 1;
            }

            /*Write the added log objects to the file from the
              inactivated queue*/
            
            //If queue one is activated now, take the logs in queue two
            if (whichQueue == 1 ) {
                for (int i = 0; i < queue2.size(); i++) {
                    ps.println(queue2.elementAt(i).toString());
                }
                //Empty this queue
                queue2.removeAllElements();
            //If queue two is activated now, take the logs in queue one
            }  else if (whichQueue == 2) {
                for (int i = 0; i < queue1.size(); i++) {
                    ps.println(queue1.elementAt(i).toString());
                }
                //Empty thus queue
                queue1.removeAllElements();
            }
        }//while
    }//Run
}//Class LogHandler
