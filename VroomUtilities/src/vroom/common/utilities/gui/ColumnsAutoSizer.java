package vroom.common.utilities.gui;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * The class <code>ColumnsAutoSizer</code> JAVADOC
 * <p>
 * Retreived from <a
 * href="http://bosmeeuw.wordpress.com/2011/08/07/java-swing-automatically-resize-table-columns-to-their-contents/">this
 * blog</a>
 * </p>
 * <quote>We render the entire table (invisible), and check the maxium required width for each column. If the table is
 * big enough to display all data, we size all columns to display everything. If the table is too small, for instance
 * when one of the cells contains a very long string, we make the biggest column(s) smaller until the data does
 * fit.</quote>
 * <p>
 * Creation date: Mar 19, 2012 - 1:42:12 PM
 * 
 * @version 1.0
 */
public class ColumnsAutoSizer implements TableModelListener, ComponentListener {

    private final JTable mTable;

    /**
     * Creates a new <code>ColumnsAutoSizer</code>
     * 
     * @param table
     */
    private ColumnsAutoSizer(JTable table) {
        mTable = table;
    }

    /**
     * Sets the preferred width of the columns of a table from prototypes
     * 
     * @param table
     *            the target table
     * @param prototypes
     *            an array of prototypes, {@code null} values will be ignored
     * @param setMaxWidth
     *            {@code true} if the maximum column width should also be set
     */
    public static void setWidthFromPrototype(JTable table, Object[] prototypes, boolean setMaxWidth) {
        if (prototypes.length != table.getColumnCount())
            throw new IllegalArgumentException("The prototypes array should contain exactly "
                    + table.getColumnCount() + " elements");
        for (int i = 0; i < prototypes.length; i++) {
            if (prototypes[i] != null) {
                Component proto = table.getDefaultRenderer(prototypes[i].getClass())
                        .getTableCellRendererComponent(table, prototypes[i], false, false, 0, i);
                int prefWidth = (int) proto.getPreferredSize().getWidth() + 1;
                table.getColumnModel().getColumn(i).setPreferredWidth(prefWidth);
                if (setMaxWidth)
                    table.getColumnModel().getColumn(i).setMaxWidth(prefWidth);
            }
        }
    }

    /**
     * Add an autosize behavior to the given table
     * 
     * @param table
     */
    public static void addAutoSizer(JTable table) {
        ColumnsAutoSizer sizer = new ColumnsAutoSizer(table);
        table.getModel().addTableModelListener(sizer);
        table.addComponentListener(sizer);
        sizer.sizeColumnsToFit();
    }

    /**
     * Resize the columns to fit their contents
     * 
     * @param table
     *            the target table
     */
    public static void sizeColumnsToFit(JTable table) {
        sizeColumnsToFit(table, 5);
    }

    /**
     * Resize the columns to fit their contents
     * 
     * @param table
     *            the target table
     * @param columnMargin
     *            the margin to add to the column witdh
     */
    public static void sizeColumnsToFit(JTable table, int columnMargin) {
        JTableHeader tableHeader = table.getTableHeader();

        if (tableHeader == null) {
            // can't auto size a table without a header
            return;
        }

        FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

        int[] minWidths = new int[table.getColumnCount()];
        int[] maxWidths = new int[table.getColumnCount()];

        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));

            minWidths[columnIndex] = headerWidth + columnMargin;

            int maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);

            maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + columnMargin;
        }

        adjustMaximumWidths(table, minWidths, maxWidths);

        // int widestCol = findLargestIndex(maxWidths);

        for (int i = 0; i < minWidths.length; i++) {
            if (minWidths[i] > 0) {
                table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
            }

            if (maxWidths[i] > 0) {
                // if (widestCol == i)
                // table.getColumnModel().getColumn(i).setMaxWidth(Integer.MAX_VALUE);
                // else
                table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);

                table.getColumnModel().getColumn(i).setWidth(maxWidths[i]);
            }
        }
    }

    private static void adjustMaximumWidths(JTable table, int[] minWidths, int[] maxWidths) {
        if (table.getWidth() > 0) {
            // to prevent infinite loops in exceptional situations
            int breaker = 0;

            // keep stealing one pixel of the maximum width of the highest column until we can fit in the width of the
            // table
            while (sum(maxWidths) > table.getWidth() && breaker < 10000) {
                int highestWidthIndex = findLargestIndex(maxWidths);

                maxWidths[highestWidthIndex] -= 1;

                maxWidths[highestWidthIndex] = Math.max(maxWidths[highestWidthIndex],
                        minWidths[highestWidthIndex]);

                breaker++;
            }
        }
    }

    private static int getMaximalRequiredColumnWidth(JTable table, int columnIndex, int headerWidth) {
        int maxWidth = headerWidth;

        TableCellRenderer cellRenderer;

        for (int row = 0; row < table.getModel().getRowCount(); row++) {
            cellRenderer = table.getCellRenderer(row, columnIndex);
            Component rendererComponent = cellRenderer.getTableCellRendererComponent(table, table
                    .getModel().getValueAt(row, columnIndex), false, false, row, columnIndex);

            double valueWidth = rendererComponent.getPreferredSize().getWidth();

            maxWidth = (int) Math.max(maxWidth, valueWidth);
        }

        return maxWidth;
    }

    private static int findLargestIndex(int[] widths) {
        int largestIndex = 0;
        int largestValue = 0;

        for (int i = 0; i < widths.length; i++) {
            if (widths[i] > largestValue) {
                largestIndex = i;
                largestValue = widths[i];
            }
        }

        return largestIndex;
    }

    private static int sum(int[] widths) {
        int sum = 0;

        for (int width : widths) {
            sum += width;
        }

        return sum;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        sizeColumnsToFit();
    }

    private void sizeColumnsToFit() {
        sizeColumnsToFit(mTable);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        sizeColumnsToFit();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        sizeColumnsToFit();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

}
