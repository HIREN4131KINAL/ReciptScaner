package co.smartreceipts.android.persistence.database.tables;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPaymentMethodColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class PDFTableTest {

    private SmartReceiptsApplication mApp;
    private DatabaseHelper mDB;

    @Before
    public void setup() {
        mApp = (SmartReceiptsApplication) RuntimeEnvironment.application;
        mDB = mApp.getPersistenceManager().getDatabase();
    }

    @After
    public void tearDown() {
        mDB.close();
        mDB = null;
        mApp = null;
    }

    @Test
    public void getPDF() {
        final List<Column<Receipt>> columns = mDB.getPDFTable().getColumns();
        assertNotNull(columns);
        assertTrue(columns.size() > 0);
    }

    @Test
    public void insertPDF() {
        final List<Column<Receipt>> oldColumns = mDB.getPDFTable().getColumns();
        final int oldSize = oldColumns.size();
        assertTrue(mDB.getPDFTable().insertDefaultColumn());
        final List<Column<Receipt>> newColumns = mDB.getPDFTable().getColumns();
        assertEquals(oldSize + 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertTrue(lastCol instanceof BlankColumn);
        assertEquals(lastCol.getId(), lastIdx + 1); //1-th indexed (not 0)
    }

    @Test
    public void insertPDFNoCache() {
        final List<Column<Receipt>> columns = mDB.getPDFTable().getColumns();
        final String name = "CategoryCode";
        assertTrue(mDB.getPDFTable().insertColumnNoCache(name));
        final int lastIdx = columns.size() - 1;
        final Column<Receipt> lastCol = columns.get(lastIdx);
        assertNotSame(lastCol.getName(), name);
    }

    @Test
    public void insertPDFFirst() {
        assertTrue(mDB.getPDFTable().insertDefaultColumn());
        final List<Column<Receipt>> newColumns = mDB.getPDFTable().getColumns();
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertTrue(lastCol instanceof BlankColumn);
        assertEquals(lastCol.getId(), lastIdx + 1);
    }

    @Test
    public void updatePDF() {
        final List<Column<Receipt>> oldColumns = mDB.getPDFTable().getColumns();
        final int lastIdx = oldColumns.size() - 1;
        final int oldSize = oldColumns.size();
        final Column<Receipt> oldColumn = oldColumns.get(lastIdx);
        final String newName = "Payment Method";
        final Column<Receipt> newColumn = new ReceiptPaymentMethodColumn(-1, newName);
        assertTrue(mDB.getPDFTable().updateColumn(oldColumn, newColumn));
        final List<Column<Receipt>> newColumns = mDB.getPDFTable().getColumns();
        assertEquals(oldSize, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getName(), newName);
    }

    @Test
    public void deletePDF() {
        final List<Column<Receipt>> oldColumns = mDB.getPDFTable().getColumns();
        final int oldSize = oldColumns.size();
        final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
        for (int i = 0; i < oldSize - 1; i++) {
            columnsList.add(oldColumns.get(i));
        }
        assertTrue(mDB.getPDFTable().deleteColumn());
        final List<Column<Receipt>> newColumns = mDB.getPDFTable().getColumns();
        assertEquals(oldSize - 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        for (int i = 0; i < newColumns.size(); i++) {
            assertEquals(columnsList.get(i).getName(), newColumns.get(i).getName());
            assertEquals(columnsList.get(i).getId(), newColumns.get(i).getId());
        }
    }

}
