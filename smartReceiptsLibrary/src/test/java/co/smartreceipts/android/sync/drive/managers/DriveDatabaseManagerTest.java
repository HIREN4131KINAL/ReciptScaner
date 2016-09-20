package co.smartreceipts.android.sync.drive.managers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.sync.drive.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class DriveDatabaseManagerTest {

    // Class under test
    DriveDatabaseManager mDriveDatabaseManager;

    File mDatabaseFile;

    @Mock
    DriveStreamsManager mDriveStreamsManager;

    @Mock
    GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mDatabaseFile = new File(RuntimeEnvironment.application.getExternalFilesDir(null), DatabaseHelper.DATABASE_NAME);
        if (!mDatabaseFile.createNewFile()) {
            throw new RuntimeException("Failed to create database file... Failing this test");
        }

        mDriveDatabaseManager = new DriveDatabaseManager(RuntimeEnvironment.application, mDriveStreamsManager, mGoogleDriveSyncMetadata, Schedulers.immediate(), Schedulers.immediate());
    }

    @After
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void tearDown() throws Exception {
        mDatabaseFile.delete();
    }

    @Test
    public void syncDatabaseForTheFirstTime() {
        final Identifier identifier = new Identifier("newId");
        when(mDriveStreamsManager.uploadFileToDrive(mDatabaseFile)).thenReturn(Observable.just(identifier));

        mDriveDatabaseManager.syncDatabase();
        verify(mGoogleDriveSyncMetadata).setDatabaseSyncIdentifier(identifier);
    }

    @Test
    public void syncExistingDatabase() {
        final Identifier identifier = new Identifier("oldId");
        when(mGoogleDriveSyncMetadata.getDatabaseSyncIdentifier()).thenReturn(identifier);
        when(mDriveStreamsManager.updateDriveFile(identifier, mDatabaseFile)).thenReturn(Observable.just(identifier));

        mDriveDatabaseManager.syncDatabase();
        verify(mGoogleDriveSyncMetadata).setDatabaseSyncIdentifier(identifier);
    }

}