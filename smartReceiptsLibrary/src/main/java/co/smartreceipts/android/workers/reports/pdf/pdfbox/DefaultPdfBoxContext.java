package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.Map;

public class DefaultPdfBoxContext implements PdfBoxContext {

    /**
     * TODO
     * move all these to {@link PdfBoxContext} or to the appropriate section??
     */
    public static final String FONT_DEFAULT = "FONT_DEFAULT";
    public static final String FONT_TABLE_HEADER = "FONT_TABLE_HEADER";
    public static final String FONT_SMALL = "FONT_SMALL";
    public static final String FONT_TITLE = "FONT_TITLE";
    public static final String COLOR_DARK_BLUE = "DARK_BLUE";
    public static final String COLOR_HEADER = "HEADER";
    public static final String COLOR_CELL = "CELL";


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
        return 24;
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
