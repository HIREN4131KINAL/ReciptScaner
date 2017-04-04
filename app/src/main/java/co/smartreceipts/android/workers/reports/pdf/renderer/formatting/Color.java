package co.smartreceipts.android.workers.reports.pdf.renderer.formatting;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

public class Color extends AbstractFormatting<AWTColor> {

    public Color(@NonNull AWTColor color) {
        super(color, AWTColor.class);
    }
}
