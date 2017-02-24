package co.smartreceipts.android.imports;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.StorageManager;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class FileImportProcessorFactoryTest {

    // Class under test
    FileImportProcessorFactory factory;

    @Mock
    Trip trip;

    @Mock
    StorageManager storageManager;

    @Mock
    UserPreferenceManager preferenceManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.factory = new FileImportProcessorFactory(RuntimeEnvironment.application, trip, storageManager, preferenceManager);
    }

    @Test
    public void get() {
        // Image Imports
        assertTrue(this.factory.get(RequestCodes.NATIVE_ADD_PHOTO_CAMERA_REQUEST) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.NATIVE_NEW_RECEIPT_CAMERA_REQUEST) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.NATIVE_RETAKE_PHOTO_CAMERA_REQUEST) instanceof ImageImportProcessor);
        assertTrue(this.factory.get(RequestCodes.IMPORT_GALLERY_IMAGE) instanceof ImageImportProcessor);

        // PDF Imports
        assertTrue(this.factory.get(RequestCodes.IMPORT_GALLERY_PDF) instanceof GenericFileImportProcessor);

        // Rest are auto fail
        assertTrue(this.factory.get(-1) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(0) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(Integer.MAX_VALUE) instanceof AutoFailImportProcessor);
        assertTrue(this.factory.get(Integer.MIN_VALUE) instanceof AutoFailImportProcessor);
    }

}