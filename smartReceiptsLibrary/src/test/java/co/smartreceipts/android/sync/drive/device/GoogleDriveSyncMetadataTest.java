package co.smartreceipts.android.sync.drive.device;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class GoogleDriveSyncMetadataTest {

    // Class under test
    GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;

    @Before
    public void setUp() throws Exception {
        mGoogleDriveSyncMetadata = new GoogleDriveSyncMetadata(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        mGoogleDriveSyncMetadata.clear();
    }

    @Test
    public void getDeviceIdentifier() {
        final Identifier deviceId = mGoogleDriveSyncMetadata.getDeviceIdentifier();
        assertNotNull(deviceId);
        assertEquals(deviceId, mGoogleDriveSyncMetadata.getDeviceIdentifier());
    }

    @Test
    public void getDatabaseSyncIdentifier() {
        assertNull(mGoogleDriveSyncMetadata.getDatabaseSyncIdentifier());
        assertTrue(mGoogleDriveSyncMetadata.getLastDatabaseSyncTime().getTime() <= 0);

        final Identifier databaseId = new Identifier("id");
        mGoogleDriveSyncMetadata.setDatabaseSyncIdentifier(databaseId);
        assertEquals(databaseId, mGoogleDriveSyncMetadata.getDatabaseSyncIdentifier());
        assertTrue(mGoogleDriveSyncMetadata.getLastDatabaseSyncTime().getTime() > 0);
    }

}