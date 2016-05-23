package co.smartreceipts.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class SanityCheck {

	@Test
    public void sanityCheck() throws Exception {
        assertEquals(RuntimeEnvironment.application.getString(R.string.sr_app_name), "Smart Receipts");
    }

}
