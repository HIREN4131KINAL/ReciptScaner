package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
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

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

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
        when(mPaymentMethod.getSyncState()).thenReturn(mSyncState);

        when(mPrimaryKey.getPrimaryKeyValue(mPaymentMethod)).thenReturn(PRIMARY_KEY_ID);

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mPaymentMethodDatabaseAdapter = new PaymentMethodDatabaseAdapter(mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory().setId(ID).setMethod(METHOD).setSyncState(mSyncState).build();
        assertEquals(paymentMethod, mPaymentMethodDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsycned() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mPaymentMethodDatabaseAdapter.write(mPaymentMethod, new DatabaseOperationMetadata());
        assertEquals(METHOD, contentValues.getAsString("method"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mPaymentMethodDatabaseAdapter.write(mPaymentMethod, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        assertEquals(METHOD, contentValues.getAsString("method"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void build() throws Exception {
        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory().setId(PRIMARY_KEY_ID).setMethod(METHOD).setSyncState(mGetSyncState).build();
        assertEquals(paymentMethod, mPaymentMethodDatabaseAdapter.build(mPaymentMethod, mPrimaryKey, mock(DatabaseOperationMetadata.class)));
    }
}