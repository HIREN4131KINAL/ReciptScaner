package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
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
    public FontSpec getDefaultFont() {
        return new FontSpec(PDType1Font.HELVETICA, 14);
    }


    @Override
    public FontSpec getTitleFont() {
        return new FontSpec(PDType1Font.HELVETICA_BOLD, 18);
    }

    @Override
    public FontSpec getSmallFont() {
        return new FontSpec(PDType1Font.HELVETICA, 12);
    }


    @Override
    public int getLineSpacing() {
        return 8;
    }

    @Override
    public int getPageMarginHorizontal() {
        return 36;
    }

    @Override
    public int getPageMarginVertical() {
        return 36;
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

    @Override
    public PDRectangle getPageSize() {
        return PDRectangle.A4;
    }




}
