package co.smartreceipts.android.workers.reports.pdf;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public interface PdfSection {
    void writeSection(@NonNull PDDocument doc) throws IOException;
}
