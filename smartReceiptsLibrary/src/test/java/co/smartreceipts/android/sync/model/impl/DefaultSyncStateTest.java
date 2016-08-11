package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.impl.DefaultReceiptImpl;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class DefaultSyncStateTest {

    //Class under testing
    DefaultSyncState mDefaultSyncState;

    UniqueId mUniqueId;

    DeletionMarkings mDeletionInformation;

    Date mLastLocalModificationTime;

    @Before
    public void setUp() throws Exception {
        mUniqueId = new UniqueId("abc");
        mDeletionInformation = new DeletionMarkings(Collections.singletonMap(SyncProvider.GoogleDrive, true));
        mLastLocalModificationTime = new Date(System.currentTimeMillis());
        mDefaultSyncState = new DefaultSyncState(mUniqueId, mDeletionInformation, mLastLocalModificationTime);
    }

    @Test
    public void getSyncId() {
        assertEquals(mUniqueId, mDefaultSyncState.getSyncId(SyncProvider.None));
    }

    @Test
    public void isMarkedForDeletion() {
        assertEquals(true, mDefaultSyncState.isMarkedForDeletion(SyncProvider.GoogleDrive));
        assertEquals(false, mDefaultSyncState.isMarkedForDeletion(SyncProvider.None));
    }

    @Test
    public void getLastLocalModificationTime() {
        assertEquals(mLastLocalModificationTime, mDefaultSyncState.getLastLocalModificationTime());
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mDefaultSyncState.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final DefaultSyncState parceledSyncState = DefaultSyncState.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledSyncState);
        assertEquals(parceledSyncState, mDefaultSyncState);
    }

}