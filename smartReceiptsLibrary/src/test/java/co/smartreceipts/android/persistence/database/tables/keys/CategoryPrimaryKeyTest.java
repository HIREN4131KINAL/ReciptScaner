package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Category;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CategoryPrimaryKeyTest {

    // Class under test
    CategoryPrimaryKey mCategoryPrimaryKey;

    @Mock
    Category mCategory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mCategoryPrimaryKey = new CategoryPrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals("name", mCategoryPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(String.class, mCategoryPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final String name = "abcd";
        when(mCategory.getName()).thenReturn(name);
        assertEquals(name, mCategoryPrimaryKey.getPrimaryKeyValue(mCategory));
    }
}