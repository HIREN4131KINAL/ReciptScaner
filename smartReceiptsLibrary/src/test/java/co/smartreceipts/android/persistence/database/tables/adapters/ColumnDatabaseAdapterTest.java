package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.ConstantColumn;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ColumnDatabaseAdapterTest {

    private static final String ID_COLUMN = "id_column";
    private static final String NAME_COLUMN = "name_column";

    private static final int ID = 5;
    private static final int PRIMARY_KEY_ID = 11;
    private static final String NAME = "abcd";

    // Class under test
    ColumnDatabaseAdapter mColumnDatabaseAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    Column<Receipt> mColumn;

    @Mock
    PrimaryKey<Column<Receipt>, Integer> mPrimaryKey;

    @Mock
    ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    Column<Receipt> mIdColumn;

    Column<Receipt> mPrimaryKeyIdColumn;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int nameIndex = 2;
        when(mCursor.getColumnIndex(ID_COLUMN)).thenReturn(idIndex);
        when(mCursor.getColumnIndex(NAME_COLUMN)).thenReturn(nameIndex);
        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);

        when(mColumn.getId()).thenReturn(ID);
        when(mColumn.getName()).thenReturn(NAME);

        mIdColumn = new ConstantColumn<>(ID, NAME);
        mPrimaryKeyIdColumn = new ConstantColumn<>(PRIMARY_KEY_ID, NAME);
        when(mReceiptColumnDefinitions.getColumn(ID, NAME)).thenReturn(mIdColumn);
        when(mReceiptColumnDefinitions.getColumn(PRIMARY_KEY_ID, NAME)).thenReturn(mPrimaryKeyIdColumn);

        when(mPrimaryKey.getPrimaryKeyValue(mColumn)).thenReturn(PRIMARY_KEY_ID);

        mColumnDatabaseAdapter = new ColumnDatabaseAdapter(mReceiptColumnDefinitions, ID_COLUMN, NAME_COLUMN);
    }

    @Test
    public void read() throws Exception {
        assertEquals(mIdColumn, mColumnDatabaseAdapter.read(mCursor));
    }

    @Test
    public void write() throws Exception {
        final ContentValues contentValues = mColumnDatabaseAdapter.write(mColumn);
        assertEquals(NAME, contentValues.getAsString(NAME_COLUMN));
        assertFalse(contentValues.containsKey(ID_COLUMN));
    }

    @Test
    public void build() throws Exception {
        assertEquals(mPrimaryKeyIdColumn, mColumnDatabaseAdapter.build(mColumn, mPrimaryKey));
    }
}