package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

public class PdfBoxImagesOnlyReport extends PdfBoxAbstractReport {

    public PdfBoxImagesOnlyReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager,
                                  @NonNull Flex flex) {
        super(context, persistenceManager, flex);
    }

    public PdfBoxImagesOnlyReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull UserPreferenceManager preferences,
                                  @NonNull StorageManager storageManager, @NonNull Flex flex) {
        super(context, db, preferences, storageManager, flex);
    }

    @Override
    public void createSections(@NonNull Trip trip, @NonNull PdfBoxReportFile pdfBoxReportFile) {
        final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts));
    }

    @Override
    protected String getFileName(Trip trip) {
        return trip.getDirectory().getName() + "Images.pdf";
    }
}
