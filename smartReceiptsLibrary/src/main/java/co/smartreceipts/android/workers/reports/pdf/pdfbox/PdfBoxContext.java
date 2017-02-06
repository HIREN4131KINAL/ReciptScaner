package co.smartreceipts.android.workers.reports.pdf.pdfbox;


import android.content.Context;
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
    Context getAndroidContext();

    /**
     * A {@link PDRectangle} that represents the full page size, eg A4 etc.
     * @return
     */
    PDRectangle getPageSize();

    Preferences getPreferences();

    FontSpec getFont(String name);
    AWTColor getColor(String name);

    int getLineSpacing();
    int getPageMarginHorizontal();
    int getPageMarginVertical();

    String getString(@StringRes int resId, Object... args);

    void setPageSize(PDRectangle rectangle);


    class FontSpec {
        private final PDFont font;

        private final int size;

        public FontSpec(PDFont font, int size) {
            this.font = font;
            this.size = size;
        }

        public PDFont getFont() {
            return font;
        }
        public int getSize() {
            return size;
        }

    }
}
