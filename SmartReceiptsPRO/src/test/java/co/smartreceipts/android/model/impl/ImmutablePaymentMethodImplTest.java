package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.utils.PaymentMethodUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ImmutablePaymentMethodImplTest {

    ImmutablePaymentMethodImpl paymentMethod;

    @Before
    public void setUp() throws Exception {
        paymentMethod = new ImmutablePaymentMethodImpl(PaymentMethodUtils.Constants.ID, PaymentMethodUtils.Constants.METHOD);
    }

    @Test
    public void testGetId() {
        assertEquals(PaymentMethodUtils.Constants.ID, paymentMethod.getId());
    }

    @Test
    public void testGetMethod() {
        assertEquals(PaymentMethodUtils.Constants.METHOD, paymentMethod.getMethod());
    }

    @Test
    public void hashCodeValidation() {
        final ImmutablePaymentMethodImpl samePaymentMethod = new ImmutablePaymentMethodImpl(PaymentMethodUtils.Constants.ID, PaymentMethodUtils.Constants.METHOD);
        final ImmutablePaymentMethodImpl wrongMethod1 = new ImmutablePaymentMethodImpl(-1, PaymentMethodUtils.Constants.METHOD);
        final ImmutablePaymentMethodImpl wrongMethod2 = new ImmutablePaymentMethodImpl(PaymentMethodUtils.Constants.ID, "wrong");
        final ImmutablePaymentMethodImpl wrongMethod3 = new ImmutablePaymentMethodImpl(-1, "wrong");
        assertEquals(paymentMethod.hashCode(), samePaymentMethod.hashCode());
        assertNotSame(paymentMethod.hashCode(), wrongMethod1.hashCode());
        assertNotSame(paymentMethod.hashCode(), wrongMethod2.hashCode());
        assertNotSame(paymentMethod.hashCode(), wrongMethod3.hashCode());
    }

    @Test
    public void equalityValidation() {
        final ImmutablePaymentMethodImpl samePaymentMethod = new ImmutablePaymentMethodImpl(PaymentMethodUtils.Constants.ID, PaymentMethodUtils.Constants.METHOD);
        final ImmutablePaymentMethodImpl wrongMethod1 = new ImmutablePaymentMethodImpl(-1, PaymentMethodUtils.Constants.METHOD);
        final ImmutablePaymentMethodImpl wrongMethod2 = new ImmutablePaymentMethodImpl(PaymentMethodUtils.Constants.ID, "wrong");
        final ImmutablePaymentMethodImpl wrongMethod3 = new ImmutablePaymentMethodImpl(-1, "wrong");
        assertEquals(paymentMethod, paymentMethod);
        assertEquals(paymentMethod, samePaymentMethod);
        assertNotSame(paymentMethod, null);
        assertNotSame(paymentMethod, new Object());
        assertNotSame(paymentMethod, wrongMethod1);
        assertNotSame(paymentMethod, wrongMethod2);
        assertNotSame(paymentMethod, wrongMethod3);
    }

    @Test
    public void parcelTest() {
        final Parcel parcel = Parcel.obtain();
        paymentMethod.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final ImmutablePaymentMethodImpl parceledPaymentMethod = ImmutablePaymentMethodImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledPaymentMethod);
        assertEquals(paymentMethod, parceledPaymentMethod);
    }
}
