package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import com.itextpdf.text.Document;

import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

/**
 * Creates an images-only pdf report
 */
public final class ImagesOnlyPdfReport extends AbstractPdfImagesReport {

    public ImagesOnlyPdfReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex) {
        super(context, persistenceManager, flex);
    }

    public ImagesOnlyPdfReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull Preferences preferences, @NonNull StorageManager storageManager, Flex flex) {
        super(context, db, preferences, storageManager, flex);
    }

    @Override
    protected String getFileName(@NonNull Trip trip) {
        return trip.getDirectory().getName() + "Images.pdf";
    }

    @Override
    protected void generateInitialPages(@NonNull Document document, @NonNull List<Receipt> receipts, @NonNull Trip trip) throws ReportGenerationException {
        // Intentional no-op
    }
}
