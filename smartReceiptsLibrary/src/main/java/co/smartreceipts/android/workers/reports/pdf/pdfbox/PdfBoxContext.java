package co.smartreceipts.android.workers.reports.pdf.pdfbox;


import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.font.PDFont;

public interface PdfBoxContext {

    PDFont getFont();
    int getFontSize();
    String getString(@StringRes int resId, Object... args);
}
