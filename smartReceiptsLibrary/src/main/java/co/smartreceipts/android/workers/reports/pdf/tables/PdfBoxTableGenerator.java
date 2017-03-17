package co.smartreceipts.android.workers.reports.pdf.tables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.TableGenerator;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;


public class PdfBoxTableGenerator<DataType> implements TableGenerator<PdfBoxTable, DataType> {

    private final PdfBoxContext mContext;
    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;
    private final float mCellPadding = 6;

    public PdfBoxTableGenerator(@NonNull PdfBoxContext context,
                                @NonNull List<Column<DataType>> columns,
                                @Nullable Filter<DataType> receiptFilter,
                                boolean printHeaders,
                                boolean printFooters) {
        mContext = context;
        mColumns = columns;
        mFilter = receiptFilter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
    }

    @NonNull
    @Override
    public PdfBoxTable generate(@NonNull List<DataType> list) throws IOException {
        List<PdfBoxTableRow> rows = new ArrayList<>();
        PdfBoxTableRow footerRow = null;
        PdfBoxTableRow headerRow = null;
        final int colCount = mColumns.size();
        final List<DataType> filteredList = new ArrayList<>(list.size());

        float xStart = mContext.getPageMarginHorizontal();
        float xEnd = mContext.getPageSize().getWidth() - mContext.getPageMarginHorizontal();

        // calculate column widths
        float[] colWidths;
        ColumnWidthCalculator columnWidthCalculator = new ColumnWidthCalculator<>(
                list, mColumns, xEnd - xStart, mCellPadding, mContext.getFontManager().getFont(PdfFontStyle.TableHeader),
                mContext.getFontManager().getFont(PdfFontStyle.Default));
        colWidths = columnWidthCalculator.calculate();

        float tableWidth = xEnd - xStart;

        // Add the header
        if (mPrintHeaders) {
            FixedWidthTextCell[] cells = new FixedWidthTextCell[colCount];
            for (int i = 0; i < colCount; i++) {
                FixedWidthTextCell cell = new FixedWidthTextCell(
                        colWidths[i],
                        mCellPadding,
                        mColumns.get(i).getHeader(),
                        mContext.getFontManager().getFont(PdfFontStyle.TableHeader),
                        mContext.getColorManager().getColor(PdfColorStyle.Outline));
                cells[i] = cell;
            }

            headerRow = new PdfBoxTableRow(cells, tableWidth,
                    mContext.getColorManager().getColor(PdfColorStyle.TableHeader));

        }

        if (!list.isEmpty()) {

            // Add each row
            for (int j = 0; j < list.size(); j++) {
                final DataType data = list.get(j);

                if (mFilter == null || mFilter.accept(data)) {
                    FixedWidthTextCell[] cells = new FixedWidthTextCell[colCount];
                    for (int i = 0; i < colCount; i++) {
                        FixedWidthTextCell cell = new FixedWidthTextCell(
                                colWidths[i],
                                mCellPadding,
                                HeavyHandedReplaceIllegalCharacters.getSafeString(mColumns.get(i).getValue(data)),
                                mContext.getFontManager().getFont(PdfFontStyle.Default),
                                mContext.getColorManager().getColor(PdfColorStyle.Default));
                        cells[i] = cell;
                    }

                    PdfBoxTableRow row = new PdfBoxTableRow(cells, tableWidth, j % 2 == 0 ? null :
                            mContext.getColorManager().getColor(PdfColorStyle.TableCell));

                    rows.add(row);
                    filteredList.add(data);
                }
            }
        }

        // Add the footer
        if (mPrintFooters) {
            FixedWidthTextCell[] cells = new FixedWidthTextCell[colCount];
            for (int i = 0; i < colCount; i++) {
                FixedWidthTextCell cell = new FixedWidthTextCell(
                        colWidths[i],
                        mCellPadding,
                        mColumns.get(i).getFooter(filteredList),
                        mContext.getFontManager().getFont(PdfFontStyle.Default),
                        mContext.getColorManager().getColor(PdfColorStyle.Outline));
                cells[i] = cell;
            }
            footerRow = new PdfBoxTableRow(cells, tableWidth,
                    mContext.getColorManager().getColor(PdfColorStyle.TableHeader));
        }
        return new PdfBoxTable(rows, headerRow, footerRow);
    }

}
