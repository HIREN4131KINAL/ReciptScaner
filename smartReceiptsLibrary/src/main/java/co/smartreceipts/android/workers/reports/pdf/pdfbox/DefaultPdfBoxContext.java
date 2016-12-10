package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

public class DefaultPdfBoxContext implements PdfBoxContext {

    private Context context;

    public DefaultPdfBoxContext(Context context) {
        this.context = context;
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
    public String getString(@StringRes int resId, Object... args) {
        return context.getString(resId, args);
    }
}
