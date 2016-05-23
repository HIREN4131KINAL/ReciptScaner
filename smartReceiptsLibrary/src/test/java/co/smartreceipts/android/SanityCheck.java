package co.smartreceipts.android;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import co.smartreceipts.android.activities.SmartReceiptsActivity;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
public class SanityCheck {

	private SmartReceiptsActivity mActivity;

	@Before
	public void setup() {
		mActivity = Robolectric.buildActivity(SmartReceiptsActivity.class).create().get();
	}

	@Test
    public void sanityCheck() throws Exception {
        assertEquals(mActivity.getString(R.string.sr_app_name), "Smart Receipts");
    }

}
