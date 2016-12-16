package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.Map;

public class DefaultPdfBoxContext implements PdfBoxContext {


    private Context context;
    private String dateSeparator;
    private Map<String, AWTColor> colors;
    private Map<String, FontSpec> fonts;

    public DefaultPdfBoxContext(Context context, String dateSeparator) {
        this.context = context;
        this.dateSeparator = dateSeparator;
    }



    @Override
    public FontSpec getFont(String name) {
        return fonts.get(name);
    }

    @Override
    public AWTColor getColor(String name) {
        return colors.get(name);
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


    public void setColors(Map<String, AWTColor> colors) {
        this.colors = colors;
    }

    public void setFonts(Map<String, FontSpec> fonts) {
        this.fonts = fonts;
    }
}
