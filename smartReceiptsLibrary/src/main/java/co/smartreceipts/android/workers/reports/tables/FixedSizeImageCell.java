package co.smartreceipts.android.workers.reports.tables;

import java.io.File;

/**
 * A cell that holds an image. The cell is supposed to have a predetermined size and the image should
 * be adjusted to match its size (respecting the <code>cellPadding</code>. The <code>width</code>
 * of the cell must be set in the constructor, but the <code>height</code> can be set later.
 */
public class FixedSizeImageCell implements FixedWidthCell {

    private final float width;
    private float height = -1.0f;
    private float cellPadding;
    private File image;


    public FixedSizeImageCell(float width, File image, float cellPadding) {
        this.width = width;
        this.image = image;
        this.cellPadding = cellPadding;
    }

    public FixedSizeImageCell(float width,
                              float height,
                              float cellPadding,
                              File image) {
        this.width = width;
        this.height = height;
        this.cellPadding = cellPadding;
        this.image = image;
    }


    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        if (height < 0) {
            throw new IllegalArgumentException("Height for " + FixedSizeImageCell.class.getName() +
                    " has not been set");
        }
        return height;
    }

    @Override
    public float getCellPadding() {
        return cellPadding;
    }

    public File getImage() {
        return image;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
