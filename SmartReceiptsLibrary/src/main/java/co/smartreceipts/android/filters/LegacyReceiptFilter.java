package co.smartreceipts.android.filters;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;

/**
 * Defines a default filter implementation that is based on the legacy approach to filtering (i.e. settings)
 */
public final class LegacyReceiptFilter extends AndFilter<Receipt> {

    public LegacyReceiptFilter(@NonNull Preferences preferences) {
        and(new LegacyReceiptMinimumPriceFilter(preferences.getMinimumReceiptPriceToIncludeInReports()));
        if (preferences.onlyIncludeExpensableReceiptsInReports()) {
            and(new ReceiptIsExpensableFilter());
        }
    }

    @Override
    Filter<Receipt> getFilter(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("This method is not supported by this class");
    }
}
