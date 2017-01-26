package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.tables.ImagesWithLegendGrid;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;
import co.smartreceipts.android.workers.reports.tables.TableGenerator;


/**
 * Generates a table of images. Every image has a legend that is displayed above the image.
 */
public class PdfBoxImageTableGenerator implements TableGenerator<PdfBoxImageTable, Receipt> {

    // TODO separator
    private static final String SEP = " - ";
    private final PdfBoxContext context;
    private final Filter<Receipt> mFilter;

    /**
     * The width of the free space that we have in the page for painting the table
     */
    private final float availableWidth;
    /**
     * The height of the free space that we have in the page for painting the table
     */
    private final float availableHeight;

    private final Preferences preferences;

    private float cellPadding = 4;
    private static final int NCOLS = 2;
    private static final int NROWS = 2;

    /**
     * @param context
     * @param mFilter
     * @param availableWidth
     * @param availableHeight
     */
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

        ImagesWithLegendGrid grid = null;
        if (!list.isEmpty()) {

            for (int j = 0; j < list.size(); j++) {
                final Receipt receipt = list.get(j);

                // If filter rejects image or no image, skip
                if (!mFilter.accept(receipt) || !receipt.hasFile()) {
                    continue;
                }

                if (receipt.isFullPage() || receipt.hasPDF()) {
                    // If we have a row in process, finish
                    if (grid != null && !grid.isEmpty()) {
                        rows.addAll(grid.getRows());
                    }
                    grid = new ImagesWithLegendGrid(context, availableWidth,
                            availableHeight, cellPadding, 1, 1);
                } else {
                    if (grid == null) {
                        grid = new ImagesWithLegendGrid(context, availableWidth,
                                availableHeight, cellPadding, NROWS, NCOLS);
                    }
                }

                final String text = buildLegendForImage(receipt);
                grid.addImageAndLegend(text, receipt.getFile());

                if (grid.isComplete()) {
                    rows.addAll(grid.getRows());
                    grid = null;
                }
            }

            // Add remaining cells (incomplete row)
            if (grid != null) {
                rows.addAll(grid.getRows());
            }
        }

        return new PdfBoxImageTable(rows, null, null);
    }



    @NonNull
    private String buildLegendForImage(Receipt receipt) {
        final int num = (preferences.includeReceiptIdInsteadOfIndexByPhoto())
                ? receipt.getId() : receipt.getIndex();
        final String extra = (preferences.getIncludeCommentByReceiptPhoto()
                && !TextUtils.isEmpty(receipt.getComment()))
                ? SEP + receipt.getComment()
                : "";
        return num + SEP + receipt.getName() + SEP
                + receipt.getFormattedDate(context.getApplicationContext(),
                preferences.getDateSeparator()) + extra;
    }
}
