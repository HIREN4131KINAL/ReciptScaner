package co.smartreceipts.android.sync.drive.listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class DatabaseBackupListenerTest {

    // Class under test
    DatabaseBackupListener<Object> mListener;

    @Mock
    DriveStreamsManager mDriveStreamsManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mListener = new DatabaseBackupListener<>(mDriveStreamsManager);
    }

    @Test
    public void onInsertSuccess() {
        mListener.onInsertSuccess(new Object(), new DatabaseOperationMetadata());
        verify(mDriveStreamsManager).updateDatabase();
    }

    @Test
    public void onSyncInsertSuccess() {
        mListener.onInsertSuccess(new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveStreamsManager, never()).updateDatabase();
    }

    @Test
    public void onUpdateSuccess() {
        mListener.onUpdateSuccess(new Object(), new Object(), new DatabaseOperationMetadata());
        verify(mDriveStreamsManager).updateDatabase();
    }

    @Test
    public void onSyncUpdateSuccess() {
        mListener.onUpdateSuccess(new Object(), new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveStreamsManager, never()).updateDatabase();
    }

    @Test
    public void onDeleteSuccess() {
        mListener.onDeleteSuccess(new Object(), new DatabaseOperationMetadata());
        verify(mDriveStreamsManager).updateDatabase();
    }

    @Test
    public void onSyncDeleteSuccess() {
        mListener.onDeleteSuccess(new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveStreamsManager, never()).updateDatabase();
    }

}