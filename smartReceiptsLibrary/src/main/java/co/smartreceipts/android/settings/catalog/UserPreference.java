package co.smartreceipts.android.settings.catalog;

import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;

public final class UserPreference<T> {

    public static final int UNKNOWN_RES = -1;

    public static final class General {
        public static final UserPreference<Integer> DefaultReportDuration = new UserPreference<>(Integer.class, R.string.pref_general_trip_duration_key, R.integer.pref_general_trip_duration_defaultValue);
        public static final UserPreference<String> DefaultCurrency = new UserPreference<>(String.class, R.string.pref_general_default_currency_key, R.string.pref_general_default_currency_defaultValue);
        public static final UserPreference<String> DateSeparator = new UserPreference<>(String.class, R.string.pref_general_default_date_separator_key, R.string.pref_general_default_date_separator_defaultValue);
        public static final UserPreference<Boolean> IncludeCostCenter = new UserPreference<>(Boolean.class, R.string.pref_general_track_cost_center_key, R.bool.pref_general_track_cost_center_defaultValue);
    }

    public static final class Receipts {
        public static final UserPreference<Float> MinimumReceiptPrice = new UserPreference<>(Float.class, R.string.pref_receipt_minimum_receipts_price_key);
        public static final UserPreference<Float> DefaultTaxPercentage = new UserPreference<>(Float.class, R.string.pref_receipt_tax_percent_key, R.dimen.pref_receipt_tax_percent_defaultValue);
        public static final UserPreference<Boolean> PredictCategories = new UserPreference<>(Boolean.class, R.string.pref_receipt_predict_categories_key, R.bool.pref_receipt_predict_categories_defaultValue);
        public static final UserPreference<Boolean> EnableAutoCompleteSuggestions = new UserPreference<>(Boolean.class, R.string.pref_receipt_enable_autocomplete_key, R.bool.pref_receipt_enable_autocomplete_defaultValue);
        public static final UserPreference<Boolean> OnlyIncludeReimbursable = new UserPreference<>(Boolean.class, R.string.pref_receipt_reimbursable_only_key, R.bool.pref_receipt_reimbursable_only_defaultValue);
        public static final UserPreference<Boolean> ReceiptsDefaultAsReimbursable = new UserPreference<>(Boolean.class, R.string.pref_receipt_reimbursable_default_key, R.bool.pref_receipt_reimbursable_default_defaultValue);
        public static final UserPreference<Boolean> ReceiptDateDefaultsToReportStartDate = new UserPreference<>(Boolean.class, R.string.pref_receipt_default_to_report_start_date_key, R.bool.pref_receipt_default_to_report_start_date_defaultValue);
        public static final UserPreference<Boolean> MatchReceiptNameToCategory = new UserPreference<>(Boolean.class, R.string.pref_receipt_match_name_to_category_key, R.bool.pref_receipt_match_name_to_category_defaultValue);
        public static final UserPreference<Boolean> MatchReceiptCommentToCategory = new UserPreference<>(Boolean.class, R.string.pref_receipt_match_comment_to_category_key, R.bool.pref_receipt_match_comment_to_category_defaultValue);
        public static final UserPreference<Boolean> ShowReceiptID = new UserPreference<>(Boolean.class, R.string.pref_receipt_show_id_key, R.bool.pref_receipt_show_id_defaultValue);
        public static final UserPreference<Boolean> IncludeTaxField = new UserPreference<>(Boolean.class, R.string.pref_receipt_include_tax_field_key, R.bool.pref_receipt_include_tax_field_defaultValue);
        public static final UserPreference<Boolean> UsePreTaxPrice = new UserPreference<>(Boolean.class, R.string.pref_receipt_pre_tax_key, R.bool.pref_receipt_pre_tax_defaultValue);
        public static final UserPreference<Boolean> DefaultToFullPage = new UserPreference<>(Boolean.class, R.string.pref_receipt_full_page_key, R.bool.pref_receipt_full_page_defaultValue);
        public static final UserPreference<Boolean> UsePaymentMethods = new UserPreference<>(Boolean.class, R.string.pref_receipt_use_payment_methods_key, R.bool.pref_receipt_use_payment_methods_defaultValue);
    }

    public static final class ReportOutput {
        public static final UserPreference<String> UserId = new UserPreference<>(String.class, R.string.pref_output_username_key, R.string.pref_output_username_defaultValue);
        public static final UserPreference<Boolean> IncludeCsvHeaders = new UserPreference<>(Boolean.class, R.string.pref_output_csv_header_key, R.bool.pref_output_csv_header_defaultValue);
        public static final UserPreference<Boolean> PrintUserIdByPdfPhoto = new UserPreference<>(Boolean.class, R.string.pref_output_print_receipt_id_by_photo_key, R.bool.pref_output_print_receipt_id_by_photo_defaultValue);
        public static final UserPreference<Boolean> PrintReceiptCommentByPdfPhoto = new UserPreference<>(Boolean.class, R.string.pref_output_print_receipt_comment_by_photo_key, R.bool.pref_output_print_receipt_comment_by_photo_defaultValue);
        public static final UserPreference<Boolean> PrintReceiptsTableInLandscape = new UserPreference<>(Boolean.class, R.string.pref_output_receipts_landscape_key, R.bool.pref_output_receipts_landscape_defaultValue);
    }

    private static List<UserPreference<?>> CACHED_VALUES;

    private final Class<T> type;
    private final int name;
    private final int defaultValue;

    private UserPreference(@NonNull Class<T> type, @StringRes int name) {
        this(type, name, UNKNOWN_RES);
    }

    private UserPreference(@NonNull Class<T> type, @StringRes int name, @AnyRes int defaultValue) {
        this.type = Preconditions.checkNotNull(type);
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @NonNull
    public Class<T> getType() {
        return type;
    }

    @StringRes
    public int getName() {
        return name;
    }

    @AnyRes
    public int getDefaultValue() {
        return defaultValue;
    }

    @NonNull
    public static synchronized List<UserPreference<?>> values() {
        if (CACHED_VALUES == null) {
            CACHED_VALUES = new ArrayList<>();
            final Class<?>[] declaredClasses = UserPreference.class.getDeclaredClasses();
            if (declaredClasses != null) {
                for (final Class<?> declaredClass : declaredClasses) {
                    final Field[] fields = declaredClass.getFields();
                    if (fields != null) {
                        for (final Field field : fields) {
                            if (UserPreference.class.equals(field.getType())) {
                                try {
                                    CACHED_VALUES.add((UserPreference<?>) field.get(declaredClass));
                                } catch (IllegalAccessException e) {
                                    Logger.warn(UserPreference.class, "Failed to get field " + field + " due to access exception", e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return CACHED_VALUES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPreference)) return false;

        UserPreference<?> that = (UserPreference<?>) o;

        if (name != that.name) return false;
        if (defaultValue != that.defaultValue) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name;
        result = 31 * result + defaultValue;
        return result;
    }
}
