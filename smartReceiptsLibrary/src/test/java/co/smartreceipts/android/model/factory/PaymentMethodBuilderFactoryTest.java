package co.smartreceipts.android.model.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.utils.PaymentMethodUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class PaymentMethodBuilderFactoryTest {

    private PaymentMethodBuilderFactory builderFactory;

    @Before
    public void setUp() throws Exception {
        builderFactory = new PaymentMethodBuilderFactory();
    }

    @Test
    public void testId() {
        builderFactory.setId(PaymentMethodUtils.Constants.ID);
        assertEquals(PaymentMethodUtils.Constants.ID, builderFactory.build().getId());
    }

    @Test
    public void testMethod() {
        builderFactory.setMethod(PaymentMethodUtils.Constants.METHOD);
        assertEquals(PaymentMethodUtils.Constants.METHOD, builderFactory.build().getMethod());
    }

    @Test
    public void testEmptyBuilder() {
        assertTrue(builderFactory.build().getId() < 0);
        assertNotNull(builderFactory.build().getMethod());
    }
}
