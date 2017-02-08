package co.smartreceipts.android.workers.reports.pdf.pdfbox;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import co.smartreceipts.android.persistence.Preferences;

public interface PdfBoxContext {

    /**
     * The android application {@link Context}.
     * Used for formatting dates and providing String resources through the
     * {@link #getString(int, Object...)} method.
     *
     * @return
     */
    @NonNull
    Context getAndroidContext();

    /**
     * A {@link PDRectangle} that represents the full page size, eg A4 etc.
     * @return
     */
    @NonNull
    PDRectangle getPageSize();

    @NonNull
    Preferences getPreferences();

    @Nullable
    FontSpec getFont(String name);
    @Nullable
    AWTColor getColor(String name);

    int getLineSpacing();
    int getPageMarginHorizontal();
    int getPageMarginVertical();

    @NonNull
    String getString(@StringRes int resId, Object... args);

    void setPageSize(@NonNull PDRectangle rectangle);


    class FontSpec {
        private final PDFont font;

        private final int size;

        public FontSpec(@NonNull PDFont font, int size) {
            this.font = font;
            this.size = size;
        }

        @NonNull
        public PDFont getFont() {
            return font;
        }

        public int getSize() {
            return size;
        }

    }
}
