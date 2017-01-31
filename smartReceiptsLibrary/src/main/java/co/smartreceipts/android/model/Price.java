package co.smartreceipts.android.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

import co.smartreceipts.android.model.gson.ExchangeRate;

/**
 * Defines a contract from which we can track the price value
 */
public interface Price extends Parcelable {

    int PRECISION = 5;

    /**
     * Gets the float representation of this price
     *
     * @return the float primitive, which represents the total price of this receipt
     */
    float getPriceAsFloat();

    /**
     * Gets the {@link java.math.BigDecimal} representation of this price
     *
     * @return the {@link java.math.BigDecimal} representation of this price
     */
    @NonNull
    BigDecimal getPrice();

    /**
     * A "decimal-formatted" price, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     *
     * @return the decimal formatted price {@link java.lang.String}
     */
    @NonNull
    String getDecimalFormattedPrice();

    /**
     * The "currency-formatted" price, which would appear as "$25.20" or "$25,20" as determined by the user's locale
     *
     * @return - the currency formatted price {@link java.lang.String}
     */
    @NonNull
    String getCurrencyFormattedPrice();

    /**
     * The "currency-code-formatted" price, which would appear as "USD25.20" or "USD25,20" as determined by the user's locale
     *
     * @return - the currency formatted price {@link java.lang.String}
     */
    @NonNull
    String getCurrencyCodeFormattedPrice();

    /**
     * Gets the currency which this price is tracked in
     *
     * @return - the {@link PriceCurrency} currency representation
     */
    @NonNull
    PriceCurrency getCurrency();

    /**
     * Gets the currency code representation for this price or {@link PriceCurrency#MISSING_CURRENCY_CODE}
     * if it cannot be found
     *
     * @return the currency code {@link java.lang.String} for this price
     */
    @NonNull
    String getCurrencyCode();

    /**
     * Gets the exchange rate associated with this particular price object, which we can use to attempt to convert this
     * price from one currency to another
     *
     * @return the {@link co.smartreceipts.android.model.gson.ExchangeRate}
     */
    @NonNull
    ExchangeRate getExchangeRate();
}
