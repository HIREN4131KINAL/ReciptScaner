package co.smartreceipts.android.workers.reports.tables;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

class FixedWidthCell {

    private final float width;
    private final String text;
    private final PdfBoxContext.FontSpec fontSpec;
    private final AWTColor color;
    private List<String> lines;
    private float cellPadding;

    public FixedWidthCell(float width, float cellPadding, String text,
                          PdfBoxContext.FontSpec fontSpec,
                          AWTColor color) {
        this.width = width;
        this.cellPadding = cellPadding;
        this.text = text;
        this.fontSpec = fontSpec;
        this.color = color;
    }

    private void breakUpString(String text) throws IOException {
        lines = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            sb.append(token).append(" ");
            String currentLine = sb.toString();
            if (PdfBoxUtils.getStringWidth(currentLine, fontSpec) > width) {
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
        return width;
    }

    public float getHeight() {
        try {
            breakUpString(text);
            return PdfBoxUtils.getFontHeight(fontSpec) * lines.size() + 2 * cellPadding;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public List<String> getLines() {
        return lines;
    }

    public PdfBoxContext.FontSpec getFontSpec() {
        return fontSpec;
    }

    public AWTColor getColor() {
        return color;
    }

    public float getCellPadding() {
        return cellPadding;
    }
}
