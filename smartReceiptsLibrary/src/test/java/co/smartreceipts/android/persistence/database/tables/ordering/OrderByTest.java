package co.smartreceipts.android.persistence.database.tables.ordering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderByTest {

    @Test
    public void getOrderByPredicate() {
        final String column = "column";
        final OrderBy descending = new OrderBy(column, true);
        final OrderBy ascending = new OrderBy(column, false);
        final OrderBy defaultOrder = new OrderBy(null, true);

        assertEquals(column + " DESC", descending.getOrderByPredicate());
        assertEquals(column + " ASC", ascending.getOrderByPredicate());
        assertNull(defaultOrder.getOrderByPredicate());

        assertEquals(descending.toString(), descending.getOrderByPredicate());
        assertEquals(ascending.toString(), ascending.getOrderByPredicate());
        assertEquals(defaultOrder.toString(), defaultOrder.getOrderByPredicate());
    }

}