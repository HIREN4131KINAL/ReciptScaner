package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.workers.reports.tables.FixedSizeImageCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthCell;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;
import co.smartreceipts.android.workers.reports.tables.TableGenerator;


// TODO apply filters etc
public class PdfBoxImageTableGenerator implements TableGenerator<PdfBoxImageTable, Receipt> {

    private final PdfBoxContext context;
    private final Filter<Receipt> mFilter;
    private final float availableWidth;
    private final float availableHeight;
    private float cellPadding = 4;
    private static final int NCOLS = 2;
    private static final int NROWS = 2;

    public PdfBoxImageTableGenerator(PdfBoxContext context, Filter<Receipt> mFilter,
                                     float availableWidth, float availableHeight) {
        this.context = context;
        this.mFilter = mFilter;
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
    }


    @NonNull
    @Override
    public PdfBoxImageTable generate(@NonNull List<Receipt> list) {
        List<PdfBoxTableRow> rows = new ArrayList<>();

        float cellWidth = availableWidth / NCOLS;

        float cellHeight = availableHeight / NROWS;

        FixedWidthCell[] cells = new FixedWidthCell[NCOLS];
        FixedSizeImageCell cell;
        int k = 0;
        if (!list.isEmpty()) {

            for (int j = 0; j < list.size(); j++) {
                final Receipt receipt = list.get(j);
                if (!receipt.hasImage()) {
                    continue;
                }

                cell = new FixedSizeImageCell(cellWidth, cellHeight,
                        "TEXT",
                        context.getFont(DefaultPdfBoxContext.FONT_DEFAULT),
                        AWTColor.BLACK,
                        cellPadding,
                        receipt.getImage()
                );

                cells[k++] = cell;
                if (k == NCOLS) {
                    PdfBoxTableRow row = new PdfBoxTableRow(cells, availableWidth, null);
                    rows.add(row);
                    cells = new FixedWidthCell[NCOLS];
                    k=0;
                }

            }

            // Add remaining cells (incomplete row)
            if (k<NCOLS) {
                PdfBoxTableRow row = new PdfBoxTableRow(cells, availableWidth, AWTColor.WHITE);
                rows.add(row);
            }
        }

        return new PdfBoxImageTable(rows, null, null);
    }
}
