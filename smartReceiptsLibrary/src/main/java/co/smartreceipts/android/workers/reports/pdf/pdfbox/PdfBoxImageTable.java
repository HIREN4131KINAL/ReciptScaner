package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import java.util.List;

import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;

public class PdfBoxImageTable extends PdfBoxTable {

    public PdfBoxImageTable(List<PdfBoxTableRow> rows,
                            PdfBoxTableRow headerRow,
                            PdfBoxTableRow footerRow) {
        super(rows, headerRow, footerRow);
    }
}
