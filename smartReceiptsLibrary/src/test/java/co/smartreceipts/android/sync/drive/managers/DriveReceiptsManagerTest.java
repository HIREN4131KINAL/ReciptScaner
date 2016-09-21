package co.smartreceipts.android.sync.drive.managers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.Arrays;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactoryFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.drive.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.DriveStreamMappings;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class DriveReceiptsManagerTest {

    // Class under testing
    DriveReceiptsManager mDriveReceiptsManager;

    @Mock
    TableController<Receipt> mReceiptTableController;

    @Mock
    ReceiptsTable mReceiptsTable;

    @Mock
    DriveStreamsManager mDriveTaskManager;

    @Mock
    DriveStreamMappings mDriveStreamMappings;

    @Mock
    ReceiptBuilderFactoryFactory mReceiptBuilderFactoryFactory;

    @Mock
    ReceiptBuilderFactory mReceiptBuilderFactory1, mReceiptBuilderFactory2;

    @Mock
    Receipt mReceipt1, mReceipt2;

    @Mock
    SyncState mSyncState1, mSyncState2, mNewSyncState1, mNewSyncState2;

    @Mock
    File mFile;

    @Captor
    ArgumentCaptor<Receipt> mReceiptCaptor;

    @Captor
    ArgumentCaptor<Receipt> mUpdatedReceiptCaptor;

    @Captor
    ArgumentCaptor<DatabaseOperationMetadata> mOperationMetadataCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mReceipt1.getSyncState()).thenReturn(mSyncState1);
        when(mReceipt2.getSyncState()).thenReturn(mSyncState2);

        when(mReceiptBuilderFactory1.build()).thenReturn(mReceipt1);
        when(mReceiptBuilderFactory2.build()).thenReturn(mReceipt2);
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                return mReceiptBuilderFactory1;
            }
        }).when(mReceiptBuilderFactoryFactory).build(mReceipt1);
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                return mReceiptBuilderFactory2;
            }
        }).when(mReceiptBuilderFactoryFactory).build(mReceipt2);
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                when(mReceipt1.getSyncState()).thenReturn((SyncState)invocation.getArguments()[0]);
                return mReceiptBuilderFactory1;
            }
        }).when(mReceiptBuilderFactory1).setSyncState(any(SyncState.class));
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                when(mReceipt2.getSyncState()).thenReturn((SyncState)invocation.getArguments()[0]);
                return mReceiptBuilderFactory2;
            }
        }).when(mReceiptBuilderFactory2).setSyncState(any(SyncState.class));

        mDriveReceiptsManager = new DriveReceiptsManager(mReceiptTableController, mReceiptsTable, mDriveTaskManager,
                mDriveStreamMappings, mReceiptBuilderFactoryFactory, Schedulers.immediate(), Schedulers.immediate());
    }

    @Test
    public void handleDelete() {
        when(mDriveTaskManager.deleteDriveFile(mSyncState1, true)).thenReturn(Observable.just(mNewSyncState1));
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);

        mDriveReceiptsManager.handleDelete(mReceipt1);

        verify(mReceiptTableController).delete(mReceiptCaptor.capture(), mOperationMetadataCaptor.capture());
        assertEquals(mReceipt1, mReceiptCaptor.getValue());
        assertEquals(mNewSyncState1, mReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, mOperationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleDeleteForIllegalSyncState() {
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(true);
        mDriveReceiptsManager.handleDelete(mReceipt1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleDeleteForIllegalMarkedForDeletionState() {
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        mDriveReceiptsManager.handleDelete(mReceipt1);
    }

    @Test
    public void handleInsertOrUpdateForNewFile() {
        when(mDriveTaskManager.uploadFileToDrive(mSyncState1, mFile)).thenReturn(Observable.just(mNewSyncState1));
        when(mSyncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mReceipt1.getFile()).thenReturn(mFile);

        mDriveReceiptsManager.handleInsertOrUpdate(mReceipt1);

        verify(mReceiptTableController).update(mReceiptCaptor.capture(), mUpdatedReceiptCaptor.capture(), mOperationMetadataCaptor.capture());
        assertNotNull(mReceiptCaptor.getValue());
        assertEquals(mReceipt1, mUpdatedReceiptCaptor.getValue());
        assertEquals(mNewSyncState1, mUpdatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, mOperationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleInsertWithoutFile() {
        when(mDriveStreamMappings.postInsertSyncState(mSyncState1, null)).thenReturn(mNewSyncState1);
        when(mSyncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mReceipt1.getFile()).thenReturn(null);

        mDriveReceiptsManager.handleInsertOrUpdate(mReceipt1);

        verify(mReceiptTableController).update(mReceiptCaptor.capture(), mUpdatedReceiptCaptor.capture(), mOperationMetadataCaptor.capture());
        assertNotNull(mReceiptCaptor.getValue());
        assertEquals(mReceipt1, mUpdatedReceiptCaptor.getValue());
        assertEquals(mNewSyncState1, mUpdatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, mOperationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleUpdateWithNewFile() {
        final Identifier identifier = new Identifier("id");
        when(mDriveTaskManager.updateDriveFile(mSyncState1, mFile)).thenReturn(Observable.just(mNewSyncState1));
        when(mSyncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mReceipt1.getFile()).thenReturn(mFile);

        mDriveReceiptsManager.handleInsertOrUpdate(mReceipt1);

        verify(mReceiptTableController).update(mReceiptCaptor.capture(), mUpdatedReceiptCaptor.capture(), mOperationMetadataCaptor.capture());
        assertNotNull(mReceiptCaptor.getValue());
        assertEquals(mReceipt1, mUpdatedReceiptCaptor.getValue());
        assertEquals(mNewSyncState1, mUpdatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, mOperationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleUpdateToDeleteExistingFile() {
        final Identifier identifier = new Identifier("id");
        when(mDriveTaskManager.deleteDriveFile(mSyncState1, false)).thenReturn(Observable.just(mNewSyncState1));
        when(mSyncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mReceipt1.getFile()).thenReturn(null);

        mDriveReceiptsManager.handleInsertOrUpdate(mReceipt1);

        verify(mReceiptTableController).update(mReceiptCaptor.capture(), mUpdatedReceiptCaptor.capture(), mOperationMetadataCaptor.capture());
        assertNotNull(mReceiptCaptor.getValue());
        assertEquals(mReceipt1, mUpdatedReceiptCaptor.getValue());
        assertEquals(mNewSyncState1, mUpdatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, mOperationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInsertOrUpdateForIllegalSyncState() {
        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(true);
        mDriveReceiptsManager.handleInsertOrUpdate(mReceipt1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInsertOrUpdateForIllegalMarkedForDeletionState() {
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        mDriveReceiptsManager.handleInsertOrUpdate(mReceipt1);
    }

    @Test
    public void initialize() {
        final DriveReceiptsManager spiedManager = spy(mDriveReceiptsManager);
        doNothing().when(spiedManager).handleInsertOrUpdate(any(Receipt.class));
        doNothing().when(spiedManager).handleDelete(any(Receipt.class));

        when(mSyncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState2.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(mSyncState2.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        when(mReceiptsTable.getUnsynced(SyncProvider.GoogleDrive)).thenReturn(Observable.just(Arrays.asList(mReceipt1, mReceipt2)));

        spiedManager.initialize();

        verify(spiedManager).handleInsertOrUpdate(mReceipt1);
        verify(spiedManager).handleDelete(mReceipt2);
    }

}