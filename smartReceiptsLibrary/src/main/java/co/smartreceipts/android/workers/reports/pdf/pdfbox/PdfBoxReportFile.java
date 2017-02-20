package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

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
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.PdfReportFile;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorManager;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontManager;

public class PdfBoxReportFile implements PdfReportFile, PdfBoxSectionFactory {

    private final DefaultPdfBoxContext mContext;
    private final PDDocument mDocument;
    private final List<PdfBoxSection> mSections;


    public PdfBoxReportFile(@NonNull Context androidContext, @NonNull UserPreferenceManager preferences) throws IOException {
        mDocument = new PDDocument();
        mSections = new ArrayList<>();

        final PdfColorManager colorManager = new PdfColorManager();
        final PdfFontManager fontManager = new PdfFontManager(androidContext, mDocument);
        fontManager.initialize();

        mContext = new DefaultPdfBoxContext(androidContext, fontManager, colorManager, preferences);
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