package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.ReportGenerationException;
import co.smartreceipts.android.workers.reports.pdf.PdfReportFile;

public class PdfBoxReportFile implements PdfReportFile, PdfBoxSectionFactory {

    private final DefaultPdfBoxContext context;
    private final PDDocument doc;
    private List<PdfBoxSection> sections;

    public PdfBoxReportFile(Context androidContext, String dateSeparator) {
        sections = new ArrayList<>();
        context = new DefaultPdfBoxContext(androidContext, dateSeparator);
        doc = new PDDocument();
    }


    @Override
    public void writeFile(OutputStream outStream, Trip trip, List<Receipt> receipts) throws ReportGenerationException {
        try {
            for (PdfBoxSection section : sections) {
                section.writeSection(trip, receipts);
            }

            doc.save(outStream);
            doc.close();
        } catch (IOException e) {
            throw new ReportGenerationException(e);
        }

    }

    public void addSection(PdfBoxSection section) {
        sections.add(section);
    }

    @Override
    public PdfBoxReceiptsTablePdfSection createReceiptsTableSection(List<Distance> distances,
                                                                    List<Column<Receipt>> columns) {
        return new PdfBoxReceiptsTablePdfSection(context, doc, distances, columns);
    }

    @Override
    public PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection() {
        return new PdfBoxReceiptsImagesPdfSection(context, doc);
    }

    @Override
    public PdfBoxSignatureSection createSignatureSection(File signature) {
        return new PdfBoxSignatureSection(context, doc, signature);
    }
}