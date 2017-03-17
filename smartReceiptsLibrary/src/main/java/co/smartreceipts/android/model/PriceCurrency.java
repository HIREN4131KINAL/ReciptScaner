package co.smartreceipts.android.model;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.utils.log.Logger;

public final class PriceCurrency {

    @Deprecated
    public static final PriceCurrency MISSING_CURRENCY = new PriceCurrency("NUL");
    
    @Deprecated
    public static final PriceCurrency MIXED_CURRENCY = new PriceCurrency("MIXED");

    private static final Map<String, PriceCurrency> sCurrencyMap = new ConcurrentHashMap<>();

    private final String mCurrencyCode;
    private Currency mCurrency;

    // Saved to reduce Memory Allocs for heavy calls
    private NumberFormat numberFormat;

    @NonNull
    public static PriceCurrency getInstance(@NonNull String currencyCode) {
        // Note: I'm not concerned if we have a few duplicate entries (ie this isn't fully thread safe) as the objects are all equal
        PriceCurrency priceCurrency = sCurrencyMap.get(currencyCode);
        if (priceCurrency != null) {
            return priceCurrency;
        } else {
            priceCurrency = new PriceCurrency(currencyCode);
            sCurrencyMap.put(currencyCode, priceCurrency);
            return priceCurrency;
        }
    }

    @NonNull
    public static PriceCurrency getDefaultCurrency() {
        return PriceCurrency.getInstance(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
    }

    private PriceCurrency(@NonNull String currencyCode) {
        this.mCurrencyCode = Preconditions.checkNotNull(currencyCode);
        try {
            mCurrency = Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            Logger.warn(this, "Unknown system currency code requested: {}. Handling this internally", currencyCode);
        }
    }

    @NonNull
    public final String getCurrencyCode() {
        if (mCurrency != null) {
            return mCurrency.getCurrencyCode();
        } else {
            return mCurrencyCode;
        }
    }

    @NonNull
    public final String format(@NonNull BigDecimal price) {
        try {
            if (mCurrency != null) {
                if (numberFormat == null) {
                    // Just in time allocation for this member variable
                    numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    numberFormat.setCurrency(mCurrency);
                }
                return numberFormat.format(price.doubleValue());
            } else {
                return mCurrencyCode + ModelUtils.getDecimalFormattedValue(price);
            }
        } catch (java.lang.NumberFormatException e) {
            return "$0.00";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceCurrency)) return false;

        PriceCurrency that = (PriceCurrency) o;

        return mCurrencyCode.equals(that.mCurrencyCode);

    }

    @Override
    public int hashCode() {
        return mCurrencyCode.hashCode();
    }
}
