package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

public class FixedWidthTextCell implements FixedWidthCell {

    private final float mWidth;
    private final String mText;
    private final PdfBoxContext.FontSpec mFontSpec;
    private final AWTColor mColor;
    private final float mCellPadding;

    private List<String> lines;

    public FixedWidthTextCell(float width, float cellPadding, String text,
                              @NonNull PdfBoxContext.FontSpec fontSpec,
                              @NonNull AWTColor color) {
        mWidth = width;
        mCellPadding = cellPadding;
        mText = text;
        mFontSpec = fontSpec;
        mColor = color;
    }

    private void breakUpString(String text) throws IOException {
        lines = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            sb.append(token).append(" ");
            String currentLine = sb.toString();
            if (PdfBoxUtils.getStringWidth(currentLine, mFontSpec) > mWidth) {
                if (sb.indexOf(token) > 0) {
                    lines.add(sb.substring(0, sb.indexOf(token)));
                    sb = new StringBuilder();
                    sb.append(token).append(" ");
                } else {
                    lines.add(sb.toString().trim());
                    sb = new StringBuilder();
                }
            }
        }
        if (!sb.toString().isEmpty()) {
            lines.add(sb.toString().trim());
        }
    }


    public float getWidth() {
        return mWidth;
    }

    @Override
    public float getHeight() {
        try {
            breakUpString(mText);
            return PdfBoxUtils.getFontHeight(mFontSpec) * lines.size() + 2 * mCellPadding;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public List<String> getLines() throws IOException {
        // TODO, that's not very cool... Should we initialize lines in the constructor?
        if (lines == null) {
            breakUpString(mText);
        }
        return lines;
    }

    public PdfBoxContext.FontSpec getFontSpec() {
        return mFontSpec;
    }

    public AWTColor getColor() {
        return mColor;
    }

    @Override
    public float getCellPadding() {
        return mCellPadding;
    }
}
