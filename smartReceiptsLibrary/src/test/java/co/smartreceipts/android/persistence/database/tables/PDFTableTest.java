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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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

    Column<Receipt> mColumn1;

    Column<Receipt> mColumn2;

    Column<Receipt> mDefaultColumn;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mPDFTable = new PDFTable(mSQLiteOpenHelper, mReceiptColumnDefinitions);
        mDefaultColumn = new BlankColumn<>(-1, "");

        when(mReceiptColumnDefinitions.getDefaultInsertColumn()).thenReturn(mDefaultColumn);
        when(mReceiptColumnDefinitions.getColumn(anyInt(), eq(""))).thenReturn(mDefaultColumn);

        // Now create the table and insert some defaults
        mPDFTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mColumn1 = mPDFTable.insert(new ReceiptNameColumn(-1, "Name")).toBlocking().first();
        mColumn2 = mPDFTable.insert(new ReceiptPriceColumn(-1, "Price")).toBlocking().first();
        assertNotNull(mColumn1);
        assertNotNull(mColumn2);
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mPDFTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("pdfcolumns", mPDFTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPDFDefaults(mPDFTable);

        assertTrue(mSqlCaptor.getValue().contains(PDFTable.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(PDFTable.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(PDFTable.COLUMN_TYPE));
        assertTrue(mSqlCaptor.getValue().contains(AbstractSqlTable.COLUMN_SYNC_ID));
        assertTrue(mSqlCaptor.getValue().contains(AbstractSqlTable.COLUMN_MARKED_FOR_DELETION));
        assertTrue(mSqlCaptor.getValue().contains(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME));
    }

    @Test
    public void onUpgradeFromV9() {
        final int oldVersion = 9;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPDFDefaults(mPDFTable);

        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PDFTable.TABLE_NAME));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PDFTable.COLUMN_ID));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PDFTable.COLUMN_TYPE));
        assertEquals(mSqlCaptor.getAllValues().get(0), "CREATE TABLE pdfcolumns (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT);");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mPDFTable.getTableName() + " ADD remote_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mPDFTable.getTableName() + " ADD marked_for_deletion TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mPDFTable.getTableName() + " ADD last_local_modification_type DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(mPDFTable);

        assertEquals(mSqlCaptor.getAllValues().get(0), "ALTER TABLE " + mPDFTable.getTableName() + " ADD remote_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mPDFTable.getTableName() + " ADD marked_for_deletion TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mPDFTable.getTableName() + " ADD last_local_modification_type DATE");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(mPDFTable);
    }

    @Test
    public void get() {
        final List<Column<Receipt>> columns = mPDFTable.get().toBlocking().first();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2));
    }

    @Test
    public void findByPrimaryKey() {
        final Column<Receipt> foundColumn = mPDFTable.findByPrimaryKey(mColumn1.getId()).toBlocking().first();
        assertNotNull(foundColumn);
        assertEquals(mColumn1, foundColumn);
    }

    @Test
    public void findByPrimaryMissingKey() {
        final Column<Receipt> foundColumn = mPDFTable.findByPrimaryKey(-1).toBlocking().first();
        assertNull(foundColumn);
    }

    @Test
    public void insert() {
        final String name = "Code";
        final Column<Receipt> column = mPDFTable.insert(new ReceiptCategoryNameColumn(-1, name)).toBlocking().first();
        assertNotNull(column);
        assertEquals(name, column.getName());

        final List<Column<Receipt>> columns = mPDFTable.get().toBlocking().first();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2, column));
    }

    @Test
    public void insertDefaultColumn() throws Exception {
        final Column<Receipt> column = mPDFTable.insertDefaultColumn().toBlocking().first();
        assertNotNull(column);
        assertEquals(column, mDefaultColumn);

        final List<Column<Receipt>> columns = mPDFTable.get().toBlocking().first();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2, column));
    }

    @Test
    public void update() {
        final String name = "Code";
        final Column<Receipt> column = mPDFTable.update(mColumn1, new ReceiptCategoryNameColumn(-1, name)).toBlocking().first();
        assertNotNull(column);
        assertEquals(name, column.getName());

        final List<Column<Receipt>> columns = mPDFTable.get().toBlocking().first();
        assertEquals(columns, Arrays.asList(column, mColumn2));
    }

    @Test
    public void delete() {
        assertTrue(mPDFTable.delete(mColumn1).toBlocking().first());
        assertEquals(mPDFTable.get().toBlocking().first(), Collections.singletonList(mColumn2));
    }

    @Test
    public void deleteLast() {
        assertTrue(mPDFTable.deleteLast().toBlocking().first());
        assertEquals(mPDFTable.get().toBlocking().first(), Collections.singletonList(mColumn1));
        assertTrue(mPDFTable.deleteLast().toBlocking().first());
        assertEquals(mPDFTable.get().toBlocking().first(), Collections.emptyList());
        assertFalse(mPDFTable.deleteLast().toBlocking().first());
    }

}
