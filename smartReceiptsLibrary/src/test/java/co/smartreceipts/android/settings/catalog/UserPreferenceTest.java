package co.smartreceipts.android.settings.catalog;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class UserPreferenceTest {

    @Test
    public void values() {
        final List<UserPreference<?>> userPreferences = UserPreference.values();

        Assert.assertNotNull(userPreferences);
        Assert.assertFalse(userPreferences.isEmpty());
        Assert.assertTrue(userPreferences.contains(UserPreference.General.DefaultReportDuration));
        Assert.assertTrue(userPreferences.contains(UserPreference.General.DefaultCurrency));
        Assert.assertTrue(userPreferences.contains(UserPreference.General.DateSeparator));
        Assert.assertTrue(userPreferences.contains(UserPreference.General.IncludeCostCenter));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.MinimumReceiptPrice));
    }

}