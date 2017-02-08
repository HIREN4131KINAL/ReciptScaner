package co.smartreceipts.android.workers.reports;


import java.io.IOException;
import java.util.StringTokenizer;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

public class PdfBoxUtils {
    public static float getStringWidth(String text, PdfBoxContext.FontSpec fontSpec) throws IOException {
        return fontSpec.getFont().getStringWidth(text) * fontSpec.getSize() / 1000F;
    }

    public static float getFontHeight(PdfBoxContext.FontSpec fontSpec) throws IOException {
        return fontSpec.getFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000
                * fontSpec.getSize();
    }

    public static float getFontAboveBaselineHeight(PdfBoxContext.FontSpec fontSpec) throws IOException {
        return (fontSpec.getFont().getFontDescriptor().getFontBoundingBox().getHeight()
                + fontSpec.getFont().getFontDescriptor().getDescent()) //descent is negative
                / 1000 * fontSpec.getSize();
    }

    /**
     * Returns the min string width to display the string broken up into various lines by spaces.
     * That's the largest word width.
     * @param text
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getMaxWordWidth(String text, PdfBoxContext.FontSpec fontSpec) throws IOException {
        float min = 0.0f;

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            float w = getStringWidth(s, fontSpec);
            if (w > min) {
                min = w;
            }
        }
        return min;
    }

}
