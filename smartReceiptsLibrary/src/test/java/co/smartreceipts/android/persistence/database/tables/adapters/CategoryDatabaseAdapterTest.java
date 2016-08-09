package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CategoryDatabaseAdapterTest {

    private static final String NAME = "name_123";
    private static final String PRIMARY_KEY_NAME = "name_456";
    private static final String CODE = "code_123";

    // Class under test
    CategoryDatabaseAdapter mCategoryDatabaseAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    Category mCategory;

    @Mock
    PrimaryKey<Category, String> mPrimaryKey;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int nameIndex = 1;
        final int codeIndex = 2;
        when(mCursor.getColumnIndex("name")).thenReturn(nameIndex);
        when(mCursor.getColumnIndex("code")).thenReturn(codeIndex);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getString(codeIndex)).thenReturn(CODE);

        when(mCategory.getName()).thenReturn(NAME);
        when(mCategory.getCode()).thenReturn(CODE);

        when(mPrimaryKey.getPrimaryKeyValue(mCategory)).thenReturn(PRIMARY_KEY_NAME);

        mCategoryDatabaseAdapter = new CategoryDatabaseAdapter();
    }

    @Test
    public void read() throws Exception {
        final Category category = new CategoryBuilderFactory().setName(NAME).setCode(CODE).build();
        assertEquals(category, mCategoryDatabaseAdapter.read(mCursor));
    }

    @Test
    public void write() throws Exception {
        final ContentValues contentValues = mCategoryDatabaseAdapter.write(mCategory);
        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(CODE, contentValues.getAsString("code"));
    }

    @Test
    public void build() throws Exception {
        final Category category = new CategoryBuilderFactory().setName(PRIMARY_KEY_NAME).setCode(CODE).build();
        assertEquals(category, mCategoryDatabaseAdapter.build(mCategory, mPrimaryKey));
    }
}