package co.smartreceipts.android;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

import static org.mockito.Mockito.when;

/**
 * Instrumentation test version of {@link AbstractPdfBoxFullReportTest}.
 *
 * This test should be {@link Ignore}d by default. It's an interactive rather than
 * an automated test.
 *
 * TODO cleanup files on <code>@After</code>
 */
@Ignore
public class PdfBoxFullPdfReportInstrTest extends AbstractPdfBoxFullReportTest {

    @Before
    public void setup() {
        super.setup();
        mPersistenceManager = Mockito.mock(PersistenceManager.class);
        mPreferences = Mockito.mock(UserPreferenceManager.class);

        when(mPersistenceManager.getPreferenceManager()).thenReturn(mPreferences);

        when(mPreferences.get(UserPreference.General.DateSeparator)).thenReturn("/");
        when(mPreferences.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)).thenReturn(true);
        when(mPreferences.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("Custom Footer!!!");
        when(mPreferences.get(UserPreference.Distance.PrintDistanceTableInReports)).thenReturn(false);
        when(mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)).thenReturn(false);
    }

    @Override
    @Test
    public void testPdfGeneration() throws Exception {
        super.testPdfGeneration();
    }

    @Override
    protected boolean useBuiltinFonts() {
        return false;
    }

    @Override
    protected Context getContext() {
        return InstrumentationRegistry.getContext();
    }

    /**
     * The pdf file is generated in external storage.
     * @return
     * @throws IOException
     */
    @Override
    public File createOutputFile() throws IOException {
        File file = (new File(mContext.getExternalFilesDir(null), OUTPUT_FILE));
        Log.d("TEST", "createOutputFile: " + file.getAbsolutePath());
        file.createNewFile();
        return file;
    }


    /**
     * Reads the image files from the <code>assets</code> directory, and copies them
     * over to external storage so that they will be available later to the application code.
     * @param fileName
     * @return
     * @throws IOException
     */
    @Override
    protected File getImageFile(String fileName) throws IOException {
        InputStream is = mContext.getAssets().open("pdf/" + fileName);

        File file = new File(mContext.getExternalFilesDir(null), fileName);
        OutputStream outStream = new FileOutputStream(file);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        is.close();
        outStream.close();

        return file;
    }



}


