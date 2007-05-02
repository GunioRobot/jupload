package wjhk.jupload2.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Technical class, used to add tooltips to names. Used in
 * {@link wjhk.jupload2.gui.FilePanelJTable}.
 */
public class NameRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -3096051530077250460L;

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value instanceof String) {
            setToolTipText((String) value);
        }
        return cell;
    }
}
