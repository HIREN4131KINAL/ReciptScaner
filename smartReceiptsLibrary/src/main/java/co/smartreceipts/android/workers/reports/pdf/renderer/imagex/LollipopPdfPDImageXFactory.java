package co.smartreceipts.android.workers.reports.pdf.renderer.imagex;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * Used to load pdf files via the native Android {@link PdfRenderer} stack
 */
public class LollipopPdfPDImageXFactory implements PdfPDImageXFactory {

    private final PDDocument pdDocument;
    private final File file;

    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer pdfRenderer;
    private int currentPage = -1;

    public LollipopPdfPDImageXFactory(@NonNull PDDocument pdDocument, @NonNull File file) {
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.file = Preconditions.checkNotNull(file);
    }

    @Override
    public void open() throws IOException, SecurityException {
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
    }

    @NonNull
    @Override
    public PDImageXObject get() throws IOException {
        Preconditions.checkNotNull(pdfRenderer, "The Pdf file must first be opened");
        Preconditions.checkNotNull(parcelFileDescriptor, "The Pdf file must first be opened");

        PdfRenderer.Page page = null;
        Bitmap bitmap = null;
        try {
            page = pdfRenderer.openPage(currentPage);
            final int height = page.getHeight();
            final int width = page.getWidth();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
            return LosslessFactory.createFromImage(pdDocument, bitmap);
        } finally {
            if (page != null) {
                page.close();
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    /**
     * Bumps us to use the next page
     *
     * @return {@code false} if we're on the last page. {@code true} otherwise
     */
    @Override
    public boolean nextPage() {
        return ++currentPage < pdfRenderer.getPageCount();
    }

    @Override
    public void close() throws IOException {
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        IOUtils.closeQuietly(parcelFileDescriptor);
    }
}
