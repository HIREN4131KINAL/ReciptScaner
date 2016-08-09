package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.PaymentMethod;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class AutoIncrementIdPrimaryKeyTest {

    private static final int ID = 55;
    private static final String COLUMN = "column";

    // Class under test
    AutoIncrementIdPrimaryKey<PaymentMethod> mAutoIncrementIdPrimaryKey;

    @Mock
    PrimaryKey<PaymentMethod, Integer> mPaymentMethodIntegerPrimaryKey;

    @Mock
    PaymentMethod mPaymentMethod;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mPaymentMethodIntegerPrimaryKey.getPrimaryKeyColumn()).thenReturn(COLUMN);
        when(mPaymentMethod.getId()).thenReturn(-1);

        mAutoIncrementIdPrimaryKey = new AutoIncrementIdPrimaryKey<>(mPaymentMethodIntegerPrimaryKey, ID);
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals(COLUMN, mAutoIncrementIdPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mAutoIncrementIdPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        assertEquals(ID, (int) mAutoIncrementIdPrimaryKey.getPrimaryKeyValue(mPaymentMethod));
    }

}