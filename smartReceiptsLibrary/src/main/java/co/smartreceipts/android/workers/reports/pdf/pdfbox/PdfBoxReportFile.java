package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
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
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.PdfReportFile;

public class PdfBoxReportFile implements PdfReportFile, PdfBoxSectionFactory {

    private final DefaultPdfBoxContext mContext;
    private final PDDocument mDocument;
    private final List<PdfBoxSection> mSections;


    public PdfBoxReportFile(@NonNull Context androidContext,
                            @NonNull UserPreferenceManager preferences) throws IOException {
        this(androidContext, preferences, false);
    }


    /**
     * @param androidContext
     * @param preferences
     * @param useBuiltinFonts Ugly parameter so that in the tests we can avoid loading the custom fonts
     * @throws IOException
     */
    @VisibleForTesting
    public PdfBoxReportFile(@NonNull Context androidContext, @NonNull UserPreferenceManager preferences,
                            boolean useBuiltinFonts) throws IOException {
        mDocument = new PDDocument();
        mSections = new ArrayList<>();
        Map<String, AWTColor> colors = new HashMap<>();
        colors.put(DefaultPdfBoxContext.COLOR_DARK_BLUE, new AWTColor(0, 122, 255));
        colors.put(DefaultPdfBoxContext.COLOR_HEADER, new AWTColor(204, 228, 255));
        colors.put(DefaultPdfBoxContext.COLOR_CELL, new AWTColor(239, 239, 244));

        PDFont MAIN_FONT = useBuiltinFonts ? PDType1Font.HELVETICA : PDType0Font.load(mDocument, androidContext.getAssets().open("NotoSerif-Regular.ttf"));
        PDFont BOLD_FONT = useBuiltinFonts ? PDType1Font.HELVETICA_BOLD : PDType0Font.load(mDocument, androidContext.getAssets().open("NotoSerif-Bold.ttf"));
        int DEFAULT_SIZE = 12;
        int TITLE_SIZE = 14;
        int SMALL_SIZE = 10;

        Map<String, PdfBoxContext.FontSpec> fonts = new HashMap<>();
        fonts.put(DefaultPdfBoxContext.FONT_DEFAULT, new PdfBoxContext.FontSpec(MAIN_FONT, DEFAULT_SIZE));
        fonts.put(DefaultPdfBoxContext.FONT_TITLE, new PdfBoxContext.FontSpec(BOLD_FONT, TITLE_SIZE));
        fonts.put(DefaultPdfBoxContext.FONT_SMALL, new PdfBoxContext.FontSpec(MAIN_FONT, SMALL_SIZE));
        fonts.put(DefaultPdfBoxContext.FONT_TABLE_HEADER, new PdfBoxContext.FontSpec(BOLD_FONT, SMALL_SIZE));


        mContext = new DefaultPdfBoxContext(androidContext, preferences);
        mContext.setColors(colors);
        mContext.setFonts(fonts);
    }


    @Override
    public void writeFile(@NonNull OutputStream outStream, @NonNull Trip trip) throws IOException {
        try {
            for (PdfBoxSection section : mSections) {
                section.writeSection(mDocument);
            }

            mDocument.save(outStream);
        } finally {
            try {
                mDocument.close();
            } catch (IOException e) {
                Logger.error(this, e);
            }
        }
    }

    public void addSection(PdfBoxSection section) {
        mSections.add(section);
    }

    @NonNull
    @Override
    public PdfBoxReceiptsTablePdfSection createReceiptsTableSection(
            @NonNull Trip trip, @NonNull List<Receipt> receipts, @NonNull List<Column<Receipt>> columns,
            @NonNull List<Distance> distances, @NonNull List<Column<Distance>> distanceColumns) {
        return new PdfBoxReceiptsTablePdfSection(mContext, trip, receipts, columns, distances, distanceColumns);
    }

    @NonNull
    @Override
    public PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection(@NonNull Trip trip, @NonNull List<Receipt> receipts) {
        return new PdfBoxReceiptsImagesPdfSection(mContext, trip, receipts);
    }

    @NonNull
    @Override
    public PdfBoxSignatureSection createSignatureSection(@NonNull Trip trip, @NonNull File signature) {
        return new PdfBoxSignatureSection(mContext, trip, signature);
    }

}