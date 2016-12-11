package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

public class DefaultPdfBoxContext implements PdfBoxContext {


    private Context context;
    private String dateSeparator;

    public DefaultPdfBoxContext(Context context, String dateSeparator) {
        this.context = context;
        this.dateSeparator = dateSeparator;
    }

    @Override
    public PDFont getFont() {
        return PDType1Font.HELVETICA;
    }

    @Override
    public int getFontSize() {
        return 14;
    }

    @Override
    public int getLineBreakOffset() {
        return 20;
    }

    @Override
    public int getPageOffsetX() {
        return 50;
    }

    @Override
    public int getPageOffsetY() {
        return 700;
    }

    @Override
    public String getString(@StringRes int resId, Object... args) {
        return context.getString(resId, args);
    }

    @Override
    public String getDateSeparator() {
        return dateSeparator;
    }

    @Override
    public Context getApplicationContext() {
        return context;
    }


}
