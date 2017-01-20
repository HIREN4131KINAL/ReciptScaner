package co.smartreceipts.android.workers.reports.tables;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.DefaultPdfBoxContext;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

/**
 * Represents a grid of images with their legends. The size of the grid can be specified with the
 * params <code>nRows</code> and <code>nCols</code> (and can be 1x1 for full page images).
 * Each cell of the grid will contain two {@link FixedWidthCell}
 */
public class ImagesWithLegendGrid {

    List<FixedWidthCell> textCells;
    List<FixedWidthCell> imageCells;

    float width;
    float height;
    private final int nRows;
    private final int nCols;

    private List<PdfBoxTableRow> rows = new ArrayList<>();
    private float cellPadding;
    private PdfBoxContext context;
    private int nItems;

    /**
     * @param context     The {@link PdfBoxContext}
     * @param width       The width of the area that we have available in the pdf
     *                    page to paint the grid
     * @param height      The width of the area that we have available in the pdf
     *                    page to paint the grid
     * @param cellPadding The padding that will be applied to all cells of the grid
     * @param nRows       The number of rows the grid will have
     * @param nCols       The number of columns the grid will have
     */
    public ImagesWithLegendGrid(PdfBoxContext context, float width,
                                float height, float cellPadding, int nRows, int nCols) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.cellPadding = cellPadding;
        this.nRows = nRows;
        this.nCols = nCols;
        textCells = new ArrayList<>();
        imageCells = new ArrayList<>();
    }


    public void addImageAndLegend(String text, File image) {

        FixedWidthTextCell textCell = new FixedWidthTextCell(width / nCols, cellPadding, text,
                context.getFont(DefaultPdfBoxContext.FONT_SMALL),
                AWTColor.BLACK);

        // leave height empty, we will calculate it later.
        FixedSizeImageCell imageCell = new FixedSizeImageCell(width / nCols, 0,
                cellPadding,
                image
        );

        textCells.add(textCell);
        imageCells.add(imageCell);

        nItems++;
    }

    public boolean isEmpty() {
        return nItems == 0;
    }

    public boolean isComplete() {
        return nItems == nCols * nRows;
    }

    /**
     * Finalizes the grid, by creating and returning the {@link PdfBoxTableRow} that
     * hold the grid's content. For every "conceptual row" of the grid, we create 2
     * {@link PdfBoxTableRow}s, one for the legends, and another one for the images.
     * @return
     */
    public List<PdfBoxTableRow> getRows() {
        // Fill in the grid with dummy elements if required
        for (int k = nItems; k < nCols * nRows; k++) {
            addImageAndLegend("", null);
        }

        for (int i = 0; i < nRows; i++) {
            FixedWidthCell[] rowTextCells = new FixedWidthCell[nCols];
            FixedWidthCell[] rowImageCells = new FixedWidthCell[nCols];

            for (int j = 0; j < nCols; j++) {
                rowTextCells[j] = textCells.get(i * nCols + j);
                rowImageCells[j] = imageCells.get(i * nCols + j);
            }

            PdfBoxTableRow textRow = new PdfBoxTableRow(rowTextCells, width, null);
            PdfBoxTableRow imageRow = new PdfBoxTableRow(rowImageCells, width, null);

            // Now set the height of the image cell, by subtracting the textRow height
            // from the available space that we have in each cell of the grid.
            for (int j = 0; j < nCols; j++) {
                ((FixedSizeImageCell) rowImageCells[i]).setHeight(height / nRows - textRow.getHeight());
            }

            rows.add(textRow);
            rows.add(imageRow);
        }

        return rows;
    }
}
