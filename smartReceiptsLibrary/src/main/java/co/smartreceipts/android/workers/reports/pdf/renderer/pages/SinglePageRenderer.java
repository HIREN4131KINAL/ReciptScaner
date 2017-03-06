package co.smartreceipts.android.workers.reports.pdf.renderer.pages;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.IOException;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxWriter;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;

public class SinglePageRenderer extends Renderer {

    private final Renderer renderer;

    public SinglePageRenderer(@NonNull Renderer renderer) {
        this.renderer = Preconditions.checkNotNull(renderer);
    }

    @Override
    public void measure() throws IOException {
        renderer.measure();
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        writer.newPage();
        renderer.render(writer);
    }
}
