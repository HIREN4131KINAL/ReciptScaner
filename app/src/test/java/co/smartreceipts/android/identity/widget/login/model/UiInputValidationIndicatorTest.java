package co.smartreceipts.android.identity.widget.login.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class UiInputValidationIndicatorTest {

    @Test
    public void getters() throws Exception {
        final UiInputValidationIndicator indicator1 = new UiInputValidationIndicator("test1", true, true);
        assertEquals("test1", indicator1.getMessage());
        assertTrue(indicator1.isEmailValid());
        assertTrue(indicator1.isPasswordValid());

        final UiInputValidationIndicator indicator2 = new UiInputValidationIndicator("test2", false, false);
        assertEquals("test2", indicator2.getMessage());
        assertFalse(indicator2.isEmailValid());
        assertFalse(indicator2.isPasswordValid());
    }

}