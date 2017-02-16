package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.tables.ImagesWithLegendGrid;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;
import co.smartreceipts.android.workers.reports.tables.TableGenerator;


/**
 * Generates a table of images. Every image has a legend that is displayed above the image.
 */
public class PdfBoxImageTableGenerator implements TableGenerator<PdfBoxImageTable, Receipt> {

    // TODO separator
    private static final String SEP = " - ";
    
    private final PdfBoxContext mContext;
    private final Filter<Receipt> mFilter;

    /**
     * The width of the free space that we have in the page for painting the table
     */
    private final float mAvailableWidth;
    /**
     * The height of the free space that we have in the page for painting the table
     */
    private final float mAvailableHeight;

    private final UserPreferenceManager mPreferences;

    private final float mCellPadding = 4;
    private static final int NCOLS = 2;
    private static final int NROWS = 2;

    /**
     * @param context
     * @param filter
     * @param availableWidth
     * @param availableHeight
     */
    public PdfBoxImageTableGenerator(@NonNull PdfBoxContext context, 
                                     @Nullable Filter<Receipt> filter,
                                     float availableWidth, float availableHeight) {
        mContext = context;
        mFilter = filter;
        mAvailableWidth = availableWidth;
        mAvailableHeight = availableHeight;
        mPreferences = context.getPreferences();
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
                    grid = new ImagesWithLegendGrid(mContext, mAvailableWidth,
                            mAvailableHeight, mCellPadding, 1, 1);
                } else {
                    if (grid == null) {
                        grid = new ImagesWithLegendGrid(mContext, mAvailableWidth,
                                mAvailableHeight, mCellPadding, NROWS, NCOLS);
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
        final int num = (mPreferences.get(UserPreference.ReportOutput.PrintUserIdByPdfPhoto))
                ? receipt.getId() : receipt.getIndex();
        final String extra = (mPreferences.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)
                && !TextUtils.isEmpty(receipt.getComment()))
                ? SEP + receipt.getComment()
                : "";
        return num + SEP + receipt.getName() + SEP
                + receipt.getFormattedDate(mContext.getAndroidContext(),
                mPreferences.get(UserPreference.General.DateSeparator)) + extra;
    }
}
