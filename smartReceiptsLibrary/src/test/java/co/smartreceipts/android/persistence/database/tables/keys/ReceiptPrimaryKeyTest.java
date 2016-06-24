package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Receipt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiptPrimaryKeyTest {

    // Class under test
    ReceiptPrimaryKey mReceiptPrimaryKey;

    @Mock
    Receipt mReceipt;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mReceiptPrimaryKey = new ReceiptPrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals("id", mReceiptPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mReceiptPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final int id = 55;
        when(mReceipt.getId()).thenReturn(id);
        assertEquals(id, (int) mReceiptPrimaryKey.getPrimaryKeyValue(mReceipt));
    }
}