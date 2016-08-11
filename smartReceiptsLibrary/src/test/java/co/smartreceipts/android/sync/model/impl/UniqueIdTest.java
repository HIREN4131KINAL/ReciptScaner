package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.math.BigDecimal;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.impl.DefaultReceiptImpl;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class UniqueIdTest {

    @Test
    public void getId() {
        final String id = "abcd";
        final UniqueId uniqueId = new UniqueId(id);
        assertEquals(id, uniqueId.getId());
    }

    @Test
    public void parcelEquality() {
        final String id = "abcd";
        final UniqueId uniqueId = new UniqueId(id);
        final Parcel parcel = Parcel.obtain();
        uniqueId.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final UniqueId parceledID = UniqueId.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledID);
        assertEquals(uniqueId, parceledID);
    }

}