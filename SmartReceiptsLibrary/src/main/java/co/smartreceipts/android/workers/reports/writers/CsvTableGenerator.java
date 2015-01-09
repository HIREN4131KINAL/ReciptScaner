package co.smartreceipts.android.workers.reports.writers;

import android.support.annotation.NonNull;

import com.itextpdf.text.pdf.PdfPTable;

import co.smartreceipts.android.workers.reports.columns.TableColumns;

/**
 * Implements the {@link TableGenerator} contract to generate
 * a {@link java.lang.String}, which is a CSV
 *
 * @author williambaumann
 */
public class CsvTableGenerator implements TableGenerator<String> {

    private static final String QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final String[] STRINGS_THAT_MUST_BE_QUOTED = { ",", "\"", "\n", "\r\n" };

    @Override
    @NonNull
    public String write(TableColumns columns) {
        final int columnCount = columns.getColumnCount();
        final StringBuilder csvBuilder = new StringBuilder("");
        while (columns.nextRow()) {
            for (int i = 0; i < columnCount; i++) {
                String csvColumn = columns.getValueAt(i);
                if (csvColumn.contains(QUOTE)) {
                    csvColumn = csvColumn.replace(QUOTE, ESCAPED_QUOTE);
                }
                for (String stringToQuote : STRINGS_THAT_MUST_BE_QUOTED) {
                    if (csvColumn.contains(stringToQuote)) {
                        csvColumn = QUOTE + csvColumn + QUOTE;
                        break;
                    }
                }
                csvBuilder.append(csvColumn);
                csvBuilder.append(",");
            }
            csvBuilder.append("\n");
        }
        return csvBuilder.toString();
    }
}
