package co.smartreceipts.android;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.impl.columns.distance.DistanceCommentColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceCurrencyColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceDateColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceDistanceColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceLocationColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistancePriceColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceRateColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptDateColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptIsPicturedColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptIsReimbursableColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TripUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;

/**
 * Base abstract test class that can be extended and run as a Unit test or an
 * Instrumentation test.
 * <p>
 * This class resides in the common test folder, so it doesn't has access to the junit
 * or mockito dependencies.
 */
public abstract class AbstractPdfBoxFullReportTest {
    protected static final int NUM_RECEIPTS = 8;
    protected static final int NUM_IMAGES = 5;
    protected static final String OUTPUT_FILE = "report.pdf";
    private static final int NUM_DISTANCES = 2;
    private static final int NUM_PDF = 0;

    protected Context mContext;

    protected PersistenceManager mPersistenceManager;

    protected UserPreferenceManager mPreferences;

    /**
     * Base method, to be overridden by subclasses. The subclass must annotate the method
     * with the JUnit <code>@Before</code> annotation, and initialize the mocks.
     */
    public void setup() {
        mContext = getContext();
    }


    /**
     * The actual test method. Can't be annotated with the JUnit <code>@Test</code>
     * annotation. Subclasses must override it, annotate it and simply call <code>super</code>.
     * The test method creates a series of receipts, and generates the pdf report file.
     *
     * @throws Exception
     */
    public void testPdfGeneration() throws Exception {

        Trip trip = TripUtils.newDefaultTrip();

        List<Receipt> receipts = new ArrayList<>();
        ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(mContext);


        for (int i = 0; i < NUM_RECEIPTS; i++) {
            File file = null;
            if (i < NUM_IMAGES) {
                file = getImageFile(String.valueOf(i + 1) + ".jpg");
            } else if (i < NUM_IMAGES + NUM_PDF) {
                file = getImageFile(String.valueOf(i - NUM_IMAGES + 1) + ".pdf");
            }


            factory.setIsFullPage(i == 1);
            factory.setIsReimbursable(i % 2 == 0);
            Receipt receipt = createReceipt(
                    factory,
                    i + 1,
                    trip,
                    getReceiptTitle(i),
                    "Comment " + (i + 1),
                    file);

            receipts.add(receipt);
        }


        PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(mContext, mPreferences, useBuiltinFonts());
        ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(new ReceiptNameColumn(1, "Name", new DefaultSyncState()));
        columns.add(new ReceiptPriceColumn(2, "Price", new DefaultSyncState()));
        columns.add(new ReceiptDateColumn(3, "Date", new DefaultSyncState(), mContext, mPreferences));
        columns.add(new ReceiptCategoryNameColumn(4, "Category name", new DefaultSyncState()));
        columns.add(new ReceiptIsReimbursableColumn(5, "Reimbursable", new DefaultSyncState(), mContext));
        columns.add(new ReceiptIsPicturedColumn(6, "Pictured", new DefaultSyncState(), mContext));

        List<Distance> distances = new ArrayList<>();
        DistanceBuilderFactory distanceFactory = new DistanceBuilderFactory();
        for (int i = 0; i < NUM_DISTANCES; i++) {
            distanceFactory.setTrip(trip);
            distanceFactory.setRate(20);
            distanceFactory.setDistance(10);
            distanceFactory.setCurrency(PriceCurrency.getInstance("USD"));
            distanceFactory.setLocation(i==0 ? "Loc 1" : "Location Location Location " + String.valueOf(i + 1));

            distances.add(distanceFactory.build());
        }

        List<Column<Distance>> distanceColumns = new ArrayList<>();
        distanceColumns.add(new DistanceLocationColumn(1, "Location", new DefaultSyncState(), mContext));
        distanceColumns.add(new DistancePriceColumn(2, "Price", new DefaultSyncState(), false));
        distanceColumns.add(new DistanceDistanceColumn(3, "Distance", new DefaultSyncState()));
        distanceColumns.add(new DistanceCurrencyColumn(4, "Currency", new DefaultSyncState()));
        distanceColumns.add(new DistanceRateColumn(5, "Rate", new DefaultSyncState()));
        distanceColumns.add(new DistanceDateColumn(6, "Date", new DefaultSyncState(), mContext, mPreferences));
        distanceColumns.add(new DistanceCommentColumn(7, "Comment", new DefaultSyncState()));


        pdfBoxReportFile.addSection(
                pdfBoxReportFile.createReceiptsTableSection(trip, receipts, columns, distances,
                        distanceColumns));

        pdfBoxReportFile.addSection(
                pdfBoxReportFile.createReceiptsImagesSection(trip, receipts));

        OutputStream os = new FileOutputStream(createOutputFile());

        pdfBoxReportFile.writeFile(os, trip);

        os.close();
    }

    protected abstract boolean useBuiltinFonts();

    private String getReceiptTitle(int i) {
        if (i == 2) {
            return "Receipt with a long long long " +
//                    "description "  + (i + 1);
                    "long long long long long description " + (i + 1);
        } else if (i == 4 && !useBuiltinFonts()) {
            return "Recibo en español con tildes: éó?¿¡" + (i + 1);
        } else if (i == 7 && !useBuiltinFonts()) {
            return "Απόδειξη ελληνική. Κεφαλαίο Όνομα" + (i + 1);
//        } else if (i == 8) {
//            return "Korean: ㅇㅋㅊ";
        } else {
            return "Receipt " + (i + 1);
        }
    }

    protected abstract Context getContext();

    /**
     * Method to be overridden by the subclasses, that should specify how the image files
     * should be read.
     * <p>
     * TODO currently the files are duplicated. Try to store them in one place (and maybe
     * copy them over to the target directories using gradle).
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    protected abstract File getImageFile(String fileName) throws IOException;


    /**
     * Method to be overridden by subclasses, that should specify where and how the
     * output file should be created.
     *
     * @return
     * @throws IOException
     */
    @NonNull
    protected abstract File createOutputFile() throws IOException;

    @NonNull
    Receipt createReceipt(ReceiptBuilderFactory f, int index, Trip trip, String name,
                          String comment, File image) {
        f.setTrip(trip);
        f.setIndex(index);
        f.setName(name);
        f.setComment(comment);
        f.setImage(image);

        return f.build();
    }
}
