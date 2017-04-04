package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.DefaultObjects;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.sync.model.SyncState;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ImmutablePaymentMethodImplTest {

    private static final int ID = 5;
    private static final String METHOD = "method";

    // Class under test
    ImmutablePaymentMethodImpl mPaymentMethod;

    SyncState mSyncState;

    @Before
    public void setUp() throws Exception {
        mSyncState = DefaultObjects.newDefaultSyncState();
        mPaymentMethod = new ImmutablePaymentMethodImpl(ID, METHOD, mSyncState);
    }

    @Test
    public void getId() {
        assertEquals(ID, mPaymentMethod.getId());
    }

    @Test
    public void getMethod() {
        assertEquals(METHOD, mPaymentMethod.getMethod());
    }

    @Test
    public void getSyncState() {
        assertEquals(mSyncState, mPaymentMethod.getSyncState());
    }

    @Test
    public void equals() {
        assertEquals(mPaymentMethod, mPaymentMethod);
        assertEquals(mPaymentMethod, new ImmutablePaymentMethodImpl(ID, METHOD, mSyncState));
        assertThat(mPaymentMethod, not(equalTo(new Object())));
        assertThat(mPaymentMethod, not(equalTo(mock(PaymentMethod.class))));
        assertThat(mPaymentMethod, not(equalTo(new ImmutablePaymentMethodImpl(-1, METHOD, mSyncState))));
        assertThat(mPaymentMethod, not(equalTo(new ImmutablePaymentMethodImpl(ID, "abcd", mSyncState))));
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mPaymentMethod.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final ImmutablePaymentMethodImpl paymentMethod = ImmutablePaymentMethodImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(paymentMethod);
        assertEquals(paymentMethod, mPaymentMethod);
    }
}