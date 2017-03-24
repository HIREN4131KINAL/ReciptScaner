package co.smartreceipts.android.workers.reports.pdf.tables;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public class PdfBoxTable {
    private final List<PdfBoxTableRow> mRows;
    private final PdfBoxTableRow mHeaderRow;
    private final PdfBoxTableRow mFooterRow;

    public PdfBoxTable(@NonNull List<PdfBoxTableRow> rows,
                       @Nullable PdfBoxTableRow headerRow,
                       @Nullable PdfBoxTableRow footerRow) {
        this.mRows = rows;
        this.mHeaderRow = headerRow;
        this.mFooterRow = footerRow;
    }

    public List<PdfBoxTableRow> getRows() {
        return mRows;
    }

    public PdfBoxTableRow getHeaderRow() {
        return mHeaderRow;
    }

    public PdfBoxTableRow getFooterRow() {
        return mFooterRow;
    }
}
