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
import co.smartreceipts.android.persistence.database.tables.columns.CSVTableColumns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CSVTableTest {

    // Class Under Test
    CSVTable mCSVTable;

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
        mCSVTable = new CSVTable(mSQLiteOpenHelper, mReceiptColumnDefinitions);

        when(mReceiptColumnDefinitions.getDefaultInsertColumn()).thenReturn(new BlankColumn<Receipt>(-1, ""));

        // Now create the table and insert some defaults
        mCSVTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mCSVTable.insertColumnNoCache("Name");
        mCSVTable.insertColumnNoCache("Date");
        mCSVTable.insertColumnNoCache("Price");
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mCSVTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals(CSVTableColumns.TABLE_NAME, mCSVTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCSVTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertCSVDefaults(mCSVTable);

        assertTrue(mSqlCaptor.getValue().contains(CSVTableColumns.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(CSVTableColumns.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(CSVTableColumns.COLUMN_TYPE));
    }

    @Test
    public void onUpgrade() {
        final int oldVersion = 2;
        final int newVersion = 14;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCSVTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertCSVDefaults(mCSVTable);

        assertTrue(mSqlCaptor.getValue().contains(CSVTableColumns.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(CSVTableColumns.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(CSVTableColumns.COLUMN_TYPE));
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = 3;
        final int newVersion = 14;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCSVTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertCSVDefaults(mCSVTable);
    }

    @Test
    public void getCSV() {
        final List<Column<Receipt>> columns = mCSVTable.getColumns();
        assertNotNull(columns);
        assertTrue(columns.size() > 0);
    }

    @Test
    public void insertCSV() {
        final List<Column<Receipt>> oldColumns = mCSVTable.getColumns();
        final int oldSize = oldColumns.size();
        assertTrue(mCSVTable.insertDefaultColumn());
        final List<Column<Receipt>> newColumns = mCSVTable.getColumns();
        assertEquals(oldSize + 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getId(), lastIdx + 1); //1-th indexed (not 0)
    }

    @Test
    public void insertCSVNoCache() {
        final List<Column<Receipt>> columns = mCSVTable.getColumns();
        final String name = "CategoryCode";
        assertTrue(mCSVTable.insertColumnNoCache(name));
        final int lastIdx = columns.size() - 1;
        final Column<Receipt> lastCol = columns.get(lastIdx);
        assertNotSame(lastCol.getName(), name);
    }

    @Test
    public void insertCSVFirst() {
        assertTrue(mCSVTable.insertDefaultColumn());
        final List<Column<Receipt>> newColumns = mCSVTable.getColumns();
        final int lastIdx = newColumns.size() - 1;
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getId(), lastIdx + 1);
    }

    @Test
    public void updateCSV() {
        final List<Column<Receipt>> oldColumns = mCSVTable.getColumns();
        final int lastIdx = oldColumns.size() - 1;
        final int oldSize = oldColumns.size();
        final Column<Receipt> oldColumn = oldColumns.get(lastIdx);
        final String newName = "Payment Method";
        final Column<Receipt> newColumn = new ReceiptPaymentMethodColumn(-1, newName);
        assertTrue(mCSVTable.updateColumn(oldColumn, newColumn));
        final List<Column<Receipt>> newColumns = mCSVTable.getColumns();
        assertEquals(oldSize, newColumns.size());
        assertEquals(oldColumns, newColumns);
        final Column<Receipt> lastCol = newColumns.get(lastIdx);
        assertEquals(lastCol.getName(), newName);
    }

    @Test
    public void deleteCSV() {
        final List<Column<Receipt>> oldColumns = mCSVTable.getColumns();
        final int oldSize = oldColumns.size();
        final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
        for (int i = 0; i < oldSize - 1; i++) {
            columnsList.add(oldColumns.get(i));
        }
        assertTrue(mCSVTable.deleteColumn());
        final List<Column<Receipt>> newColumns = mCSVTable.getColumns();
        assertEquals(oldSize - 1, newColumns.size());
        assertEquals(oldColumns, newColumns);
        for (int i = 0; i < newColumns.size(); i++) {
            assertEquals(columnsList.get(i).getName(), newColumns.get(i).getName());
            assertEquals(columnsList.get(i).getId(), newColumns.get(i).getId());
        }
    }

}
