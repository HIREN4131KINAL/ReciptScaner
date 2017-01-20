package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.tables.FixedSizeImageCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthTextCell;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;
import co.smartreceipts.android.workers.reports.tables.TableGenerator;


// TODO apply filters etc

/**
 * Generates a table of images. Every image has a legend that is displayed above the image.
 */
public class PdfBoxImageTableGenerator implements TableGenerator<PdfBoxImageTable, Receipt> {

    // TODO separator
    private static final String SEP = " - ";
    private final PdfBoxContext context;
    private final Filter<Receipt> mFilter;
    private final float availableWidth;
    private final float availableHeight;
    private final Preferences preferences;
    private float cellPadding = 4;
    private static final int NCOLS = 2;
    private static final int NROWS = 2;

    public PdfBoxImageTableGenerator(PdfBoxContext context, Filter<Receipt> mFilter,
                                     float availableWidth, float availableHeight) {
        this.context = context;
        this.mFilter = mFilter;
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
        this.preferences = context.getPreferences();
    }



    /**
     * We will generate 2 {@link PdfBoxTableRow}s for each visible "row" of the table, one to hold
     * the legends and another for the images. We do this so that the images of the same row
     * are aligned with each other (in case the legends differ in height).
     */
    @NonNull
    @Override
    public PdfBoxImageTable generate(@NonNull List<Receipt> list) {
        List<PdfBoxTableRow> rows = new ArrayList<>();

        float cellWidth = availableWidth / NCOLS;

        float cellHeight = availableHeight / NROWS;

        FixedWidthCell[] textCells = new FixedWidthCell[NCOLS];
        FixedWidthCell[] imageCells = new FixedWidthCell[NCOLS];
        int k = 0;
        if (!list.isEmpty()) {

            for (int j = 0; j < list.size(); j++) {
                final Receipt receipt = list.get(j);
                if (!receipt.hasImage()) {
                    continue;
                }

                final int num = (preferences.includeReceiptIdInsteadOfIndexByPhoto())
                        ? receipt.getId() : receipt.getIndex();
                final String extra = (preferences.getIncludeCommentByReceiptPhoto()
                        && !TextUtils.isEmpty(receipt.getComment()))
                        ? SEP + receipt.getComment()
                        : "";
                final String text = num + SEP + receipt.getName() + SEP
                        + receipt.getFormattedDate(context.getApplicationContext(),
                        preferences.getDateSeparator()) + extra;

                FixedWidthTextCell textCell = new FixedWidthTextCell(cellWidth, cellPadding, text,
                        context.getFont(DefaultPdfBoxContext.FONT_SMALL),
                        AWTColor.BLACK);

                // leave height empty, we will calculate it later.
                FixedSizeImageCell imageCell = new FixedSizeImageCell(cellWidth, 0,
                        cellPadding,
                        receipt.getImage()
                );

                textCells[k] = textCell;
                imageCells[k] = imageCell;
                k++;
                if (k == NCOLS) {
                    PdfBoxTableRow textRow = new PdfBoxTableRow(textCells, availableWidth, null);
                    PdfBoxTableRow imageRow = new PdfBoxTableRow(imageCells, availableWidth, null);

                    for (int i=0; i <NCOLS; i++) {
                        ((FixedSizeImageCell) imageCells[i]).setHeight(cellHeight - textRow.getHeight());
                    }

                    rows.add(textRow);
                    rows.add(imageRow);
                    textCells = new FixedWidthCell[NCOLS];
                    imageCells = new FixedWidthCell[NCOLS];
                    k = 0;
                }
            }

            // Add remaining cells (incomplete row)
            if (k < NCOLS) {
                PdfBoxTableRow textRow = new PdfBoxTableRow(textCells, availableWidth, null);
                PdfBoxTableRow imagesRow = new PdfBoxTableRow(imageCells, availableWidth, AWTColor.WHITE);
                rows.add(textRow);
                rows.add(imagesRow);
            }
        }

        return new PdfBoxImageTable(rows, null, null);
    }
}
