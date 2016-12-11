package co.smartreceipts.android.workers.reports.pdf.pdfbox;


import android.content.Context;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.font.PDFont;

public interface PdfBoxContext {

    PDFont getFont();
    int getFontSize();
    int getLineBreakOffset();
    int getPageOffsetX();
    int getPageOffsetY();
    String getString(@StringRes int resId, Object... args);

    String getDateSeparator();

    Context getApplicationContext();
}
