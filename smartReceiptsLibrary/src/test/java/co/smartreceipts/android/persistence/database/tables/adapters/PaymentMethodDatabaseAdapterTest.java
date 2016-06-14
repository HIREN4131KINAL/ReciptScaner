package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class PaymentMethodDatabaseAdapterTest {

    private static final int ID = 5;
    private static final int PRIMARY_KEY_ID = 11;
    private static final String METHOD = "abcd";

    // Class under test
    PaymentMethodDatabaseAdapter mPaymentMethodDatabaseAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    PaymentMethod mPaymentMethod;

    @Mock
    PrimaryKey<PaymentMethod, Integer> mPrimaryKey;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int methodIndex = 2;
        when(mCursor.getColumnIndex("id")).thenReturn(idIndex);
        when(mCursor.getColumnIndex("method")).thenReturn(methodIndex);
        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(methodIndex)).thenReturn(METHOD);

        when(mPaymentMethod.getId()).thenReturn(ID);
        when(mPaymentMethod.getMethod()).thenReturn(METHOD);

        when(mPrimaryKey.getPrimaryKeyValue(mPaymentMethod)).thenReturn(PRIMARY_KEY_ID);

        mPaymentMethodDatabaseAdapter = new PaymentMethodDatabaseAdapter();
    }

    @Test
    public void read() throws Exception {
        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory().setId(ID).setMethod(METHOD).build();
        assertEquals(paymentMethod, mPaymentMethodDatabaseAdapter.read(mCursor));
    }

    @Test
    public void write() throws Exception {
        final ContentValues contentValues = mPaymentMethodDatabaseAdapter.write(mPaymentMethod);
        assertEquals(METHOD, contentValues.getAsString("method"));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void build() throws Exception {
        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory().setId(PRIMARY_KEY_ID).setMethod(METHOD).build();
        assertEquals(paymentMethod, mPaymentMethodDatabaseAdapter.build(mPaymentMethod, mPrimaryKey));
    }
}