package co.smartreceipts.android.workers.reports.pdf.renderer.formatting;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;

public class Color extends AbstractFormatting<AWTColor> {

    public Color(@NonNull AWTColor color) {
        super(color, AWTColor.class);
    }
}
