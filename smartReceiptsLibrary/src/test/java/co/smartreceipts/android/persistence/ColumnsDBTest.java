package co.smartreceipts.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryCodeColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPaymentMethodColumn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ColumnsDBTest {

    private SmartReceiptsApplication mApp;
    private DatabaseHelper mDB;

    @Before
    public void setup() {
        mApp = (SmartReceiptsApplication) Robolectric.application;
        mDB = mApp.getPersistenceManager().getDatabase();
    }

    @After
    public void tearDown() {
        mDB.close();
        mDB = null;
        mApp = null;
    }

    @Test
    public void getCSV() {
        final List<Column<Receipt>> columns = mDB.getCSVColumns();
        assertNotNull(columns);
        assertTrue(columns.size() > 0);
    }

    @Test
    public void getPDF() {
        final List<Column<Receipt>> columns = mDB.getPDFColumns();
        assertNotNull(columns);
        assertTrue(columns.size() > 0);
    }

    @Test
    public void insertCSV() {
        final List<Column<Receipt>> oldColumns = mDB.getCSVColumns();
        final int oldSize = oldColumns.size();
        assertTrue(mDB.insertCSVColumn());
        final List<Column<Receipt>> newColumns = mDB.getCSVColumns();
        assertEquals(oldSize + 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertTrue(lastCol instanceof BlankColumn);
        assertEquals(lastCol.getId(), lastIdx + 1); //1-th indexed (not 0)
    }

    @Test
    public void insertPDF() {
        final List<Column<Receipt>> oldColumns = mDB.getPDFColumns();
        final int oldSize = oldColumns.size();
        assertTrue(mDB.insertPDFColumn());
        final List<Column<Receipt>> newColumns = mDB.getPDFColumns();
        assertEquals(oldSize + 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertTrue(lastCol instanceof BlankColumn);
        assertEquals(lastCol.getId(), lastIdx + 1); //1-th indexed (not 0)
    }

    @Test
    public void insertCSVNoCache() {
        final List<Column<Receipt>> columns = mDB.getCSVColumns();
        final String name = "CategoryCode";
        assertTrue(mDB.insertCSVColumnNoCache(name));
        final int lastIdx = columns.size() - 1;
        final Column<Receipt> lastCol = columns.get(lastIdx);
        assertNotSame(lastCol.getName(), name);
    }

    @Test
    public void insertPDFNoCache() {
        final List<Column<Receipt>> columns = mDB.getPDFColumns();
        final String name = "CategoryCode";
        assertTrue(mDB.insertPDFColumnNoCache(name));
        final int lastIdx = columns.size() - 1;
        final Column<Receipt> lastCol = columns.get(lastIdx);
        assertNotSame(lastCol.getName(), name);
    }

    @Test
    public void insertCSVFirst() {
        assertTrue(mDB.insertCSVColumn());
        final List<Column<Receipt>> newColumns = mDB.getCSVColumns();
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertTrue(lastCol instanceof BlankColumn);
        assertEquals(lastCol.getId(), lastIdx + 1);
    }

    @Test
    public void insertPDFFirst() {
        assertTrue(mDB.insertPDFColumn());
        final List<Column<Receipt>> newColumns = mDB.getPDFColumns();
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertTrue(lastCol instanceof BlankColumn);
        assertEquals(lastCol.getId(), lastIdx + 1);
    }

    @Test
    public void updateCSV() {
        final List<Column<Receipt>> oldColumns = mDB.getCSVColumns();
        final int lastIdx = oldColumns.size() - 1;
        final int oldSize = oldColumns.size();
        final Column<Receipt> oldColumn = oldColumns.get(lastIdx);
        final String newName = "Payment Method";
        final Column<Receipt> newColumn = new ReceiptPaymentMethodColumn(-1, newName);
        assertTrue(mDB.updateCSVColumn(oldColumn, newColumn));
        final List<Column<Receipt>> newColumns = mDB.getCSVColumns();
        assertEquals(oldSize, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getName(), newName);
    }

    @Test
    public void updatePDF() {
        final List<Column<Receipt>> oldColumns = mDB.getPDFColumns();
        final int lastIdx = oldColumns.size() - 1;
        final int oldSize = oldColumns.size();
        final Column<Receipt> oldColumn = oldColumns.get(lastIdx);
        final String newName = "Payment Method";
        final Column<Receipt> newColumn = new ReceiptPaymentMethodColumn(-1, newName);
        assertTrue(mDB.updatePDFColumn(oldColumn, newColumn));
        final List<Column<Receipt>> newColumns = mDB.getPDFColumns();
        assertEquals(oldSize, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getName(), newName);
    }

    @Test
    public void deleteCSV() {
        final List<Column<Receipt>> oldColumns = mDB.getCSVColumns();
        final int oldSize = oldColumns.size();
        final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
        for (int i = 0; i < oldSize - 1; i++) {
            columnsList.add(oldColumns.get(i));
        }
        assertTrue(mDB.deleteCSVColumn());
        final List<Column<Receipt>> newColumns = mDB.getCSVColumns();
        assertEquals(oldSize - 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        for (int i = 0; i < newColumns.size(); i++) {
            assertEquals(columnsList.get(i).getName(), newColumns.get(i).getName());
            assertEquals(columnsList.get(i).getId(), newColumns.get(i).getId());
        }
    }

    @Test
    public void deletePDF() {
        final List<Column<Receipt>> oldColumns = mDB.getPDFColumns();
        final int oldSize = oldColumns.size();
        final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
        for (int i = 0; i < oldSize - 1; i++) {
            columnsList.add(oldColumns.get(i));
        }
        assertTrue(mDB.deletePDFColumn());
        final List<Column<Receipt>> newColumns = mDB.getPDFColumns();
        assertEquals(oldSize - 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        for (int i = 0; i < newColumns.size(); i++) {
            assertEquals(columnsList.get(i).getName(), newColumns.get(i).getName());
            assertEquals(columnsList.get(i).getId(), newColumns.get(i).getId());
        }
    }

}
