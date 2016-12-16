package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DistancePrimaryKeyTest {

    // Class under test
    DistancePrimaryKey mDistancePrimaryKey;

    @Mock
    Distance mDistance;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mDistancePrimaryKey = new DistancePrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals("id", mDistancePrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mDistancePrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final int id = 55;
        when(mDistance.getId()).thenReturn(id);
        assertEquals(id, (int) mDistancePrimaryKey.getPrimaryKeyValue(mDistance));
    }
}