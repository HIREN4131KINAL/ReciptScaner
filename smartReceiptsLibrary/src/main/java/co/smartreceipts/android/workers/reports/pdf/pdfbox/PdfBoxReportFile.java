package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, AWTColor> colors = new HashMap<>();
        colors.put("DARK_BLUE", new AWTColor(0, 122, 255));
        colors.put("HEADER_BACKGROUND", new AWTColor(204, 228, 255));
        colors.put("CELL_BACKGROUND", new AWTColor(239, 239, 24));

        PDFont MAIN_FONT = PDType1Font.HELVETICA;
        PDFont BOLD_FONT = PDType1Font.HELVETICA_BOLD;
        int DEFAULT_SIZE = 12;
        int TITLE_SIZE = 14;
        int SMALL_SIZE = 10;

        Map<String,PdfBoxContext.FontSpec> fonts = new HashMap<>();
        fonts.put("DEFAULT", new PdfBoxContext.FontSpec(MAIN_FONT, DEFAULT_SIZE));
        fonts.put("TITLE", new PdfBoxContext.FontSpec(BOLD_FONT, TITLE_SIZE));
        fonts.put("SMALL", new PdfBoxContext.FontSpec(MAIN_FONT, SMALL_SIZE));
        fonts.put("TABLE_HEADER", new PdfBoxContext.FontSpec(BOLD_FONT, SMALL_SIZE));


        context = new DefaultPdfBoxContext(androidContext, dateSeparator);
        context.setColors(colors);
        context.setFonts(fonts);
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