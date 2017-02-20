package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.Map;

import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontManager;

public class DefaultPdfBoxContext implements PdfBoxContext {

    /**
     * TODO
     * move all these to {@link PdfBoxContext} or to the appropriate section??
     */
    public static final String COLOR_DARK_BLUE = "DARK_BLUE";
    public static final String COLOR_HEADER = "HEADER";
    public static final String COLOR_CELL = "CELL";

    private final Context mContext;
    private final PdfFontManager fontManager;
    private final UserPreferenceManager mPreferences;

    private Map<String, AWTColor> mColors;
    private PDRectangle mPageSize = PDRectangle.A4;

    public DefaultPdfBoxContext(@NonNull Context context,
                                @NonNull PdfFontManager fontManager,
                                @NonNull UserPreferenceManager preferences) {
        mContext = context;
        this.fontManager = Preconditions.checkNotNull(fontManager);
        mPreferences = preferences;
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
    public UserPreferenceManager getPreferences() {
        return mPreferences;
    }

    @NonNull
    @Override
    public PdfFontManager getFontManager() {
        return fontManager;
    }


    public void setColors(Map<String, AWTColor> colors) {
        this.mColors = colors;
    }

}
