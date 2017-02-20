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
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

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
        mPreferences = Mockito.mock(UserPreferenceManager.class);

        when(mPersistenceManager.getPreferenceManager()).thenReturn(mPreferences);

        when(mPreferences.get(UserPreference.General.DateSeparator)).thenReturn("/");
        when(mPreferences.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)).thenReturn(true);
        when(mPreferences.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("FOOTER");
        when(mPreferences.get(UserPreference.Distance.PrintDistanceTableInReports)).thenReturn(true);
        when(mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)).thenReturn(false);
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

}
