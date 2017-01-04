package co.smartreceipts.android.workers.reports.tables;

import com.tom_roush.pdfbox.util.awt.AWTColor;

public class PdfBoxTableRow {
    private final FixedWidthCell[] cells;

    private AWTColor backgroundColor;
    private float width;


    public PdfBoxTableRow(FixedWidthCell[] cells, float width, AWTColor backgroundColor) {
        this.cells = cells;
        this.width = width;
        this.backgroundColor = backgroundColor;
    }

    public AWTColor getBackgroundColor() {
        return backgroundColor;
    }


    public float getWidth() {
        return width;
    }

    public float getHeight() {
        float rowHeight = 0;
        for (int i = 0; i < cells.length; i++) {
            rowHeight = Math.max(cells[i].getHeight(), rowHeight);
        }
        return rowHeight;
    }


    public FixedWidthCell[] getCells() {
        return cells;
    }
}
