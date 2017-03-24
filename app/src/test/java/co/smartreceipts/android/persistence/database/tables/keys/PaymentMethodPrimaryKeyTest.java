package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.PaymentMethod;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PaymentMethodPrimaryKeyTest {

    // Class under test
    PaymentMethodPrimaryKey mPaymentMethodPrimaryKey;

    @Mock
    PaymentMethod mPaymentMethod;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPaymentMethodPrimaryKey = new PaymentMethodPrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals("id", mPaymentMethodPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mPaymentMethodPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final int id = 55;
        when(mPaymentMethod.getId()).thenReturn(id);
        assertEquals(id, (int) mPaymentMethodPrimaryKey.getPrimaryKeyValue(mPaymentMethod));
    }
}