package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;

import static android.R.attr.y;


public class PdfBoxTableGenerator<DataType> implements TableGenerator<Void, DataType> {

    private final PDPageContentStream mContentStream;
    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;
    private final int mCurrentLineOffset;

    public PdfBoxTableGenerator(PDPageContentStream contentStream,
                                List<Column<DataType>> columns,
                                Filter<DataType> receiptFilter,
                                boolean printHeaders,
                                boolean printFooters,
                                int currentLineOffset) {
        mContentStream = contentStream;
        mColumns = columns;
        mFilter = receiptFilter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
        mCurrentLineOffset = currentLineOffset;
    }

    @NonNull
    @Override
    public Void generate(@NonNull List<DataType> list) {
        try {
            if (!list.isEmpty()) {
                final int columnCount = mColumns.size();
                final List<DataType> filteredList = new ArrayList<>(list.size());
                final int rowCount = filteredList.size()
                        + (mPrintHeaders ? 1 : 0)
                        + (mPrintFooters ? 1 : 0);


                //draw the rows
                float nexty = mCurrentLineOffset;
                for (int i = 0; i <= rowCount; i++) {
                    mContentStream.moveTo(100, nexty);
                    mContentStream.lineTo(500, nexty);
                    mContentStream.stroke();
                    nexty -= 20;
                }


                // Add the header
                if (mPrintHeaders) {
                    for (int i = 0; i < columnCount; i++) {
                        // mColumns.get(i).getHeader();
                    }
                }

                // Add each row
                for (int j = 0; j < list.size(); j++) {
                    final DataType data = list.get(j);
                    if (mFilter == null || mFilter.accept(data)) {
                        for (int i = 0; i < columnCount; i++) {
                            // mColumns.get(i).getValue(data);
                        }
                        filteredList.add(data);
                    }
                }


                // Add the footer
                if (mPrintFooters) {
                    for (int i = 0; i < columnCount; i++) {
                        // mColumns.get(i).getFooter(filteredList)
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return null;
    }
}
