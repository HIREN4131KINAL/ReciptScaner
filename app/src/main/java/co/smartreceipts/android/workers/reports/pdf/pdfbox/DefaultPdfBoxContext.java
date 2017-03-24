package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorManager;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontManager;

public class DefaultPdfBoxContext implements PdfBoxContext {

    private final Context mContext;
    private final PdfFontManager fontManager;
    private final PdfColorManager colorManager;
    private final UserPreferenceManager mPreferences;

    private PDRectangle mPageSize = PDRectangle.A4;

    public DefaultPdfBoxContext(@NonNull Context context,
                                @NonNull PdfFontManager fontManager,
                                @NonNull PdfColorManager colorManager,
                                @NonNull UserPreferenceManager preferences) {
        mContext = context;
        this.fontManager = Preconditions.checkNotNull(fontManager);
        this.colorManager = Preconditions.checkNotNull(colorManager);
        mPreferences = preferences;
    }

    @Override
    public int getLineSpacing() {
        return 8;
    }

    @Override
    public float getPageMarginHorizontal() {
        return 32;
    }

    @Override
    public float getPageMarginVertical() {
        return 32;
    }

    @NonNull
    @Override
    public String getString(@StringRes int resId, Object... args) {
        return mContext.getString(resId, args);
    }

    @Override
    public void setPageSize(@NonNull PDRectangle rectangle) {
        mPageSize = Preconditions.checkNotNull(rectangle);
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

    @NonNull
    @Override
    public PdfColorManager getColorManager() {
        return colorManager;
    }

}
