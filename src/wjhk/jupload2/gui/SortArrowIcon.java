package wjhk.jupload2.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Icon implementation, to control and indicate the current sort order, into the
 * {@link wjhk.jupload2.gui.FilePanelJTable}.
 */
public class SortArrowIcon implements Icon {
    /**
     * Don't draw an arrow.
     */
    public static final int NONE = 0;

    /**
     * Draw arrow, representing descending sort order.
     */
    public static final int DESCENDING = 1;

    /**
     * Draw arrow, representing ascending sort order.
     */
    public static final int ASCENDING = 2;

    protected int direction;

    protected int width = 8;

    protected int height = 8;

    /**
     * Creates a new instance.
     * 
     * @param direction The desired direction, either {@link #ASCENDING},
     *            {@link #DESCENDING} or {@link #NONE}
     */
    public SortArrowIcon(int direction) {
        this.direction = direction;
    }

    /**
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return this.width;
    }

    /**
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return this.height;
    }

    /**
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
     *      int, int)
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        final Color bg = c.getBackground();
        final Color light = bg.brighter().brighter();
        final Color shade = bg.darker().darker();

        final int w = this.width;
        final int h = this.height;
        final int m = w / 2;
        switch (this.direction) {
            case ASCENDING:
                g.setColor(shade);
                g.drawLine(x, y, x + w, y);
                g.drawLine(x, y, x + m, y + h);
                g.setColor(light);
                g.drawLine(x + w, y, x + m, y + h);
                break;
            case DESCENDING:
                g.setColor(shade);
                g.drawLine(x + m, y, x, y + h);
                g.setColor(light);
                g.drawLine(x, y + h, x + w, y + h);
                g.drawLine(x + m, y, x + w, y + h);
                break;
        }
    }
}
