package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.pdf.tables.ImagesWithLegendGrid;
import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTableRow;
import co.smartreceipts.android.workers.reports.TableGenerator;


/**
 * Generates a table of images. Every image has a legend that is displayed above the image.
 */
public class PdfBoxImageTableGenerator implements TableGenerator<PdfBoxImageTable, Receipt> {

    private static final int DEFAULT_COLUMN_COUNT = 2;
    private static final int DEFAULT_ROW_COUNT = 2;
    private static final String SEP = " - ";
    
    private final PdfBoxContext pdfBoxContext;
    private final Filter<Receipt> filter;
    private final UserPreferenceManager userPreferenceManager;

    /**
     * The width of the free space that we have in the page for painting the table
     */
    private final float availableWidth;
    /**
     * The height of the free space that we have in the page for painting the table
     */
    private final float availableHeight;

    private final float cellPadding = 4;


    public PdfBoxImageTableGenerator(@NonNull PdfBoxContext context, @NonNull Filter<Receipt> filter,
                                     float availableWidth, float availableHeight) {
        this.pdfBoxContext = Preconditions.checkNotNull(context);
        this.filter = Preconditions.checkNotNull(filter);
        this.userPreferenceManager = Preconditions.checkNotNull(context.getPreferences());
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
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
                if (!filter.accept(receipt) || receipt.getFile() == null) {
                    continue;
                }

                if (receipt.isFullPage() || receipt.hasPDF()) {
                    // If we have a row in process, finish
                    if (grid != null && !grid.isEmpty()) {
                        rows.addAll(grid.getRows());
                    }
                    grid = new ImagesWithLegendGrid(pdfBoxContext, availableWidth, availableHeight, cellPadding, 1, 1);

                    // TODO: If it's a pdf, add multiple instances of these
                    // TODO: 2 - make Lollipop the minimum version?
                    // TODO: Each needs it's own 'page' number id in the current formating. Should I re-write? Maybe
                } else {
                    if (grid == null) {
                        grid = new ImagesWithLegendGrid(pdfBoxContext, availableWidth,
                                availableHeight, cellPadding, DEFAULT_ROW_COUNT, DEFAULT_COLUMN_COUNT);
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
    private String buildLegendForImage(@NonNull Receipt receipt) {
        final int num = (userPreferenceManager.get(UserPreference.ReportOutput.PrintUserIdByPdfPhoto)) ?
                receipt.getId() : receipt.getIndex();

        final String extra = (userPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)
                && !TextUtils.isEmpty(receipt.getComment()))
                ? SEP + receipt.getComment()
                : "";

        return num + SEP + receipt.getName() + SEP
                + receipt.getFormattedDate(pdfBoxContext.getAndroidContext(),
                userPreferenceManager.get(UserPreference.General.DateSeparator)) + extra;
    }
}
