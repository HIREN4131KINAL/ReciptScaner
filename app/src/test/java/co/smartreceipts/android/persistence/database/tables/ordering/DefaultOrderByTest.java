package co.smartreceipts.android.persistence.database.tables.ordering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class DefaultOrderByTest {

    @Test
    public void getOrderByPredicate() {
        final DefaultOrderBy defaultOrderBy = new DefaultOrderBy();
        assertNull(defaultOrderBy.getOrderByPredicate());
        assertEquals(defaultOrderBy.toString(), defaultOrderBy.getOrderByPredicate());
    }

}