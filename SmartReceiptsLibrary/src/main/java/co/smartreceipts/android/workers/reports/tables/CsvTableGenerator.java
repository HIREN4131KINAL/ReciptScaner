package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;

/**
 * Implements the {@link TableGenerator} contract to generate
 * a {@link java.lang.String}, which is a CSV
 *
 * @author williambaumann
 */
public final class CsvTableGenerator<DataType> implements TableGenerator<String, DataType> {

    private static final String QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final String[] STRINGS_THAT_MUST_BE_QUOTED = {",", "\"", "\n", "\r\n"};

    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;

    public CsvTableGenerator(@NonNull List<Column<DataType>> columns, boolean printHeaders, boolean printFooters) {
        this(columns, null, printHeaders, printFooters);
    }

    public CsvTableGenerator(@NonNull List<Column<DataType>> columns, @Nullable Filter<DataType> filter, boolean printHeaders, boolean printFooters) {
        mColumns = columns;
        mFilter = filter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
    }

    @NonNull
    @Override
    public String generate(@NonNull List<DataType> list) {
        if (!list.isEmpty()) {
            final int columnCount = mColumns.size();
            final StringBuilder csvBuilder = new StringBuilder("");

            // Add the header
            if (mPrintHeaders) {
                for (int i = 0; i < columnCount; i++) {
                    addCell(csvBuilder, mColumns.get(i).getHeader());
                }
                csvBuilder.append("\n");
            }

            // Add each row
            final List<DataType> filteredList = new ArrayList<DataType>(list.size());
            for (int j = 0; j < list.size(); j++) {
                final DataType data = list.get(j);
                for (int i = 0; i < columnCount; i++) {
                    if (mFilter != null && mFilter.accept(data)) {
                        addCell(csvBuilder, mColumns.get(i).getValue(data));
                        filteredList.add(data);
                    }
                }
                csvBuilder.append("\n");
            }

            // Add the footer
            if (mPrintFooters) {
                for (int i = 0; i < columnCount; i++) {
                    addCell(csvBuilder, mColumns.get(i).getFooter(filteredList));
                }
                csvBuilder.append("\n");
            }
            return csvBuilder.toString();
        } else {
            return ""; // Just return an empty csv if we don't have any objects
        }
    }


    private void addCell(@NonNull StringBuilder csvBuilder, @Nullable String value) {
        if (value != null) {
            if (value.contains(QUOTE)) {
                value = value.replace(QUOTE, ESCAPED_QUOTE);
            }
            for (String stringToQuote : STRINGS_THAT_MUST_BE_QUOTED) {
                if (value.contains(stringToQuote)) {
                    value = QUOTE + value + QUOTE;
                    break;
                }
            }
            csvBuilder.append(value);
        }
        csvBuilder.append(",");
    }

}
