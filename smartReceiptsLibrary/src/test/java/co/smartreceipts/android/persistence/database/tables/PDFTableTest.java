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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPaymentMethodColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
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
        mColumn1 = mPDFTable.insert(new ReceiptNameColumn(-1, "Name"));
        mColumn2 = mPDFTable.insert(new ReceiptPriceColumn(-1, "Price"));
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
    public void get() {
        final List<Column<Receipt>> columns = mPDFTable.get();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2));
    }

    @Test
    public void insert() {
        final String name = "Code";
        final Column<Receipt> column = mPDFTable.insert(new ReceiptCategoryNameColumn(-1, name));
        assertNotNull(column);
        assertEquals(name, column.getName());

        final List<Column<Receipt>> columns = mPDFTable.get();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2, column));
    }

    @Test
    public void insertDefaultColumn() {
        final Column<Receipt> column = mPDFTable.insertDefaultColumn();
        assertNotNull(column);
        assertEquals(column, mDefaultColumn);

        final List<Column<Receipt>> columns = mPDFTable.get();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2, column));
    }

    @Test
    public void update() {
        final String name = "Code";
        final Column<Receipt> column = mPDFTable.update(mColumn1, new ReceiptCategoryNameColumn(-1, name));
        assertNotNull(column);
        assertEquals(name, column.getName());

        final List<Column<Receipt>> columns = mPDFTable.get();
        assertEquals(columns, Arrays.asList(column, mColumn2));
    }

    @Test
    public void delete() {
        assertTrue(mPDFTable.delete(mColumn1));
        assertEquals(mPDFTable.get(), Collections.singletonList(mColumn2));
    }

    @Test
    public void deleteLast() {
        assertTrue(mPDFTable.deleteLast());
        assertEquals(mPDFTable.get(), Collections.singletonList(mColumn1));
    }

}
