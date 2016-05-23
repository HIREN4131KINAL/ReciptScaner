package co.smartreceipts.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.PaymentMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PaymentMethodsDBTest {

    private static final String METHOD = "method";
    private static final String METHOD2 = "method2";

    private SmartReceiptsApplication mApp;
    private DatabaseHelper mDB;

    @Before
    public void setup() {
        mApp = (SmartReceiptsApplication) RuntimeEnvironment.application;
        mDB = mApp.getPersistenceManager().getDatabase();
    }

    @After
    public void tearDown() {
        mDB.close();
        mDB = null;
        mApp = null;
    }

    @Test
    public void getMethodsList() {
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        assertNotNull(paymentMethods);
        assertTrue(paymentMethods.size() > 0);
    }

    @Test
    public void getMethodsCachedList() {
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        final List<PaymentMethod> cachedPaymentMethods = mDB.getPaymentMethods();
        assertEquals(cachedPaymentMethods, paymentMethods);
    }

    @Test
    public void insertPaymentMethod() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        assertNotNull(paymentMethod);
        assertEquals(METHOD, paymentMethod.getMethod());
    }

    @Test
    public void insertPaymentMethodThenGet() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        assertNotNull(paymentMethod);
        assertEquals(METHOD, paymentMethod.getMethod());
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        assertEquals(paymentMethod, paymentMethods.get(paymentMethods.size() - 1));
    }

    @Test
    public void getThenInsertPaymentMethod() {
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        final int initialSize = paymentMethods.size();
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        assertNotNull(paymentMethod);
        assertEquals(METHOD, paymentMethod.getMethod());
        assertEquals(initialSize + 1, paymentMethods.size());
        assertEquals(paymentMethod, paymentMethods.get(initialSize));
    }

    @Test
    public void findPaymentMethodById() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        final PaymentMethod foundMethod = mDB.findPaymentMethodById(paymentMethod.getId());
        assertNotNull(foundMethod);
        assertEquals(paymentMethod, foundMethod);
    }

    @Test
    public void findPaymentMethodByMissingId() {
        final int missingId = -1;
        final PaymentMethod foundMethod = mDB.findPaymentMethodById(missingId);
        assertEquals(foundMethod, null);
    }

    @Test
    public void updatePaymentMethod() {
        final PaymentMethod oldPaymentMethod = mDB.insertPaymentMethod(METHOD);
        final PaymentMethod updatedPaymentMethod = mDB.updatePaymentMethod(oldPaymentMethod, METHOD2);
        assertNotNull(updatedPaymentMethod);
        assertEquals(METHOD2, updatedPaymentMethod.getMethod());
        assertNotSame(oldPaymentMethod, updatedPaymentMethod);
    }

    @Test
    public void updatePaymentMethodThenGet() {
        final PaymentMethod oldPaymentMethod = mDB.insertPaymentMethod(METHOD);
        final PaymentMethod updatedPaymentMethod = mDB.updatePaymentMethod(oldPaymentMethod, METHOD2);
        assertNotNull(updatedPaymentMethod);
        assertEquals(METHOD2, updatedPaymentMethod.getMethod());
        assertNotSame(oldPaymentMethod, updatedPaymentMethod);
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        assertEquals(updatedPaymentMethod, paymentMethods.get(paymentMethods.size() - 1));
    }

    @Test
    public void getThenUpdatePaymentMethod() {
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        final int initialSize = paymentMethods.size();
        final PaymentMethod oldPaymentMethod = mDB.insertPaymentMethod(METHOD);
        final PaymentMethod updatedPaymentMethod = mDB.updatePaymentMethod(oldPaymentMethod, METHOD2);
        assertNotNull(updatedPaymentMethod);
        assertEquals(METHOD2, updatedPaymentMethod.getMethod());
        assertNotSame(oldPaymentMethod, updatedPaymentMethod);
        assertEquals(initialSize + 1, paymentMethods.size());
        assertEquals(updatedPaymentMethod, paymentMethods.get(initialSize));
    }

    @Test
    public void deletePaymentMethod() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        assertTrue(mDB.deletePaymenthMethod(paymentMethod));
    }

    @Test
    public void deletePaymentMethodThenGet() {
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        assertTrue(mDB.deletePaymenthMethod(paymentMethod));
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        assertFalse(paymentMethods.contains(paymentMethod));
    }

    @Test
    public void getThenDeletePaymentMethod() {
        final List<PaymentMethod> paymentMethods = mDB.getPaymentMethods();
        final PaymentMethod paymentMethod = mDB.insertPaymentMethod(METHOD);
        assertTrue(mDB.deletePaymenthMethod(paymentMethod));
        assertFalse(paymentMethods.contains(paymentMethod));
    }

}
