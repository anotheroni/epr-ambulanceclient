import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.*;
import java.sql.*;
import java.util.*;
import java.text.*;

/**
 * Class that formats the patient parameter table in the patient parameters
 * pane.
 *
 * @version 20030725
 * @author Oskar Nilsson
 */
public class ParameterTableModel extends AbstractTableModel implements 
FocusEventReceiver
{
   private MessageInterface pr;
   private DB2Connect dbcon;
   private boolean isEditable;
   private RowHeaderTableModel rowHeaderTModel;

   private Vector rows;
   private Vector hiddenRows;
   private Vector times = null;

   private JTextField timeEditorFld;
   private int lastCell = 0;
   private int nextActiveCell = 0;
   private boolean setValueAtFailed;
   private boolean newValueSaved;   /// Used to avoid changing a value twice
   
   // Used by getColumnClass
   private Class obj = (new Object()).getClass();
   private Class dat = (new java.util.Date()).getClass();

   private SimpleDateFormat formatter;
   
   /**
     * Creates a table model object, and initializes it
     * @param pr Reference to the Patient Record.
     * @param dbcon A reference to the database.
     * @param isEditable True if the table should be editable.
     */
   public ParameterTableModel(MessageInterface pr, DB2Connect dbcon,
         boolean isEditable)
   {
      this.pr = pr;
      this.dbcon = dbcon;
      this.isEditable = isEditable;

      rows = new Vector();
      hiddenRows = new Vector();
      times = new Vector();

      formatter = new SimpleDateFormat("HH:mm");
    
      timeEditorFld = new JTextField();
      ((AbstractDocument)timeEditorFld.getDocument()).
         setDocumentFilter(new LimitedTimeFilter(5, timeEditorFld));
      ComponentFocusListener f_listener =
         new ComponentFocusListener (this, timeEditorFld);
      timeEditorFld.addFocusListener (f_listener);
 
      rowHeaderTModel = new RowHeaderTableModel(this);

      rows.add(times);
   }

   /**
     * Method used to get the row header table model for the table.
     * @return The row header table model.
     */
   public RowHeaderTableModel getRowHeader()
   {
      return rowHeaderTModel;
   }

   /**
     * Method that returns the time values for all columns in the table.
     * @return The time values of all columns.
     */
   public Vector getTimes()
   {
      return times;
   }
  
    /**
      * Method that adds a new column (observation time) to the table.
      */
    public void addColumn()
    {
       int col;
       java.util.Date d = new java.util.Date();
       Time newTime = new Time(d.getHours(), d.getMinutes(), d.getSeconds());

       // while the new date is earlier than the date before in the vector
       for (col = times.size() ; col > 0 &&
             newTime.before((java.util.Date)times.elementAt(col-1)) ; col--)
          ;

       times.add(col, newTime);
 
       // Add the column to all rows
       for (int i=1 ; i < rows.size() ; i++) // row 0 is time
             ((ObservationTableRowInterface)rows.elementAt(i)).
             addColumn(null, col);
       for (int i=0 ; i < hiddenRows.size() ; i++)
          ((ObservationTableRowInterface)hiddenRows.elementAt(i)).
             addColumn(null, col);

       fireTableStructureChanged();
    }

    /**
      * Method that adds a row (new observation type) to the table.
      * @param rowModel The row model
      * @param row Requested row number, if the table is smaller the
      * new row is added last.
      * @return The row number that the row is inserted into.
      */
    public int insertRow(ObservationTableRowInterface rowModel, int row)
    {
       // If the row already is in the table do nothing
       if (rows.indexOf(rowModel) != -1)
          return rows.indexOf(rowModel);
          
       // Try to remove the model from the hidden rows, if it isn't there
       // it's a new row else move the old row to the active row vector.
       if (!hiddenRows.remove(rowModel))
       {
          // A new row
          Vector newTimes = rowModel.getNewTimes();
          // There exist new columns in the row
          if (newTimes.size() != 0)
          {
             int i;
             Iterator it = newTimes.iterator();
             java.util.Date newTime;
             if (it.hasNext())
                newTime = (java.util.Date)it.next();
             else
                newTime = null;

             // Merge the time vectors
             for (i=0 ; newTime != null && i < times.size() ; )
             {
                // true if times is after newTime, add a new column
                if (newTime.compareTo(times.elementAt(i)) < 0)
                {
                   times.add(i, newTime);
                   // Add a new column to all rows
                   for (int j=1 ; j < rows.size() ; j++)
                      ((ObservationTableRowInterface)rows.
                       elementAt(j)).addColumn(null, i);
                   for (int j=0 ; j < hiddenRows.size() ; j++)
                      ((ObservationTableRowInterface)hiddenRows.
                       elementAt(j)).addColumn(null, i);
                   if (it.hasNext())
                      newTime = (java.util.Date)it.next();
                   else
                      newTime = null;
                }
                // Same time, the "new" column already exists
                else if (newTime.compareTo(times.elementAt(i)) == 0)
                {
                   if (it.hasNext())
                      newTime = (java.util.Date)it.next();
                   else
                      newTime = null;
                   i++;
                }
                // newTime is after times, wrong place to insert the new time.
                else
                {
                   i++;
                }
             }
             // Add new times that are left from the first loop
             while (newTime != null)
             {
                times.add(i, newTime);
                // Add a new column to all rows
                for (int j=1 ; j < rows.size() ; j++)
                   ((ObservationTableRowInterface)rows.
                    elementAt(j)).addColumn(null, i);
                for (int j=0 ; j < hiddenRows.size() ; j++)
                   ((ObservationTableRowInterface)hiddenRows.
                    elementAt(j)).addColumn(null, i);
                if (it.hasNext())
                   newTime = (java.util.Date)it.next();
                else
                   newTime = null;
                i++;
             }
          }
       }
       
       try {
          rows.add(row, rowModel);
       } catch (ArrayIndexOutOfBoundsException e) {
         // If add failed at position row insert rowModel last
         rows.add(rowModel);
         row = rows.size() - 1;
       }
       fireTableStructureChanged();
       rowHeaderTModel.updateTable();
       return row;
    }

    /**
      * Method that removes the row from the table.
      * @param rowModel The row to remove.
      * @return The row number that was removed.
      */
    public int removeRow(ObservationTableRowInterface rowModel)
    {
       int row;
       String res;
       if ((res = rowModel.saveRow(times)) != null)
          pr.setMessage(res);
       row = rows.indexOf(rowModel);
       if (row != -1)
       {
          rows.remove(row);
          hiddenRows.add(rowModel);
          fireTableRowsDeleted(row, row);
       }
       rowHeaderTModel.updateTable();
       return row;
    }

    /**
     * Method to get the number of columns in the table.
     */
    public int getColumnCount()
    {
        return times.size();
    }

    /**
     * Method to get the number of rows in the table.
     * @return The number of rows in the table.
     */ 
    public int getRowCount()
    {
       return rows.size();
    }

    /**
     * Method to get a column name.
     * @param col The column number to get the name for.
     * @return The column name.
     */
    public String getColumnName(int col)
    {
       // There is no table column header
       return null;
    }

    /**
      * Method to get the row name.
      * @param row The row number to get the name for.
      * @return The row name.
      */
    public String getRowName(int row)
    {
       if (row == 0)
          return "Tid";
       else
       {
          try {
             return ((ObservationTableRowInterface)rows.elementAt(row)).
                getRowName();
          } catch (ArrayIndexOutOfBoundsException e) { return null; }
       }
    }

    /**
     * Method to get the value in a cell.
     * @param row The row number.
     * @param col The column number.
     * @return The object in the specified cell.
     */
    public Object getValueAt(int row, int col)
    {
       if (row == 0)
       {
          return formatter.format(times.elementAt(col));
       }
       else
          return ((ObservationTableRowInterface)rows.elementAt(row)).
             getValue(col);
    }

    /**
     * JTable uses this method to determine the default renderer/
     * editor for each cell. If we didn't implement this method,
     * the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int col)
    {
       // Uses JRowTable and RowEditorModel the get a unique row editor.
       return obj;
    }

    /**
     * Method used to se if a cell is editable
     * @param row the row to check
     * @param col the column to check
     */
    public boolean isCellEditable(int row, int col)
    {
        return isEditable;
    }

    /**
     * Method to change data in a cell
     * @param value the new value
     * @param row the row to change
     * @param col the colom to change
     */
    public void setValueAt(Object value, int row, int col)
    {
       newValueSaved = true;
      // Time row
      if (row == 0)
      {
          int end = col;
          int i, j;
          java.util.Date oldTime = (java.util.Date)times.elementAt(col);
          java.util.Date newTime = null;// = (java.util.Date)value;

          try {
             newTime = formatter.parse((String)value);
          } catch (ParseException e) {
             pr.setMessage("Observations tiden måste ha formen TT:MM");
             setValueAtFailed = true;
             return;
          }

          // if the new date is earlier than the date before in the vector
          for (i = (col-1) ; i >= 0 &&
                newTime.compareTo(times.elementAt(i)) < 0 ; i--)
          {
             times.setElementAt(times.elementAt(i), i+1);
          }
          if (i != (col-1))
             end = i+1;
          
          // if the new date is later than the date after in the vector
          for (j = (col+1) ; j < times.size() &&
                newTime.compareTo(times.elementAt(j)) > 0 ; j++)
          {
             times.setElementAt(times.elementAt(j), j-1);
          }
          if (j != (col+1))
             end = j-1;
          
          times.setElementAt(newTime, end);
          
          // Update all rows
          for (i=1 ; i < rows.size() ; i++)
             ((ObservationTableRowInterface)rows.elementAt(i)).
                changeTime(newTime, oldTime, col, end);
          for (i=0 ; i < hiddenRows.size() ; i++)
             ((ObservationTableRowInterface)hiddenRows.elementAt(i)).
                changeTime(newTime, oldTime, col, end);
 
          fireTableDataChanged();
       }
       else
       {
          String msg = null;
          if (value != null) 
            msg = ((ObservationTableRowInterface)rows.elementAt(row)).
               setValue(value.toString(), col);
          else
             msg = ((ObservationTableRowInterface)rows.elementAt(row)).
               setValue(null, col);
          if (msg != null) // Error
          {
             pr.setMessage(msg);
          }
          fireTableCellUpdated(row, col);
       }
    }
    
    /**
      * Method that returns the TableCellEditor for the specified row.
      * @param row Row number of the row to get the editor for.
      * @param col Column number of the row to get the editor for.
      * @return The TableCellEditor for the row, null if none is specified.
     */
    public TableCellEditor getEditor(int row, int col)
    {
       if (row == 0)
       {
          nextActiveCell = col;
          return new DefaultCellEditor(timeEditorFld);
       }
       try {
          return ((ObservationTableRowInterface)rows.elementAt(row)).
             getCellEditor(col);
       } catch (ArrayIndexOutOfBoundsException e) { return null; }
    }

    /**
      * Method that saves the data in the tanle
      * @return null if all is ok, else an error message.
      */
    public String saveTable()
    {
       String res, msg = null;

       for (int i=1 ; i < rows.size() ; i++)
       {
          res = ((ObservationTableRowInterface)rows.elementAt(i)).
             saveRow(times);
          if (res != null)
          {
             pr.setMessage(res);
             msg = res;
          }
       }
       for (int i=0 ; i < hiddenRows.size() ; i++)
       {
          res = ((ObservationTableRowInterface)hiddenRows.elementAt(i)).
             saveRow(times);
          if (res != null)
          {
              pr.setMessage(res);
              msg = res;
          }
       }

       return msg;
    }

    /**
     * Method called by the cell editor focus listener when the editor
     * looses focus. Saves the current time value.
     * @param val The text in the editor.
     * @return null if all is ok, else an error message.
     */
    public String saveCurrentCell (String val)
    {
       if (newValueSaved)  // If the value already is saved just return.
          return null;
       setValueAtFailed = false;
       setValueAt(val, 0, lastCell);
       // Needed to remove the editor text field
       fireTableStructureChanged ();
       if (setValueAtFailed)
          return "error";
       else
          return null;
    }

   /**
     * Method that sets the current active cell.
     */
   public void setCurrentCell ()
   {
      newValueSaved = false;
      lastCell = nextActiveCell;
   }

} //Class
