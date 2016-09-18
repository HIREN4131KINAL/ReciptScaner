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
import co.smartreceipts.android.sync.model.impl.SyncStatusMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SyncStateAdapterTest {

    private static final String IDENTIFIER_STRING = "id";
    private static final boolean DRIVE_IS_SYNCED = true;
    private static final boolean DRIVE_IS_MARKED = true;
    private static final long LAST_LOCAL_MODIFICATION_TIME = 50000000000L;

    // Class under test
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    SyncState mSyncState;

    IdentifierMap mIdentifierMap = new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier(IDENTIFIER_STRING)));

    SyncStatusMap mSyncStatusMap = new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, DRIVE_IS_SYNCED));

    MarkedForDeletionMap mMarkedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, DRIVE_IS_MARKED));

    Date mLastLocalModificationTime = new Date(LAST_LOCAL_MODIFICATION_TIME);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int identifierIndex = 1;
        final int driveIsSyncedIndex = 2;
        final int driveIsMarkedForDeletionIndex = 3;
        final int lastLocalModificationTimeIndex = 4;
        when(mCursor.getColumnIndex("drive_sync_id")).thenReturn(identifierIndex);
        when(mCursor.getColumnIndex("drive_is_synced")).thenReturn(driveIsSyncedIndex);
        when(mCursor.getColumnIndex("drive_marked_for_deletion")).thenReturn(driveIsMarkedForDeletionIndex);
        when(mCursor.getColumnIndex("last_local_modification_time")).thenReturn(lastLocalModificationTimeIndex);

        when(mCursor.getString(identifierIndex)).thenReturn(IDENTIFIER_STRING);
        when(mCursor.getInt(driveIsSyncedIndex)).thenReturn(DRIVE_IS_SYNCED ? 1 : 0);
        when(mCursor.getInt(driveIsMarkedForDeletionIndex)).thenReturn(DRIVE_IS_MARKED ? 1 : 0);
        when(mCursor.getLong(lastLocalModificationTimeIndex)).thenReturn(LAST_LOCAL_MODIFICATION_TIME);

        when(mSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(new Identifier(IDENTIFIER_STRING));
        when(mSyncState.isSynced(SyncProvider.GoogleDrive)).thenReturn(DRIVE_IS_SYNCED);
        when(mSyncState.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(DRIVE_IS_MARKED);
        when(mSyncState.getLastLocalModificationTime()).thenReturn(mLastLocalModificationTime);

        mSyncStateAdapter = new SyncStateAdapter();
    }

    @Test
    public void read() throws Exception {
        final DefaultSyncState syncState = new DefaultSyncState(mIdentifierMap, mSyncStatusMap, mMarkedForDeletionMap, mLastLocalModificationTime);
        assertEquals(syncState, mSyncStateAdapter.read(mCursor));
    }

    @Test
    public void writeUnsynced() throws Exception {
        final ContentValues contentValues = mSyncStateAdapter.writeUnsynced(mSyncState);
        assertEquals(IDENTIFIER_STRING, contentValues.getAsString("drive_sync_id"));
        assertFalse(contentValues.getAsBoolean("drive_is_synced")); // Note: It's always false here
        assertEquals(DRIVE_IS_MARKED, contentValues.getAsBoolean("drive_marked_for_deletion"));
        assertEquals(LAST_LOCAL_MODIFICATION_TIME, (long) contentValues.getAsLong("last_local_modification_time"));
    }

    @Test
    public void writeUnsyncedWithNullId() throws Exception {
        when(mSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        final ContentValues contentValues = mSyncStateAdapter.writeUnsynced(mSyncState);
        assertNull(contentValues.getAsString("drive_sync_id"));
        assertFalse(contentValues.getAsBoolean("drive_is_synced")); // Note: It's always false here
        assertEquals(DRIVE_IS_MARKED, contentValues.getAsBoolean("drive_marked_for_deletion"));
        assertEquals(LAST_LOCAL_MODIFICATION_TIME, (long) contentValues.getAsLong("last_local_modification_time"));
    }

    @Test
    public void write() throws Exception {
        final ContentValues contentValues = mSyncStateAdapter.write(mSyncState);
        assertEquals(IDENTIFIER_STRING, contentValues.getAsString("drive_sync_id"));
        assertEquals(DRIVE_IS_SYNCED, contentValues.getAsBoolean("drive_is_synced"));
        assertEquals(DRIVE_IS_MARKED, contentValues.getAsBoolean("drive_marked_for_deletion"));
        assertEquals(LAST_LOCAL_MODIFICATION_TIME, (long) contentValues.getAsLong("last_local_modification_time"));
    }

    @Test
    public void writeWithNullId() throws Exception {
        when(mSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        final ContentValues contentValues = mSyncStateAdapter.write(mSyncState);
        assertNull(contentValues.getAsString("drive_sync_id"));
        assertEquals(DRIVE_IS_SYNCED, contentValues.getAsBoolean("drive_is_synced"));
        assertEquals(DRIVE_IS_MARKED, contentValues.getAsBoolean("drive_marked_for_deletion"));
        assertEquals(LAST_LOCAL_MODIFICATION_TIME, (long) contentValues.getAsLong("last_local_modification_time"));
    }

}