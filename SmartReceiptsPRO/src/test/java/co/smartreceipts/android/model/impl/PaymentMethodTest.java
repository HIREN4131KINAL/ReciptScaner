package co.smartreceipts.android.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import co.smartreceipts.android.model.PaymentMethod;

@Config(emulateSdk = 18, manifest = "../SmartReceiptsPRO/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class PaymentMethodTest {

	private static final int ID = 101;
	private static final String METHOD = "method";
	
	private PaymentMethod paymentMethod;
	
	@Before
	public void setUp() throws Exception {
		final PaymentMethod.Builder builder = new PaymentMethod.Builder();
		builder.setId(ID);
		builder.setMethod(METHOD);
		paymentMethod = builder.build();
	}
	
	@Test
	public void getId() {
		assertEquals(ID, paymentMethod.getId());
	}
	
	@Test
	public void getMethod() {
		assertEquals(METHOD, paymentMethod.getMethod());
	}
	
	@Test
	public void hashCodeValidation() {
		final PaymentMethod.Builder builder = new PaymentMethod.Builder();
		builder.setId(ID);
		builder.setMethod(METHOD);
		final PaymentMethod samePaymentMethod = builder.build();
		final PaymentMethod wrongMethod1 = builder.setId(-1).build();
		final PaymentMethod wrongMethod2 = builder.setMethod("wrong").build();
		final PaymentMethod wrongMethod3 = builder.setId(ID).build();
		assertEquals(paymentMethod.hashCode(), samePaymentMethod.hashCode());
		assertNotSame(paymentMethod.hashCode(), wrongMethod1.hashCode());
		assertNotSame(paymentMethod.hashCode(), wrongMethod2.hashCode());
		assertNotSame(paymentMethod.hashCode(), wrongMethod3.hashCode());
	}
	
	@Test
	public void equalityValidation() {
		final PaymentMethod.Builder builder = new PaymentMethod.Builder();
		builder.setId(ID);
		builder.setMethod(METHOD);
		final PaymentMethod samePaymentMethod = builder.build();
		final PaymentMethod wrongMethod1 = builder.setId(-1).build();
		final PaymentMethod wrongMethod2 = builder.setMethod("wrong").build();
		final PaymentMethod wrongMethod3 = builder.setId(ID).build();
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
		final PaymentMethod parceledPaymentMethod = PaymentMethod.CREATOR.createFromParcel(parcel);
		assertNotNull(parceledPaymentMethod);
		assertEquals(paymentMethod, parceledPaymentMethod);
	}
}
