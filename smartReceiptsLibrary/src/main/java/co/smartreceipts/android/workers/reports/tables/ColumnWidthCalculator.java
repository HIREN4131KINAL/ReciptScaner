package co.smartreceipts.android.workers.reports.tables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;



public class ColumnWidthCalculator<DataType> {


    private static final float EPSILON = 0.00001f;
    private final List<DataType> list;
    private final List<Column<DataType>> mColumns;
    private final float availableWidth;
    private final PdfBoxContext.FontSpec fontHeader;
    private final PdfBoxContext.FontSpec fontContent;
    private final float cellPadding;


    /**
     *
     * @param list
     * @param mColumns
     * @param availableWidth
     * @param fontHeader
     * @param fontContent
     */
    public ColumnWidthCalculator(List<DataType> list,
                                 List<Column<DataType>> mColumns,
                                 float availableWidth,
                                 float cellPadding,
                                 PdfBoxContext.FontSpec fontHeader,
                                 PdfBoxContext.FontSpec fontContent) {

        this.list = list;
        this.mColumns = mColumns;
        this.availableWidth = availableWidth;
        this.fontHeader = fontHeader;
        this.fontContent = fontContent;
        this.cellPadding = cellPadding;
    }


    public float[] calculate() throws IOException {

        float availableWidthExcludingPadding = availableWidth - 2*mColumns.size()*cellPadding;

        float[] widths = new float[mColumns.size()];
        ArrayList<ColumnAttributes> attrs = new ArrayList<ColumnAttributes>(mColumns.size());

        for (int i = 0; i < mColumns.size(); i++) {
            attrs.add(new ColumnAttributes(mColumns.get(i).getHeader(), list, i));
        }

        // TOO MANY COLUMNS CHECK
        // If columns do not fit with their min mWidth, abort
        float sumCheck = 0.0f;
        for (int i = 0; i < attrs.size(); i++) {
            float m = Math.max(attrs.get(i).headerMinWidth, attrs.get(i).contentMinWidth);
            sumCheck += m;
        }
        if (sumCheck > availableWidthExcludingPadding) {
            // TODO handle this differently
            throw new RuntimeException("TOO MANY COLUMNS");
        }


        // FIRST ATTEMPT
        // If all columns fit with their max mWidth, assign maxWidth
        // and then redistribute evenly
        for (int i = 0; i < attrs.size(); i++) {
            float m = Math.max(attrs.get(i).headerMaxWidth, attrs.get(i).contentMaxWidth);
            widths[i] = m + 2*cellPadding;
        }
        printWidths(widths);

        if (sum(widths) < availableWidth) {
            return distributeExtraSpaceEvenly(widths);
        }

        // TODO this is currently unused, remove it?
        boolean[] haveWrappedHeader = new boolean[mColumns.size()];

        // SECOND ATTEMPT
        // Wrap titles (maintaining content unwrapped)
        for (int i = 0; i < attrs.size(); i++) {
            // The second condition defensively wraps the title (not necessarilly to the minimum
            // header mWidth, but just up to the content's max mWidth)
            if (attrs.get(i).isHeaderBreakable
                    && attrs.get(i).contentMaxWidth < attrs.get(i).headerMaxWidth) {
                float m = Math.max(attrs.get(i).headerMinWidth, attrs.get(i).contentMaxWidth);
                widths[i] = m + 2*cellPadding;
                haveWrappedHeader[i] = true;
            }
        }
        printWidths(widths);
        if (sum(widths) < availableWidth) {
            return distributeExtraSpaceEvenly(widths);
        }


        // THIRD PASS
        // Wrap contents (and possibly further wrap title)
        boolean[] flex = new boolean[mColumns.size()];
        for (int i = 0; i < attrs.size(); i++) {
//            if (attrs.get(i).isContentFlex) {
                float m = Math.max(attrs.get(i).contentMinWidth, attrs.get(i).headerMinWidth);
                float newWidth = m + 2 * cellPadding;
                if (Math.abs(newWidth - widths[i]) > EPSILON) {
                    widths[i] = newWidth;
                    flex[i] = true;
                }
//            }
        }
        printWidths(widths);
        if (sum(widths) < availableWidth) {
            return distributeExtraSpaceOnlyToColumns(widths, flex);
        }



        throw new RuntimeException("FAIL");
//                return widths;
    }

    private void printWidths(float[] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append("Widths: ");
        for (float width : widths) {
            sb.append(" " + width);
        }
        System.out.println(sb.toString());
        Logger.debug(this, sb.toString());
    }

    private float[] distributeExtraSpaceOnlyToColumns(float[] widths, boolean[] flex) {
        int nCols = 0;
        for (boolean b : flex) {
            if (b) {
                nCols++;
            }
        }

        float extraSpace = availableWidth - sum(widths);

        for (int i = 0; i < widths.length; i++) {
            if (flex[i]) {
                widths[i] += extraSpace/nCols;
            }
        }
        printWidths(widths);

        return widths;
    }

    public float[] distributeExtraSpaceEvenly(float[] widths) {
        float extraSpace = availableWidth - sum(widths);
        for (int j = 0; j < widths.length; j++) {
            widths[j] += extraSpace/mColumns.size();
        }
        printWidths(widths);
        return widths;
    }

    private float sum(float[] widths) {
        float sum = 0.0f;
        for (float width : widths) {
            sum += width;
        }
        return sum;

    }


    class ColumnAttributes {
        float headerMinWidth;
        float headerMaxWidth;
        float contentMinWidth;
        float contentMaxWidth;
        boolean isHeaderBreakable;
        boolean isContentFlex;

        public ColumnAttributes(String header, List<DataType> list, int i) throws IOException {
            headerMaxWidth = PdfBoxUtils.getStringWidth(header, fontHeader);
            headerMinWidth = PdfBoxUtils.getMaxWordWidth(header, fontHeader);


            float maxOfAllStringWidths = 0.0f;  // the max string mWidth of all values (without breaking up the string)
            float minOfAllStringWidths = Float.MAX_VALUE;  // the min string mWidth of all values (without breaking up the string)
            float maxOfMaxWordWidths = 0.0f;    // the global max of the



            for (DataType dataType : list) {
                String v = mColumns.get(i).getValue(dataType);

                float vWidth = PdfBoxUtils.getStringWidth(v, fontContent);
                float vMaxWordWidth = PdfBoxUtils.getMaxWordWidth(v, fontContent);

                if (vWidth > maxOfAllStringWidths) {
                    maxOfAllStringWidths = vWidth;
                }
                if (vWidth < minOfAllStringWidths) {
                    minOfAllStringWidths = vWidth;
                }

                if (vMaxWordWidth > maxOfMaxWordWidths) {
                    maxOfMaxWordWidths = vMaxWordWidth;
                }
            }

            contentMaxWidth = maxOfAllStringWidths;
            contentMinWidth = maxOfMaxWordWidths;


            isHeaderBreakable = header.contains(" ");

            isContentFlex = maxOfAllStringWidths / minOfAllStringWidths > 1.2f;


        }
    }
}
