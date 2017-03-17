package co.smartreceipts.android.utils.sorting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Category;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CategoryNameComparatorTest {

    @Test
    public void compare() {
        final CategoryNameComparator comparator = new CategoryNameComparator();
        final Category category1 = mock(Category.class);
        final Category category2 = mock(Category.class);
        final String string1 = "abcde";
        final String string2 = "wxyz";
        when(category1.getName()).thenReturn(string1);
        when(category2.getName()).thenReturn(string2);
        when(category1.getCode()).thenReturn("");
        when(category2.getCode()).thenReturn("");
        assertTrue(comparator.compare(category1, category1) == 0);
        assertTrue(comparator.compare(category2, category2) == 0);
        assertTrue(comparator.compare(category1, category2) < 0);
        assertTrue(comparator.compare(category2, category1) > 0);
    }
}