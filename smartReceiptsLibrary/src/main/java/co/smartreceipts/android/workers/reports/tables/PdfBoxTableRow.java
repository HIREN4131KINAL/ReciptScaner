package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.util.awt.AWTColor;

public class PdfBoxTableRow {

    private final FixedWidthCell[] mCells;
    private final AWTColor mBackgroundColor;
    private final float mWidth;


    public PdfBoxTableRow(@NonNull FixedWidthCell[] cells, float width, @Nullable AWTColor backgroundColor) {
        mCells = Preconditions.checkNotNull(cells);
        mWidth = width;
        mBackgroundColor = backgroundColor;
    }

    @Nullable
    public AWTColor getBackgroundColor() {
        return mBackgroundColor;
    }


    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        float rowHeight = 0;
        for (int i = 0; i < mCells.length; i++) {
            if (mCells[i] != null) {
                rowHeight = Math.max(mCells[i].getHeight(), rowHeight);
            }
        }
        return rowHeight;
    }

    @NonNull
    public FixedWidthCell[] getCells() {
        return mCells;
    }
}
