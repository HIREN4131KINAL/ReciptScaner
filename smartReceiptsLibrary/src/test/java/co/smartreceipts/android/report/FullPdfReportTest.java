package co.smartreceipts.android.report;


import android.content.Context;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TripUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.flex.Flex;

@RunWith(RobolectricTestRunner.class)
public class FullPdfReportTest {

    private static final int NUM_RECEIPTS = 5;
    @Mock
    Context mContext;

    @Mock
    PersistenceManager mPersistenceManager;

    @Mock
    Flex mFlex;

    private Context context;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        context = RuntimeEnvironment.application;


    }


    @Test
    public void testPdfGeneration() throws Exception {

        Trip trip = TripUtils.newDefaultTrip();

        List<Receipt> receipts = new ArrayList<>();
        for (int i=0; i<NUM_RECEIPTS; i++) {
            Receipt receipt = createReceipt("Receipt " + (i+1), trip, null);
            receipts.add(receipt);
        }


        PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(context, "/");
        ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(new ReceiptNameColumn(1, "Name", new DefaultSyncState()));
        columns.add(new ReceiptNameColumn(2, "Price", new DefaultSyncState()));
        columns.add(new ReceiptNameColumn(3, "Date", new DefaultSyncState()));
        columns.add(new ReceiptNameColumn(4, "Category name", new DefaultSyncState()));
        columns.add(new ReceiptNameColumn(5, "Reimbursable", new DefaultSyncState()));
        columns.add(new ReceiptNameColumn(6, "Pictured", new DefaultSyncState()));

        pdfBoxReportFile.addSection(
                pdfBoxReportFile.createReceiptsTableSection(new ArrayList<Distance>(),
                        columns));

        OutputStream os = new FileOutputStream(new File("aaa3.pdf"));


        pdfBoxReportFile.writeFile(os, trip, receipts);


        os.close();

    }

    @NonNull
    private Receipt createReceipt(String name, Trip trip, File image) {
        ReceiptBuilderFactory f = ReceiptUtils.newDefaultReceiptBuilderFactory();
        f.setTrip(trip);
        f.setName(name);
        f.setImage(image);

        return f.build();
    }
}
