package co.smartreceipts.android.workers.reports;


import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.StringTokenizer;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

public class PdfBoxUtils {

    /**
     * Returns the width of a string, when rendered with the specified font
     * @param text
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getStringWidth(@NonNull String text, @NonNull PdfBoxContext.FontSpec fontSpec)
            throws IOException {
        return fontSpec.getFont().getStringWidth(text) * fontSpec.getSize() / 1000F;
    }

    /**
     * Returns the full height of a font (including the bounding box).
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getFontHeight(@NonNull PdfBoxContext.FontSpec fontSpec) {
        return fontSpec.getFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000
                * fontSpec.getSize();
    }


    /**
     * Returns the height of the font, excluding the descent, ie starting from the baseline and
     * above.
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getFontAboveBaselineHeight(@NonNull PdfBoxContext.FontSpec fontSpec) {
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
    public static float getMaxWordWidth(@NonNull String text, @NonNull PdfBoxContext.FontSpec fontSpec) throws IOException {
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
