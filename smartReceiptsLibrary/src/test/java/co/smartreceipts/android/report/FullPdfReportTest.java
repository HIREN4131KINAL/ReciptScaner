package co.smartreceipts.android.report;


import android.content.Context;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import co.smartreceipts.android.AbstractPdfBoxFullReportTest;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;

import static org.mockito.Mockito.when;

/**
 * Unit test version of {@link AbstractPdfBoxFullReportTest}.
 *
 * This test should be {@link Ignore}d by default. It's an interactive rather than
 * an automated test.
 *
 */
@RunWith(RobolectricTestRunner.class)
@Ignore
public class FullPdfReportTest extends AbstractPdfBoxFullReportTest {


    @Before
    public void setup() {
        super.setup();

        mPersistenceManager = Mockito.mock(PersistenceManager.class);
        mPreferences = Mockito.mock(Preferences.class);

        when(mPersistenceManager.getPreferences()).thenReturn(mPreferences);

        when(mPreferences.getDateSeparator()).thenReturn("/");
        when(mPreferences.getIncludeCommentByReceiptPhoto()).thenReturn(true);
        when(mPreferences.getPdfFooterText()).thenReturn("FOOTER");
        when(mPreferences.getPrintDistanceTable()).thenReturn(true);
        when(mPreferences.isReceiptsTableLandscapeMode()).thenReturn(false);
    }


    @Override
    protected Context getContext() {
        return RuntimeEnvironment.application;
    }

    /**
     * The file is generated in the <code>smartReceiptLibrary</code>
     * module's root directory.
     * @return
     */
    @Override
    public File createOutputFile() {
        return new File(OUTPUT_FILE);
    }


    /**
     * Images are read from the resources directory.
     * @param fileName
     * @return
     */
    @Override
    protected File getImageFile(String fileName) {
        return new File(getClass().getClassLoader()
                .getResource("pdf/" + fileName).getFile());
    }



    @Override
    @Test
    public void testPdfGeneration() throws Exception {
        super.testPdfGeneration();
    }

    @Override
    protected boolean useBuiltinFonts() {
        return true;
    }
}
