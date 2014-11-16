package co.smartreceipts.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.sql.Date;
import java.util.List;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.testutils.ReceiptUtils;
import co.smartreceipts.android.testutils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ReceiptsDBTest {

    private SmartReceiptsApplication mApp;
    private DatabaseHelper mDB;
    private Trip mTrip, mTrip2;

    @Before
    public void setup() {
        mApp = (SmartReceiptsApplication) Robolectric.application;
        mDB = mApp.getPersistenceManager().getDatabase();
        mTrip = mDB.insertTripSerial(mApp.getPersistenceManager().getStorageManager().mkdir(TripUtils.Constants.DIRECTORY_NAME),
                TripUtils.Constants.START_DATE,
                TripUtils.Constants.END_DATE,
                TripUtils.Constants.COMMENT,
                TripUtils.Constants.COST_CENTER,
                TripUtils.Constants.CURRENCY_CODE);
        mTrip2 = mDB.insertTripSerial(mApp.getPersistenceManager().getStorageManager().mkdir(TripUtils.Constants.DIRECTORY_NAME + "2"),
                new Date(TripUtils.Constants.START_DATE_MILLIS + 2000),
                new Date(TripUtils.Constants.END_DATE_MILLIS + 2000),
                TripUtils.Constants.COMMENT + "2",
                TripUtils.Constants.COST_CENTER,
                "EUR");
    }

    @After
    public void tearDown() {
        mDB.close();
        mDB = null;
        mApp = null;
    }

    private Receipt insertDefaultReceipt() {
        File img = new File(mTrip.getDirectory(), ReceiptUtils.Constants.IMAGE_FILE_NAME);
        mApp.getPersistenceManager().getStorageManager().createFile(img);
        return insertDefaultReceipt(img);
    }

    private Receipt insertDefaultReceipt(PaymentMethod method) {
        File img = new File(mTrip.getDirectory(), ReceiptUtils.Constants.IMAGE_FILE_NAME);
        mApp.getPersistenceManager().getStorageManager().createFile(img);
        return insertDefaultReceipt(img, method);
    }

    private Receipt insertDefaultReceipt(File file) {
        return mDB.insertReceiptSerial(mTrip,
                file,
                ReceiptUtils.Constants.NAME,
                ReceiptUtils.Constants.CATEGORY,
                ReceiptUtils.Constants.DATE,
                ReceiptUtils.Constants.COMMENT,
                ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE,
                ReceiptUtils.Constants.DECIMAL_FORMATTED_TAX,
                ReceiptUtils.Constants.IS_EXPENSABLE,
                ReceiptUtils.Constants.CURRENCY_CODE,
                ReceiptUtils.Constants.IS_FULLPAGE,
                null,
                ReceiptUtils.Constants.EXTRA1,
                ReceiptUtils.Constants.EXTRA2,
                ReceiptUtils.Constants.EXTRA3);
    }

    private Receipt insertDefaultReceipt(File file, PaymentMethod method) {
        return mDB.insertReceiptSerial(mTrip,
                file,
                ReceiptUtils.Constants.NAME,
                ReceiptUtils.Constants.CATEGORY,
                ReceiptUtils.Constants.DATE,
                ReceiptUtils.Constants.COMMENT,
                ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE,
                ReceiptUtils.Constants.DECIMAL_FORMATTED_TAX,
                ReceiptUtils.Constants.IS_EXPENSABLE,
                ReceiptUtils.Constants.CURRENCY_CODE,
                ReceiptUtils.Constants.IS_FULLPAGE,
                method,
                ReceiptUtils.Constants.EXTRA1,
                ReceiptUtils.Constants.EXTRA2,
                ReceiptUtils.Constants.EXTRA3);
    }

    @Test
    public void insertAndGetAll() {
        File img = new File(mTrip.getDirectory(), ReceiptUtils.Constants.IMAGE_FILE_NAME);
        Receipt insertReceipt = insertDefaultReceipt();
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(insertReceipt);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(insertReceipt, receipts.get(0));
        assertNotSame(img, insertReceipt.getFile());
        assertEquals(insertReceipt.getFileName(), insertReceipt.getIndex() + "_" + insertReceipt.getName() + ".jpg");
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(insertReceipt, receipts.get(0));
    }

    @Test
    public void insertMultipleAndGetAll() {
        Receipt insertReceipt1 = insertDefaultReceipt();
        Receipt insertReceipt2 = mDB.insertReceiptSerial(mTrip, insertReceipt1);
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(insertReceipt1);
        assertNotNull(insertReceipt2);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        assertEquals(insertReceipt1, receipts.get(1));
        assertEquals(insertReceipt2, receipts.get(0));
        ReceiptUtils.assertFieldEquality(insertReceipt1, receipts.get(1));
        ReceiptUtils.assertFieldEquality(insertReceipt2, receipts.get(0));
    }

    @Test
    public void insertAndGetById() {
        Receipt insertReceipt = insertDefaultReceipt();
        Receipt findReceipt = mDB.getReceiptByID(insertReceipt.getId());
        assertNotNull(insertReceipt);
        assertNotNull(findReceipt);
        assertEquals(insertReceipt, findReceipt);
        ReceiptUtils.assertFieldEquality(insertReceipt, findReceipt);
    }

    @Test
    public void insertNonNullPaymentMethod() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod("method");
        final Receipt insertReceipt = insertDefaultReceipt(paymentMethod);
        Receipt findReceipt = mDB.getReceiptByID(insertReceipt.getId());
        assertNotNull(insertReceipt);
        assertNotNull(findReceipt);
        assertEquals(insertReceipt, findReceipt);
        ReceiptUtils.assertFieldEquality(insertReceipt, findReceipt);
    }

    @Test
    public void insertNullPaymentMethod() {
        final PaymentMethod paymentMethod = null;
        final Receipt insertReceipt = insertDefaultReceipt(paymentMethod);
        Receipt findReceipt = mDB.getReceiptByID(insertReceipt.getId());
        assertNotNull(insertReceipt);
        assertNotNull(findReceipt);
        assertEquals(insertReceipt, findReceipt);
        ReceiptUtils.assertFieldEquality(insertReceipt, findReceipt);
    }

    @Test
    public void updateWithNullPaymentMethod() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod("method");
        Receipt insertReceipt = insertDefaultReceipt(paymentMethod);
        Receipt updateReceipt = mDB.updateReceiptSerial(insertReceipt,
                mTrip,
                ReceiptUtils.Constants.NAME + "x",
                ReceiptUtils.Constants.CATEGORY + "x",
                new Date(ReceiptUtils.Constants.DATE_MILLIS + 2000),
                ReceiptUtils.Constants.COMMENT + "x",
                "10.0",
                "2.00",
                !ReceiptUtils.Constants.IS_EXPENSABLE,
                "EUR",
                !ReceiptUtils.Constants.IS_FULLPAGE,
                null,
                null,
                "2",
                null);
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(updateReceipt);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(updateReceipt, insertReceipt);
        assertEquals(updateReceipt, receipts.get(0));
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));
    }

    @Test
    public void updateWithNonNullPaymentMethod() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod("method");
        final PaymentMethod nullPaymentMethod = null;
        Receipt insertReceipt = insertDefaultReceipt(nullPaymentMethod);
        Receipt updateReceipt = mDB.updateReceiptSerial(insertReceipt,
                mTrip,
                ReceiptUtils.Constants.NAME + "x",
                ReceiptUtils.Constants.CATEGORY + "x",
                new Date(ReceiptUtils.Constants.DATE_MILLIS + 2000),
                ReceiptUtils.Constants.COMMENT + "x",
                "10.0",
                "2.00",
                !ReceiptUtils.Constants.IS_EXPENSABLE,
                "EUR",
                !ReceiptUtils.Constants.IS_FULLPAGE,
                paymentMethod,
                null,
                "2",
                null);
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(updateReceipt);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(updateReceipt, insertReceipt);
        assertEquals(updateReceipt, receipts.get(0));
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));
    }

    @Test
    public void updateFile() {
        // Files
        File pdf = new File(mTrip.getDirectory(), ReceiptUtils.Constants.PDF_FILE_NAME);
        File img = new File(mTrip.getDirectory(), "2_" + ReceiptUtils.Constants.IMAGE_FILE_NAME);
        File bad = new File(mTrip.getDirectory(), "bad.ext");
        mApp.getPersistenceManager().getStorageManager().createFile(pdf);
        mApp.getPersistenceManager().getStorageManager().createFile(img);
        assertTrue(pdf.exists());
        assertTrue(img.exists());
        assertFalse(bad.exists());

        // PDF Update test
        Receipt insertReceipt = insertDefaultReceipt();
        Receipt updateReceipt = mDB.updateReceiptFile(insertReceipt, pdf);
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(updateReceipt);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(updateReceipt, insertReceipt);
        assertEquals(updateReceipt, receipts.get(0));
        assertTrue(updateReceipt.hasPDF());
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));

        // Image Update test
        updateReceipt = mDB.updateReceiptFile(insertReceipt, img);
        receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(updateReceipt);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(updateReceipt, insertReceipt);
        assertEquals(updateReceipt, receipts.get(0));
        assertTrue(updateReceipt.hasImage());
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));

        // Bad File Update test
        updateReceipt = mDB.updateReceiptFile(insertReceipt, bad);
        receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(updateReceipt);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(updateReceipt, insertReceipt);
        assertEquals(updateReceipt, receipts.get(0));
        assertFalse(updateReceipt.hasFile());
    }

    @Test
    public void copyImg() {
        Receipt imgReceipt = insertDefaultReceipt();
        assertTrue(mDB.copyReceiptSerial(imgReceipt, mTrip2));
        List<Receipt> receipts1 = mDB.getReceiptsSerial(mTrip);
        List<Receipt> receipts2 = mDB.getReceiptsSerial(mTrip2);
        assertNotNull(imgReceipt);
        assertNotNull(receipts1);
        assertNotNull(receipts2);
        assertEquals(receipts1.size(), 1);
        assertEquals(receipts2.size(), 1);
        assertTrue(imgReceipt.hasImage());
        assertTrue(receipts1.get(0).hasImage());
        assertTrue(receipts2.get(0).hasImage());
        assertNotSame(imgReceipt.getTrip(), receipts2.get(0).getTrip());
        assertEquals(imgReceipt, receipts1.get(0)); // receipts2.get(0) will have a different id
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(imgReceipt, receipts1.get(0));
        assertNotSame(imgReceipt.getTrip(), receipts2.get(0).getTrip());
        assertEquals(receipts2.get(0).getTrip(), mTrip2);
        // Since we've verified the trip/image stuff, change them so we can use field equals
        receipts2.get(0).setFile(imgReceipt.getFile());
        ReceiptUtils.assertFieldEquality(imgReceipt, receipts2.get(0));
    }

    @Test
    public void moveImg() {
        Receipt imgReceipt = insertDefaultReceipt();
        assertTrue(mDB.moveReceiptSerial(imgReceipt, mTrip, mTrip2));
        List<Receipt> receipts1 = mDB.getReceiptsSerial(mTrip);
        List<Receipt> receipts2 = mDB.getReceiptsSerial(mTrip2);
        assertNotNull(imgReceipt);
        assertNotNull(receipts1);
        assertNotNull(receipts2);
        assertEquals(receipts1.size(), 0);
        assertEquals(receipts2.size(), 1);
        assertFalse((new File(mTrip.getDirectory(), ReceiptUtils.Constants.IMAGE_FILE_NAME).exists()));
        assertFalse(imgReceipt.hasImage());
        assertTrue(receipts2.get(0).hasImage());
        assertNotSame(imgReceipt.getTrip(), receipts2.get(0).getTrip());
        assertEquals(receipts2.get(0).getTrip(), mTrip2);
        // Since we've verified the trip/image stuff, change them so we can use field equals
        receipts2.get(0).setFile(imgReceipt.getFile());
        ReceiptUtils.assertFieldEquality(imgReceipt, receipts2.get(0));
    }

    @Test
    public void delete() {
        Receipt imgReceipt = insertDefaultReceipt();
        assertTrue(mDB.deleteReceiptSerial(imgReceipt, mTrip));
        assertFalse(mDB.deleteReceiptSerial(imgReceipt, mTrip)); //Double delete should return false
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 0);
    }

    @Test
    public void parentMove() {
        insertDefaultReceipt();
        Trip newTrip = mDB.updateTripSerial(mTrip,
                mApp.getPersistenceManager().getStorageManager().mkdir(TripUtils.Constants.DIRECTORY_NAME + "_new"),
                mTrip.getStartDate(),
                mTrip.getEndDate(),
                mTrip.getComment(),
                mTrip.getCostCenter(),
                mTrip.getCurrencyCode());
        List<Receipt> receipts = mDB.getReceiptsSerial(newTrip);
        assertNotNull(newTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 1);
        assertEquals(receipts.get(0).getTrip(), newTrip);
        // TODO: Create a coupler class so single actions for db update plus storage update
    }

    @Test
    public void moveUp() {
        Receipt receipt1 = insertDefaultReceipt();
        Receipt receipt2 = insertDefaultReceipt();
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        ReceiptUtils.assertFieldEquality(receipt1, receipts.get(1));
        ReceiptUtils.assertFieldEquality(receipt2, receipts.get(0));
        assertTrue(mDB.moveReceiptUp(mTrip, receipt1));
        receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
        ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
        assertFalse(mDB.moveReceiptUp(mTrip, receipt1));
        receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
        ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
    }

    @Test
    public void moveDown() {
        Receipt receipt1 = insertDefaultReceipt();
        Receipt receipt2 = insertDefaultReceipt();
        List<Receipt> receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        ReceiptUtils.assertFieldEquality(receipt1, receipts.get(1));
        ReceiptUtils.assertFieldEquality(receipt2, receipts.get(0));
        assertTrue(mDB.moveReceiptUp(mTrip, receipt1));
        receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
        ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
        assertFalse(mDB.moveReceiptUp(mTrip, receipt1));
        receipts = mDB.getReceiptsSerial(mTrip);
        assertNotNull(receipts);
        assertEquals(receipts.size(), 2);
        ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
        ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
    }

}
