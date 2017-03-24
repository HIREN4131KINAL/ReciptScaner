package co.smartreceipts.android.workers.reports.pdf.tables;

import android.support.annotation.Nullable;

import java.io.File;

/**
 * A cell that holds an file. The cell is supposed to have a predetermined size and the file should
 * be adjusted to match its size (respecting the <code>mCellPadding</code>. The <code>mWidth</code>
 * of the cell must be set in the constructor, but the <code>mHeight</code> can be set later.
 */
public class FixedSizeImageCell implements FixedWidthCell {

    private final float mWidth;
    private final float mCellPadding;
    private final File file;
    private float mHeight;

    // TODO: Refactor to ensure that the image file is always @NonNull for this
    public FixedSizeImageCell(float width, float height, float cellPadding, @Nullable File image) {
        mWidth = width;
        mHeight = height;
        mCellPadding = cellPadding;
        file = image;
    }


    public float getWidth() {
        return mWidth;
    }

    @Override
    public float getHeight() {
        if (mHeight < 0) {
            throw new IllegalArgumentException("Height for " + FixedSizeImageCell.class.getName() + " has not been set");
        }
        return mHeight;
    }

    @Override
    public float getCellPadding() {
        return mCellPadding;
    }

    @Nullable
    public File getFile() {
        return file;
    }

    public void setHeight(float height) {
        this.mHeight = height;
    }
}
