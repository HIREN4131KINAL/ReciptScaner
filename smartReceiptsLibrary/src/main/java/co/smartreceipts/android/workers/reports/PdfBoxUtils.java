package co.smartreceipts.android.workers.reports;


import java.io.IOException;

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


}
