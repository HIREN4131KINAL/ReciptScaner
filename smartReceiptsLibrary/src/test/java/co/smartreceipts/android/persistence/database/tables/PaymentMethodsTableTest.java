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
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.columns.PaymentMethodsTableColumns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class PaymentMethodsTableTest {

    private static final String METHOD1 = "name1";
    private static final String METHOD2 = "name2";
    private static final String METHOD3 = "name3";

    // Class under test
    PaymentMethodsTable mPaymentMethodsTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    SQLiteOpenHelper mSQLiteOpenHelper;

    PaymentMethod mPaymentMethod1;

    PaymentMethod mPaymentMethod2;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mPaymentMethodsTable = new PaymentMethodsTable(mSQLiteOpenHelper);

        // Now create the table and insert some defaults
        mPaymentMethodsTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mPaymentMethod1 = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD1).build());
        mPaymentMethod2 = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD2).build());
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mPaymentMethodsTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("paymentmethods", mPaymentMethodsTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPaymentMethodsTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPaymentMethodDefaults(mPaymentMethodsTable);

        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTableColumns.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTableColumns.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTableColumns.COLUMN_METHOD));
    }

    @Test
    public void onUpgrade() {
        final int oldVersion = 10;
        final int newVersion = 14;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPaymentMethodsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPaymentMethodDefaults(mPaymentMethodsTable);

        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTableColumns.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTableColumns.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTableColumns.COLUMN_METHOD));
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = 12;
        final int newVersion = 14;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPaymentMethodsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPaymentMethodDefaults(mPaymentMethodsTable);
    }

    @Test
    public void get() {
        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get();
        assertEquals(paymentMethods, Arrays.asList(mPaymentMethod1, mPaymentMethod2));
    }

    @Test
    public void insert() {
        final PaymentMethod paymentMethod = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD3).build());
        assertNotNull(paymentMethod);
        assertEquals(METHOD3, paymentMethod.getMethod());

        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get();
        assertEquals(paymentMethods, Arrays.asList(mPaymentMethod1, mPaymentMethod2, paymentMethod));
    }

    @Test
    public void findPaymentMethodById() {
        final PaymentMethod paymentMethod = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD3).build());
        assertNotNull(paymentMethod);

        final PaymentMethod foundMethod = mPaymentMethodsTable.findPaymentMethodById(paymentMethod.getId());
        assertNotNull(foundMethod);
        assertEquals(paymentMethod, foundMethod);
    }

    @Test
    public void findPaymentMethodByMissingId() {
        final int missingId = -1;
        final PaymentMethod foundMethod = mPaymentMethodsTable.findPaymentMethodById(missingId);
        assertNull(foundMethod);
    }

    @Test
    public void update() {
        final PaymentMethod updatedPaymentMethod = mPaymentMethodsTable.update(mPaymentMethod1, new PaymentMethodBuilderFactory().setMethod(METHOD3).build());
        assertNotNull(updatedPaymentMethod);
        assertEquals(METHOD3, updatedPaymentMethod.getMethod());
        assertFalse(mPaymentMethod1.equals(updatedPaymentMethod));

        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get();
        assertEquals(paymentMethods, Arrays.asList(updatedPaymentMethod, mPaymentMethod2));
    }

    @Test
    public void delete() {
        assertTrue(mPaymentMethodsTable.delete(mPaymentMethod1));

        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get();
        assertEquals(paymentMethods, Collections.singletonList(mPaymentMethod2));
    }

}