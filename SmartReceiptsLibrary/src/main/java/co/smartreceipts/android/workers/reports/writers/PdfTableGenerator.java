package co.smartreceipts.android.workers.reports.writers;

import android.support.annotation.NonNull;

import com.itextpdf.text.pdf.PdfPTable;

import co.smartreceipts.android.workers.reports.columns.TableColumns;

/**
 * Implements the {@link TableGenerator} contract to generate
 * a {@link com.itextpdf.text.pdf.PdfPTable}
 *
 * @author williambaumann
 */
public final class PdfTableGenerator implements TableGenerator<PdfPTable> {

    @Override
    @NonNull
    public PdfPTable write(TableColumns columns) {
        final int columnCount = columns.getColumnCount();
        final PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100);
        while (columns.nextRow()) {
            for (int i = 0; i < columnCount; i++) {
                table.addCell(columns.getValueAt(i));
            }
        }
        return table;
    }
}
