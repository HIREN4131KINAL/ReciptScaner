package co.smartreceipts.android.model.comparators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.sql.Date;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ReceiptDateComparatorTest {

    ReceiptDateComparator ascendingComparator, descendingComparator, defaultComparator;

    @Mock
    Receipt first, second;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // DESC places the 'most recent' date at the front of the list
        ascendingComparator = new ReceiptDateComparator(true);
        descendingComparator = new ReceiptDateComparator(false);
        defaultComparator = new ReceiptDateComparator(); // same as ascendingComparator

        final long now = System.currentTimeMillis();
        when(first.getDate()).thenReturn(new Date(now));
        when(second.getDate()).thenReturn(new Date(now + 10000L));
    }

    @Test
    public void compareNullFirstToNullSecond() {
        assertTrue(ascendingComparator.compare(null, null) == 0);
        assertTrue(descendingComparator.compare(null, null) == 0);
        assertTrue(defaultComparator.compare(null, null) == 0);
    }

    @Test
    public void compareFirstToNullSecond() {
        assertTrue(ascendingComparator.compare(first, null) > 0);
        assertTrue(descendingComparator.compare(first, null) > 0);
        assertTrue(defaultComparator.compare(first, null) > 0);
    }

    @Test
    public void compareNullFirstToSecond() {
        assertTrue(ascendingComparator.compare(null, second) < 0);
        assertTrue(descendingComparator.compare(null, second) < 0);
        assertTrue(defaultComparator.compare(null, second) < 0);
    }

    @Test
    public void compareFirstToFirst() {
        assertTrue(ascendingComparator.compare(first, first) == 0);
        assertTrue(descendingComparator.compare(first, first) == 0);
        assertTrue(defaultComparator.compare(first, first) == 0);
    }

    @Test
    public void compareFirstToSecond() {
        assertTrue(ascendingComparator.compare(first, second) < 0);
        assertTrue(descendingComparator.compare(first, second) > 0);
        assertTrue(defaultComparator.compare(first, second) < 0);
    }

    @Test
    public void compareSecondToFirst() {
        assertTrue(ascendingComparator.compare(second, first) > 0);
        assertTrue(descendingComparator.compare(second, first) < 0);
        assertTrue(defaultComparator.compare(second, first) > 0);
    }

}
