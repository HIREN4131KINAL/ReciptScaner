package co.smartreceipts.android.workers.reports.tables;


import java.util.List;

public class PdfBoxTable {
    private List<PdfBoxTableRow> rows;
    private PdfBoxTableRow headerRow;

    private PdfBoxTableRow footerRow;

    public PdfBoxTable(List<PdfBoxTableRow> rows, PdfBoxTableRow headerRow, PdfBoxTableRow footerRow) {
        this.rows = rows;
        this.headerRow = headerRow;
        this.footerRow = footerRow;
    }

    public List<PdfBoxTableRow> getRows() {
        return rows;
    }

    public PdfBoxTableRow getHeaderRow() {
        return headerRow;
    }

    public PdfBoxTableRow getFooterRow() {
        return footerRow;
    }
}
