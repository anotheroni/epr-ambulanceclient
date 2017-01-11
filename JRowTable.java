import javax.swing.*;
import javax.swing.table.*;

/**
  * Class implementing a table using a RowEditorModel. Each cell can have a
  * unique cell editor.
  *
  * @version 20030218
  * @author Oskar Nilsson
  */
public class JRowTable extends JTable
{

   ParameterTableModel tm;

   /**
     * Standard constructor.
     * @param tm The table model the table should use.
     */
   public JRowTable(ParameterTableModel tm)
   {
      super(tm);
      this.tm = tm;
   }

   /**
     * Method used by the GUI to ask the table which CellEditor to use when
     * editing a cell.
     * @param row The row edited.
     * @param col The column edited.
     * @return The TableCellEditor that the cel (row,col) should use.
     */
   public TableCellEditor getCellEditor(int row, int col)
   {
      TableCellEditor ed = null;
      if (tm != null)
         ed = tm.getEditor(row, col);
      if (ed != null)
         return ed;
      return super.getCellEditor(row, col);
   }
}
