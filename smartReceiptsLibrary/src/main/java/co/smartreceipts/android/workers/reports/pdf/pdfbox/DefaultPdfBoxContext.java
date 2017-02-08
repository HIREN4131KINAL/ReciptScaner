package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.Map;

import co.smartreceipts.android.persistence.Preferences;

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


    private final Context mContext;
    private final Preferences mPreferences;

    private Map<String, AWTColor> mColors;
    private Map<String, FontSpec> mFonts;
    private PDRectangle mPageSize = PDRectangle.A4;

    public DefaultPdfBoxContext(@NonNull Context context,
                                @NonNull Preferences preferences) {
        mContext = context;
        mPreferences = preferences;
    }



    @Override
    public FontSpec getFont(String name) {
        return mFonts.get(name);
    }

    @Override
    public AWTColor getColor(String name) {
        return mColors.get(name);
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
        return mContext.getString(resId, args);
    }

    @Override
    public void setPageSize(PDRectangle rectangle) {
        mPageSize = rectangle;
    }

    @NonNull
    @Override
    public Context getAndroidContext() {
        return mContext;
    }

    @NonNull
    @Override
    public PDRectangle getPageSize() {
        return mPageSize;
    }

    @NonNull
    @Override
    public Preferences getPreferences() {
        return mPreferences;
    }


    public void setColors(Map<String, AWTColor> colors) {
        this.mColors = colors;
    }

    public void setFonts(Map<String, FontSpec> fonts) {
        this.mFonts = fonts;
    }
}
