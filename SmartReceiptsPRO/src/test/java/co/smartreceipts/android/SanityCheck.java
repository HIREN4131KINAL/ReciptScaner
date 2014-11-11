package co.smartreceipts.android;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import wb.receiptspro.R;
import co.smartreceipts.android.activities.SmartReceiptsActivity;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class SanityCheck {

	private SmartReceiptsActivity mActivity;

	@Before
	public void setup() {
		mActivity = Robolectric.buildActivity(SmartReceiptsActivity.class).create().get();
	}

	@After
	public void tearDown() {
		mActivity = null;
	}

	/**
	 * This is a very simple sanity check to confirm that Robo Electric is actually working
	 * The app name should stay the same across translations
	 * @throws Exception
	 */
	@Test
    public void sanityCheck() throws Exception
    {
        String appName = mActivity.getString(R.string.sr_app_name);
        assertEquals(appName, "Smart Receipts Pro");
    }

}
