package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTableRow;

public class PdfBoxImageTable extends PdfBoxTable {


    PdfBoxImageTable(@NonNull List<PdfBoxTableRow> rows,
                     @Nullable PdfBoxTableRow headerRow,
                     @Nullable PdfBoxTableRow footerRow) {
        super(rows, headerRow, footerRow);
    }
}
