package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

public abstract class PdfBoxAbstractReport extends AbstractReport {
    public PdfBoxAbstractReport(Context context, PersistenceManager persistenceManager, Flex flex) {
        super(context, persistenceManager, flex);
    }

    public PdfBoxAbstractReport(Context context, DatabaseHelper db, Preferences preferences, StorageManager storageManager, Flex flex) {
        super(context, db, preferences, storageManager, flex);
    }

    @NonNull
    @Override
    public File generate(@NonNull Trip trip) throws ReportGenerationException {
        final String outputFileName = getFileName(trip);
        FileOutputStream pdfStream = null;

        try {
            getStorageManager().delete(trip.getDirectory(), outputFileName);

            pdfStream = getStorageManager().getFOS(trip.getDirectory(), outputFileName);

            PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(getContext(), getPreferences());

            final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));
            final List<Column<Receipt>> columns = getDatabase().getPDFTable().get().toBlocking().first();

            createSections(trip, pdfBoxReportFile, columns);


            pdfBoxReportFile.writeFile(pdfStream, trip, receipts);

            return getStorageManager().getFile(trip.getDirectory(), outputFileName);

        } catch (IOException e) {
            Logger.error(this, e);
            throw new ReportGenerationException(e);
        } finally {
            if (pdfStream != null) {
                StorageManager.closeQuietly(pdfStream);
            }
        }

    }

    public abstract void createSections(@NonNull Trip trip, PdfBoxReportFile pdfBoxReportFile, List<Column<Receipt>> columns);

    private String getFileName(Trip trip) {
        return trip.getDirectory().getName() + ".pdf";
    }
}
