package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

public class PdfBoxImagesOnlyReport extends PdfBoxAbstractReport {
    public PdfBoxImagesOnlyReport(Context context, PersistenceManager persistenceManager, Flex flex) {
        super(context, persistenceManager, flex);
    }

    public PdfBoxImagesOnlyReport(Context context, DatabaseHelper db, Preferences preferences, StorageManager storageManager, Flex flex) {
        super(context, db, preferences, storageManager, flex);
    }

    @Override
    public void createSections(@NonNull Trip trip, PdfBoxReportFile pdfBoxReportFile, List<Column<Receipt>> columns) {

        pdfBoxReportFile.addSection(
                pdfBoxReportFile.createReceiptsImagesSection());

    }
}
