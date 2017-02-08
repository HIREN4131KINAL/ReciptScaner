package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.DefaultPdfBoxContext;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

/**
 * Represents a grid of images with their legends. The size of the grid can be specified with the
 * params <code>mRows</code> and <code>mCols</code> (and can be 1x1 for full page images).
 * Each cell of the grid will contain two {@link FixedWidthCell}
 */
public class ImagesWithLegendGrid {

    List<FixedWidthCell> mTextCells;
    List<FixedWidthCell> mImageCells;

    private final float mWidth;
    private final float mHeight;
    private final int mRows;
    private final int mCols;

    private float mCellPadding;
    private int nItems;

    private List<PdfBoxTableRow> rows = new ArrayList<>();
    private PdfBoxContext mContext;

    /**
     * @param context     The {@link PdfBoxContext}
     * @param width       The mWidth of the area that we have available in the pdf
     *                    page to paint the grid
     * @param height      The mWidth of the area that we have available in the pdf
     *                    page to paint the grid
     * @param cellPadding The padding that will be applied to all cells of the grid
     * @param nRows       The number of rows the grid will have
     * @param nCols       The number of columns the grid will have
     */
    public ImagesWithLegendGrid(@NonNull PdfBoxContext context,
                                float width,
                                float height,
                                float cellPadding,
                                int nRows,
                                int nCols) {
        mContext = context;
        mWidth = width;
        mHeight = height;
        mCellPadding = cellPadding;
        mRows = nRows;
        mCols = nCols;
        mTextCells = new ArrayList<>();
        mImageCells = new ArrayList<>();
    }


    public void addImageAndLegend(String text, File image) {

        FixedWidthTextCell textCell = new FixedWidthTextCell(mWidth / mCols, mCellPadding, text,
                mContext.getFont(DefaultPdfBoxContext.FONT_SMALL),
                AWTColor.BLACK);

        // leave height empty, we will calculate it later.
        FixedSizeImageCell imageCell = new FixedSizeImageCell(mWidth / mCols, 0,
                mCellPadding,
                image
        );

        mTextCells.add(textCell);
        mImageCells.add(imageCell);

        nItems++;
    }

    public boolean isEmpty() {
        return nItems == 0;
    }

    public boolean isComplete() {
        return nItems == mCols * mRows;
    }

    /**
     * Finalizes the grid, by creating and returning the {@link PdfBoxTableRow} that
     * hold the grid's content. For every "conceptual row" of the grid, we create 2
     * {@link PdfBoxTableRow}s, one for the legends, and another one for the images.
     * @return
     */
    @NonNull
    public List<PdfBoxTableRow> getRows() {
        // Fill in the grid with dummy elements if required
        for (int k = nItems; k < mCols * mRows; k++) {
            addImageAndLegend("", null);
        }

        for (int i = 0; i < mRows; i++) {
            FixedWidthCell[] rowTextCells = new FixedWidthCell[mCols];
            FixedWidthCell[] rowImageCells = new FixedWidthCell[mCols];

            for (int j = 0; j < mCols; j++) {
                rowTextCells[j] = mTextCells.get(i * mCols + j);
                rowImageCells[j] = mImageCells.get(i * mCols + j);
            }

            PdfBoxTableRow textRow = new PdfBoxTableRow(rowTextCells, mWidth, null);
            PdfBoxTableRow imageRow = new PdfBoxTableRow(rowImageCells, mWidth, null);

            // Now set the height of the image cell, by subtracting the textRow height
            // from the available space that we have in each cell of the grid.
            for (int j = 0; j < mCols; j++) {
                ((FixedSizeImageCell) rowImageCells[i]).setHeight(mHeight / mRows - textRow.getHeight());
            }

            rows.add(textRow);
            rows.add(imageRow);
        }

        return rows;
    }
}
