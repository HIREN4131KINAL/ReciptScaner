package co.smartreceipts.android.settings.catalog;

import android.support.annotation.NonNull;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import co.smartreceipts.android.R;

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
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.DefaultTaxPercentage));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.PredictCategories));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.EnableAutoCompleteSuggestions));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.OnlyIncludeReimbursable));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.ReceiptsDefaultAsReimbursable));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.MatchReceiptNameToCategory));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.MatchReceiptCommentToCategory));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.ShowReceiptID));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.IncludeTaxField));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.UsePreTaxPrice));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.DefaultToFullPage));
        Assert.assertTrue(userPreferences.contains(UserPreference.Receipts.UsePaymentMethods));
        Assert.assertTrue(userPreferences.contains(UserPreference.ReportOutput.UserId));
        Assert.assertTrue(userPreferences.contains(UserPreference.ReportOutput.IncludeCsvHeaders));
        Assert.assertTrue(userPreferences.contains(UserPreference.ReportOutput.PrintUserIdByPdfPhoto));
        Assert.assertTrue(userPreferences.contains(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto));
        Assert.assertTrue(userPreferences.contains(UserPreference.ReportOutput.PrintReceiptsTableInLandscape));
        Assert.assertTrue(userPreferences.contains(UserPreference.Email.ToAddresses));
        Assert.assertTrue(userPreferences.contains(UserPreference.Email.CcAddresses));
        Assert.assertTrue(userPreferences.contains(UserPreference.Email.BccAddresses));
        Assert.assertTrue(userPreferences.contains(UserPreference.Email.Subject));
        Assert.assertTrue(userPreferences.contains(UserPreference.Camera.UseNativeCamera));
        Assert.assertTrue(userPreferences.contains(UserPreference.Camera.SaveImagesInGrayScale));
        Assert.assertTrue(userPreferences.contains(UserPreference.Camera.AutomaticallyRotateImages));
    }

    @Test
    public void general() {
        Assert.assertEquals(UserPreference.General.DefaultReportDuration.getType(), Integer.class);
        Assert.assertEquals(name(UserPreference.General.DefaultReportDuration), "TripDuration");
        Assert.assertEquals(UserPreference.General.DefaultReportDuration.getDefaultValue(), R.integer.pref_general_trip_duration_defaultValue);

        Assert.assertEquals(UserPreference.General.DefaultCurrency.getType(), String.class);
        Assert.assertEquals(name(UserPreference.General.DefaultCurrency), "isocurr");
        Assert.assertEquals(UserPreference.General.DefaultCurrency.getDefaultValue(), R.string.pref_general_default_currency_defaultValue);

        Assert.assertEquals(UserPreference.General.DateSeparator.getType(), String.class);
        Assert.assertEquals(name(UserPreference.General.DateSeparator), "dateseparator");
        Assert.assertEquals(UserPreference.General.DateSeparator.getDefaultValue(), R.string.pref_general_default_date_separator_defaultValue);

        Assert.assertEquals(UserPreference.General.IncludeCostCenter.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.General.IncludeCostCenter), "trackcostcenter");
        Assert.assertEquals(UserPreference.General.IncludeCostCenter.getDefaultValue(), R.bool.pref_general_track_cost_center_defaultValue);
    }

    @Test
    public void receipts() {
        Assert.assertEquals(UserPreference.Receipts.MinimumReceiptPrice.getType(), Float.class);
        Assert.assertEquals(name(UserPreference.Receipts.MinimumReceiptPrice), "MinReceiptPrice");
        Assert.assertEquals(UserPreference.Receipts.MinimumReceiptPrice.getDefaultValue(), UserPreference.UNKNOWN_RES);

        Assert.assertEquals(UserPreference.Receipts.DefaultTaxPercentage.getType(), Float.class);
        Assert.assertEquals(name(UserPreference.Receipts.DefaultTaxPercentage), "TaxPercentage");
        Assert.assertEquals(UserPreference.Receipts.DefaultTaxPercentage.getDefaultValue(), R.dimen.pref_receipt_tax_percent_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.PredictCategories.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.PredictCategories), "PredictCats");
        Assert.assertEquals(UserPreference.Receipts.PredictCategories.getDefaultValue(), R.bool.pref_receipt_predict_categories_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.EnableAutoCompleteSuggestions.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.EnableAutoCompleteSuggestions), "EnableAutoCompleteSuggestions");
        Assert.assertEquals(UserPreference.Receipts.EnableAutoCompleteSuggestions.getDefaultValue(), R.bool.pref_receipt_enable_autocomplete_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.OnlyIncludeReimbursable.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.OnlyIncludeReimbursable), "OnlyIncludeExpensable");
        Assert.assertEquals(UserPreference.Receipts.OnlyIncludeReimbursable.getDefaultValue(), R.bool.pref_receipt_reimbursable_only_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.ReceiptsDefaultAsReimbursable.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.ReceiptsDefaultAsReimbursable), "ExpensableDefault");
        Assert.assertEquals(UserPreference.Receipts.ReceiptsDefaultAsReimbursable.getDefaultValue(), R.bool.pref_receipt_reimbursable_default_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate), "DefaultToFirstReportDate");
        Assert.assertEquals(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate.getDefaultValue(), R.bool.pref_receipt_default_to_report_start_date_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.MatchReceiptNameToCategory.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.MatchReceiptNameToCategory), "MatchNameCats");
        Assert.assertEquals(UserPreference.Receipts.MatchReceiptNameToCategory.getDefaultValue(), R.bool.pref_receipt_match_name_to_category_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.MatchReceiptCommentToCategory.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.MatchReceiptCommentToCategory), "MatchCommentCats");
        Assert.assertEquals(UserPreference.Receipts.MatchReceiptCommentToCategory.getDefaultValue(), R.bool.pref_receipt_match_comment_to_category_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.ShowReceiptID.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.ShowReceiptID), "ShowReceiptID");
        Assert.assertEquals(UserPreference.Receipts.ShowReceiptID.getDefaultValue(), R.bool.pref_receipt_show_id_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.IncludeTaxField.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.IncludeTaxField), "IncludeTaxField");
        Assert.assertEquals(UserPreference.Receipts.IncludeTaxField.getDefaultValue(), R.bool.pref_receipt_include_tax_field_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.UsePreTaxPrice.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.UsePreTaxPrice), "PreTax");
        Assert.assertEquals(UserPreference.Receipts.UsePreTaxPrice.getDefaultValue(), R.bool.pref_receipt_pre_tax_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.DefaultToFullPage.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.DefaultToFullPage), "UseFullPage");
        Assert.assertEquals(UserPreference.Receipts.DefaultToFullPage.getDefaultValue(), R.bool.pref_receipt_full_page_defaultValue);

        Assert.assertEquals(UserPreference.Receipts.UsePaymentMethods.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Receipts.UsePaymentMethods), "UsePaymentMethods");
        Assert.assertEquals(UserPreference.Receipts.UsePaymentMethods.getDefaultValue(), R.bool.pref_receipt_use_payment_methods_defaultValue);
    }

    @Test
    public void reportOutput() {
        Assert.assertEquals(UserPreference.ReportOutput.UserId.getType(), String.class);
        Assert.assertEquals(name(UserPreference.ReportOutput.UserId), "UserName");
        Assert.assertEquals(UserPreference.ReportOutput.UserId.getDefaultValue(), R.string.pref_output_username_defaultValue);

        Assert.assertEquals(UserPreference.ReportOutput.IncludeCsvHeaders.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.ReportOutput.IncludeCsvHeaders), "IncludeCSVHeaders");
        Assert.assertEquals(UserPreference.ReportOutput.IncludeCsvHeaders.getDefaultValue(), R.bool.pref_output_csv_header_defaultValue);

        Assert.assertEquals(UserPreference.ReportOutput.PrintUserIdByPdfPhoto.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.ReportOutput.PrintUserIdByPdfPhoto), "PrintByIDPhotoKey");
        Assert.assertEquals(UserPreference.ReportOutput.PrintUserIdByPdfPhoto.getDefaultValue(), R.bool.pref_output_print_receipt_id_by_photo_defaultValue);

        Assert.assertEquals(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto), "PrintCommentByPhoto");
        Assert.assertEquals(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto.getDefaultValue(), R.bool.pref_output_print_receipt_comment_by_photo_defaultValue);

        Assert.assertEquals(UserPreference.ReportOutput.PrintReceiptsTableInLandscape.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.ReportOutput.PrintReceiptsTableInLandscape), "ReceiptsTableLandscape");
        Assert.assertEquals(UserPreference.ReportOutput.PrintReceiptsTableInLandscape.getDefaultValue(), R.bool.pref_output_receipts_landscape_defaultValue);
    }

    @Test
    public void email() {
        Assert.assertEquals(UserPreference.Email.ToAddresses.getType(), String.class);
        Assert.assertEquals(name(UserPreference.Email.ToAddresses), "EmailTo");
        Assert.assertEquals(UserPreference.Email.ToAddresses.getDefaultValue(), R.string.pref_email_default_email_to_defaultValue);

        Assert.assertEquals(UserPreference.Email.CcAddresses.getType(), String.class);
        Assert.assertEquals(name(UserPreference.Email.CcAddresses), "EmailCC");
        Assert.assertEquals(UserPreference.Email.CcAddresses.getDefaultValue(), R.string.pref_email_default_email_cc_defaultValue);

        Assert.assertEquals(UserPreference.Email.BccAddresses.getType(), String.class);
        Assert.assertEquals(name(UserPreference.Email.BccAddresses), "EmailBCC");
        Assert.assertEquals(UserPreference.Email.BccAddresses.getDefaultValue(), R.string.pref_email_default_email_bcc_defaultValue);

        Assert.assertEquals(UserPreference.Email.Subject.getType(), String.class);
        Assert.assertEquals(name(UserPreference.Email.Subject), "EmailSubject");
        Assert.assertEquals(UserPreference.Email.Subject.getDefaultValue(), R.string.EMAIL_DATA_SUBJECT);
    }

    @Test
    public void camera() {
        Assert.assertEquals(UserPreference.Camera.UseNativeCamera.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Camera.UseNativeCamera), "UseNativeCamera");
        Assert.assertEquals(UserPreference.Camera.UseNativeCamera.getDefaultValue(), R.bool.pref_camera_use_native_camera_defaultValue);

        Assert.assertEquals(UserPreference.Camera.SaveImagesInGrayScale.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Camera.SaveImagesInGrayScale), "SaveBW");
        Assert.assertEquals(UserPreference.Camera.SaveImagesInGrayScale.getDefaultValue(), R.bool.pref_camera_bw_defaultValue);

        Assert.assertEquals(UserPreference.Camera.AutomaticallyRotateImages.getType(), Boolean.class);
        Assert.assertEquals(name(UserPreference.Camera.AutomaticallyRotateImages), "Camera_Rotate");
        Assert.assertEquals(UserPreference.Camera.AutomaticallyRotateImages.getDefaultValue(), R.bool.pref_camera_rotate_defaultValue);
    }

    @NonNull
    private String name(@NonNull UserPreference<?> userPreference) {
        return RuntimeEnvironment.application.getString(userPreference.getName());
    }

}