package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * A cell that holds an mImage. The cell is supposed to have a predetermined size and the mImage should
 * be adjusted to match its size (respecting the <code>mCellPadding</code>. The <code>mWidth</code>
 * of the cell must be set in the constructor, but the <code>mHeight</code> can be set later.
 */
public class FixedSizeImageCell implements FixedWidthCell {

    private final float mWidth;
    private final float mCellPadding;
    private final File mImage;
    private float mHeight;



    public FixedSizeImageCell(float width,
                              float height,
                              float cellPadding,
                              File image) {
        mWidth = width;
        mHeight = height;
        mCellPadding = cellPadding;
        mImage = image;
    }


    public float getWidth() {
        return mWidth;
    }

    @Override
    public float getHeight() {
        if (mHeight < 0) {
            throw new IllegalArgumentException("Height for " + FixedSizeImageCell.class.getName() +
                    " has not been set");
        }
        return mHeight;
    }

    @Override
    public float getCellPadding() {
        return mCellPadding;
    }

    @NonNull
    public File getImage() {
        return mImage;
    }

    public void setHeight(float height) {
        this.mHeight = height;
    }
}
