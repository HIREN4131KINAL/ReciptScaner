package co.smartreceipts.android.workers.reports.tables;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.File;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

public class FixedSizeImageCell implements FixedWidthCell {

    private final float width;
    private final float height;
    private final String text;
    private final PdfBoxContext.FontSpec fontSpec;
    private final AWTColor color;
    private float cellPadding;
    private File image;


    public FixedSizeImageCell(float width,
                              float height,
                              String text,
                              PdfBoxContext.FontSpec fontSpec,
                              AWTColor color,
                              float cellPadding,
                              File image) {
        this.width = width;
        this.height = height;
        this.text = text;
        this.fontSpec = fontSpec;
        this.color = color;
        this.cellPadding = cellPadding;
        this.image = image;
    }


    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getCellPadding() {
        return cellPadding;
    }

    public PdfBoxContext.FontSpec getFontSpec() {
        return fontSpec;
    }

    public AWTColor getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    public File getImage() {
        return image;
    }
}
