package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Trip;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TripPrimaryKeyTest {

    // Class under test
    TripPrimaryKey mTripPrimaryKey;

    @Mock
    Trip mTrip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mTripPrimaryKey = new TripPrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals("name", mTripPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(String.class, mTripPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final String name = "abcd";
        when(mTrip.getName()).thenReturn(name);
        assertEquals(name, mTripPrimaryKey.getPrimaryKeyValue(mTrip));
    }
}