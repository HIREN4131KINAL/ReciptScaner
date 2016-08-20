package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import java.sql.Date;
import java.util.Collections;

import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.android.sync.model.impl.Identifier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SyncStateAdapterTest {

    private static final String IDENTIFIER_MAP_JSON = "{\"identifier_map\":{\"GoogleDrive\":{\"id\":\"abc\"}}}";
    private static final String MARKED_FOR_DELETION_MAP_JSON = "{\"deletion_map\":{\"GoogleDrive\":true}}";
    private static final long LAST_LOCAL_MODIFICATION_TIME = 50000000000L;

    // Class under test
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    SyncState mSyncState;

    IdentifierMap mIdentifierMap = new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("abc")));

    MarkedForDeletionMap mMarkedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true));

    Date mLastLocalModificationTime = new Date(LAST_LOCAL_MODIFICATION_TIME);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int identifierIndex = 1;
        final int markedForDeletionIndex = 2;
        final int lastLocalModificationTimeIndex = 3;
        when(mCursor.getColumnIndex("remote_sync_id")).thenReturn(identifierIndex);
        when(mCursor.getColumnIndex("marked_for_deletion")).thenReturn(markedForDeletionIndex);
        when(mCursor.getColumnIndex("last_local_modification_time")).thenReturn(lastLocalModificationTimeIndex);

        when(mCursor.getString(identifierIndex)).thenReturn(IDENTIFIER_MAP_JSON);
        when(mCursor.getString(markedForDeletionIndex)).thenReturn(MARKED_FOR_DELETION_MAP_JSON);
        when(mCursor.getLong(lastLocalModificationTimeIndex)).thenReturn(LAST_LOCAL_MODIFICATION_TIME);

        when(mSyncState.getIdentifierMap()).thenReturn(mIdentifierMap);
        when(mSyncState.getMarkedForDeletionMap()).thenReturn(mMarkedForDeletionMap);
        when(mSyncState.getLastLocalModificationTime()).thenReturn(mLastLocalModificationTime);

        mSyncStateAdapter = new SyncStateAdapter();
    }

    @Test
    public void read() throws Exception {
        final DefaultSyncState syncState = new DefaultSyncState(mIdentifierMap, mMarkedForDeletionMap, mLastLocalModificationTime);
        assertEquals(syncState, mSyncStateAdapter.read(mCursor));
    }

    @Test
    public void write() throws Exception {
        final ContentValues contentValues = mSyncStateAdapter.write(mSyncState);
        assertEquals(IDENTIFIER_MAP_JSON, contentValues.getAsString("remote_sync_id"));
        assertEquals(MARKED_FOR_DELETION_MAP_JSON, contentValues.getAsString("marked_for_deletion"));
        assertEquals(LAST_LOCAL_MODIFICATION_TIME, (long) contentValues.getAsLong("last_local_modification_time"));
    }

}