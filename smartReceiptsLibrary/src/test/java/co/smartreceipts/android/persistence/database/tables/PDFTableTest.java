package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPaymentMethodColumn;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PDFTableTest {

    // Class Under Test
    PDFTable mPDFTable;

    @Mock
    ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    SQLiteOpenHelper mSQLiteOpenHelper;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mPDFTable = new PDFTable(mSQLiteOpenHelper, mReceiptColumnDefinitions);

        when(mReceiptColumnDefinitions.getDefaultInsertColumn()).thenReturn(new BlankColumn<Receipt>(-1, ""));

        // Now create the table and insert some defaults
        mPDFTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mPDFTable.insertColumnNoCache("Name");
        mPDFTable.insertColumnNoCache("Date");
        mPDFTable.insertColumnNoCache("Price");
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mPDFTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals(PDFTableColumns.TABLE_NAME, mPDFTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPDFDefaults(mPDFTable);

        assertTrue(mSqlCaptor.getValue().contains(PDFTableColumns.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(PDFTableColumns.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(PDFTableColumns.COLUMN_TYPE));
    }

    @Test
    public void onUpgrade() {
        final int oldVersion = 9;
        final int newVersion = 14;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPDFDefaults(mPDFTable);

        assertTrue(mSqlCaptor.getValue().contains(PDFTableColumns.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(PDFTableColumns.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(PDFTableColumns.COLUMN_TYPE));
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = 10;
        final int newVersion = 14;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(mPDFTable);
    }

    @Test
    public void getPDF() {
        final List<Column<Receipt>> columns = mPDFTable.get();
        assertNotNull(columns);
        assertTrue(columns.size() > 0);
    }

    @Test
    public void insertPDF() {
        final List<Column<Receipt>> oldColumns = mPDFTable.get();
        final int oldSize = oldColumns.size();
        assertTrue(mPDFTable.insertDefaultColumn());
        final List<Column<Receipt>> newColumns = mPDFTable.get();
        assertEquals(oldSize + 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getId(), lastIdx + 1); //1-th indexed (not 0)
    }

    @Test
    public void insertPDFNoCache() {
        final List<Column<Receipt>> columns = mPDFTable.get();
        final String name = "CategoryCode";
        assertTrue(mPDFTable.insertColumnNoCache(name));
        final int lastIdx = columns.size() - 1;
        final Column<Receipt> lastCol = columns.get(lastIdx);
        assertNotSame(lastCol.getName(), name);
    }

    @Test
    public void insertPDFFirst() {
        assertTrue(mPDFTable.insertDefaultColumn());
        final List<Column<Receipt>> newColumns = mPDFTable.get();
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getId(), lastIdx + 1);
    }

    @Test
    public void updatePDF() {
        final List<Column<Receipt>> oldColumns = mPDFTable.get();
        final int lastIdx = oldColumns.size() - 1;
        final int oldSize = oldColumns.size();
        final Column<Receipt> oldColumn = oldColumns.get(lastIdx);
        final String newName = "Payment Method";
        final Column<Receipt> newColumn = new ReceiptPaymentMethodColumn(-1, newName);
        assertTrue(mPDFTable.update(oldColumn, newColumn));
        final List<Column<Receipt>> newColumns = mPDFTable.get();
        assertEquals(oldSize, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getName(), newName);
    }

    @Test
    public void deletePDF() {
        final List<Column<Receipt>> oldColumns = mPDFTable.get();
        final int oldSize = oldColumns.size();
        final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
        for (int i = 0; i < oldSize - 1; i++) {
            columnsList.add(oldColumns.get(i));
        }
        assertTrue(mPDFTable.deleteColumn());
        final List<Column<Receipt>> newColumns = mPDFTable.get();
        assertEquals(oldSize - 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        for (int i = 0; i < newColumns.size(); i++) {
            assertEquals(columnsList.get(i).getName(), newColumns.get(i).getName());
            assertEquals(columnsList.get(i).getId(), newColumns.get(i).getId());
        }
    }

}
