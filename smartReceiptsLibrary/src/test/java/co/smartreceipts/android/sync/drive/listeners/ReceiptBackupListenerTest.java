package co.smartreceipts.android.sync.drive.listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiptBackupListenerTest {

    // Class under test
    ReceiptBackupListener mListener;

    @Mock
    DriveStreamsManager mDriveStreamsManager;
    
    @Mock
    DriveReceiptsManager mDriveReceiptsManager;
    
    @Mock
    Receipt mReceipt;

    @Mock
    Receipt mOldReceipt;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mListener = new ReceiptBackupListener(mDriveStreamsManager, mDriveReceiptsManager);
    }

    @Test
    public void onInsertSuccess() {
        mListener.onInsertSuccess(mReceipt, new DatabaseOperationMetadata());
        verify(mDriveStreamsManager).updateDatabase();
        verify(mDriveReceiptsManager).handleInsertOrUpdate(mReceipt);
    }

    @Test
    public void onSyncInsertSuccess() {
        mListener.onInsertSuccess(mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveStreamsManager, never()).updateDatabase();
        verify(mDriveReceiptsManager, never()).handleInsertOrUpdate(any(Receipt.class));
    }

    @Test
    public void onUpdateSuccess() {
        mListener.onUpdateSuccess(mOldReceipt, mReceipt, new DatabaseOperationMetadata());
        verify(mDriveStreamsManager).updateDatabase();
        verify(mDriveReceiptsManager).handleInsertOrUpdate(mReceipt);
    }

    @Test
    public void onSyncUpdateSuccess() {
        mListener.onUpdateSuccess(mOldReceipt, mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveStreamsManager, never()).updateDatabase();
        verify(mDriveReceiptsManager, never()).handleInsertOrUpdate(any(Receipt.class));
    }

    @Test
    public void onDeleteSuccess() {
        mListener.onDeleteSuccess(mReceipt, new DatabaseOperationMetadata());
        verify(mDriveStreamsManager).updateDatabase();
        verify(mDriveReceiptsManager).handleDelete(mReceipt);
    }

    @Test
    public void onSyncDeleteSuccess() {
        mListener.onDeleteSuccess(mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveStreamsManager, never()).updateDatabase();
        verify(mDriveReceiptsManager, never()).handleDelete(any(Receipt.class));
    }

}