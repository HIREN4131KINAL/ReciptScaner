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
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.PdfReportFile;

public class PdfBoxReportFile implements PdfReportFile, PdfBoxSectionFactory {

    private final DefaultPdfBoxContext context;
    private final PDDocument doc;
    private final Preferences preferences;
    private List<PdfBoxSection> sections;

    public PdfBoxReportFile(Context androidContext, Preferences preferences) {
        this.preferences = preferences;
        sections = new ArrayList<>();
        Map<String, AWTColor> colors = new HashMap<>();
        colors.put(DefaultPdfBoxContext.COLOR_DARK_BLUE, new AWTColor(0, 122, 255));
        colors.put(DefaultPdfBoxContext.COLOR_HEADER, new AWTColor(204, 228, 255));
        colors.put(DefaultPdfBoxContext.COLOR_CELL, new AWTColor(239, 239, 244));

        PDFont MAIN_FONT = PDType1Font.HELVETICA;
        PDFont BOLD_FONT = PDType1Font.HELVETICA_BOLD;
        int DEFAULT_SIZE = 12;
        int TITLE_SIZE = 14;
        int SMALL_SIZE = 10;

        Map<String,PdfBoxContext.FontSpec> fonts = new HashMap<>();
        fonts.put(DefaultPdfBoxContext.FONT_DEFAULT, new PdfBoxContext.FontSpec(MAIN_FONT, DEFAULT_SIZE));
        fonts.put(DefaultPdfBoxContext.FONT_TITLE, new PdfBoxContext.FontSpec(BOLD_FONT, TITLE_SIZE));
        fonts.put(DefaultPdfBoxContext.FONT_SMALL, new PdfBoxContext.FontSpec(MAIN_FONT, SMALL_SIZE));
        fonts.put(DefaultPdfBoxContext.FONT_TABLE_HEADER, new PdfBoxContext.FontSpec(BOLD_FONT, SMALL_SIZE));


        context = new DefaultPdfBoxContext(androidContext, preferences);
        context.setColors(colors);
        context.setFonts(fonts);
        doc = new PDDocument();
    }


    @Override
    public void writeFile(OutputStream outStream, Trip trip, List<Receipt> receipts) throws IOException {
        try {
            for (PdfBoxSection section : sections) {
                section.writeSection(trip, receipts);
            }

            doc.save(outStream);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                doc.close();
            } catch (IOException e) {
                Logger.error(this, e);
            }
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

    @Override
    public PdfBoxSection createDistancesTableSection(List<Distance> distances, List<Column<Distance>> columns) {
        return new PdfBoxDistancesTablePdfSection(context, doc, distances, columns);
    }
}