package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import junit.framework.Assert;

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
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
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
        mPaymentMethod1 = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD1).build()).toBlocking().first();
        mPaymentMethod2 = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD2).build()).toBlocking().first();
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

        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTable.TABLE_NAME));
        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTable.COLUMN_ID));
        assertTrue(mSqlCaptor.getValue().contains(PaymentMethodsTable.COLUMN_METHOD));
    }

    @Test
    public void onUpgradeFromV11() {
        final int oldVersion = 11;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPaymentMethodsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPaymentMethodDefaults(mPaymentMethodsTable);

        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PaymentMethodsTable.TABLE_NAME));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PaymentMethodsTable.COLUMN_ID));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PaymentMethodsTable.COLUMN_METHOD));
        assertEquals(mSqlCaptor.getAllValues().get(0), "CREATE TABLE paymentmethods (id INTEGER PRIMARY KEY AUTOINCREMENT, method TEXTremote_sync_id TEXT, marked_for_deletion TEXT, last_local_modification_type DATE);");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mPaymentMethodsTable.getTableName() + " ADD remote_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mPaymentMethodsTable.getTableName() + " ADD marked_for_deletion TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mPaymentMethodsTable.getTableName() + " ADD last_local_modification_type DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPaymentMethodsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPaymentMethodDefaults(mPaymentMethodsTable);

        assertEquals(mSqlCaptor.getAllValues().get(0), "ALTER TABLE " + mPaymentMethodsTable.getTableName() + " ADD remote_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mPaymentMethodsTable.getTableName() + " ADD marked_for_deletion TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mPaymentMethodsTable.getTableName() + " ADD last_local_modification_type DATE");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPaymentMethodsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPaymentMethodDefaults(mPaymentMethodsTable);
    }

    @Test
    public void get() {
        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get().toBlocking().first();
        assertEquals(paymentMethods, Arrays.asList(mPaymentMethod1, mPaymentMethod2));
    }

    @Test
    public void insert() {
        final PaymentMethod paymentMethod = mPaymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(METHOD3).build()).toBlocking().first();
        assertNotNull(paymentMethod);
        assertEquals(METHOD3, paymentMethod.getMethod());

        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get().toBlocking().first();
        assertEquals(paymentMethods, Arrays.asList(mPaymentMethod1, mPaymentMethod2, paymentMethod));
    }

    @Test
    public void findByPrimaryKey() {
        final PaymentMethod foundMethod = mPaymentMethodsTable.findByPrimaryKey(mPaymentMethod1.getId()).toBlocking().first();
        assertNotNull(foundMethod);
        assertEquals(mPaymentMethod1, foundMethod);
    }

    @Test
    public void findByPrimaryMissingKey() {
        final PaymentMethod foundMethod = mPaymentMethodsTable.findByPrimaryKey(-1).toBlocking().first();
        assertNull(foundMethod);
    }

    @Test
    public void update() {
        final PaymentMethod updatedPaymentMethod = mPaymentMethodsTable.update(mPaymentMethod1, new PaymentMethodBuilderFactory().setMethod(METHOD3).build()).toBlocking().first();
        assertNotNull(updatedPaymentMethod);
        assertEquals(METHOD3, updatedPaymentMethod.getMethod());
        assertFalse(mPaymentMethod1.equals(updatedPaymentMethod));

        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get().toBlocking().first();
        assertEquals(paymentMethods, Arrays.asList(updatedPaymentMethod, mPaymentMethod2));
    }

    @Test
    public void delete() {
        assertTrue(mPaymentMethodsTable.delete(mPaymentMethod1).toBlocking().first());

        final List<PaymentMethod> paymentMethods = mPaymentMethodsTable.get().toBlocking().first();
        assertEquals(paymentMethods, Collections.singletonList(mPaymentMethod2));
    }

}