package co.smartreceipts.android.workers.reports.pdf;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public interface PdfSection {
    void writeSection(PDDocument doc) throws IOException;
}
