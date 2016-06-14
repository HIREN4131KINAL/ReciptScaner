package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ColumnPrimaryKeyTest {

    private static final String COLUMN = "column";

    // Class under test
    ColumnPrimaryKey mColumnPrimaryKey;

    @Mock
    Column<Receipt> mColumn;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mColumnPrimaryKey = new ColumnPrimaryKey(COLUMN);
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals(COLUMN, mColumnPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mColumnPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final int id = 55;
        when(mColumn.getId()).thenReturn(id);
        assertEquals(id, (int) mColumnPrimaryKey.getPrimaryKeyValue(mColumn));
    }
}