package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutableDistanceImpl;

/**
 * A {@link co.smartreceipts.android.model.gson.ExchangeRate} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.gson.ExchangeRate} objects
 */
public final class ExchangeRateBuilderFactory implements BuilderFactory<ExchangeRate> {

    private final Map<String, Double> _rates;
    private String _baseCurrencyCode;

    public ExchangeRateBuilderFactory() {
        _rates = new HashMap<>();
    }

    public ExchangeRateBuilderFactory setBaseCurrency(@NonNull WBCurrency baseCurrency) {
        _baseCurrencyCode = baseCurrency.getCurrencyCode();
        return this;
    }

    public ExchangeRateBuilderFactory setBaseCurrency(@NonNull String baseCurrencyCode) {
        _baseCurrencyCode = baseCurrencyCode;
        return this;
    }

    public ExchangeRateBuilderFactory setRate(@NonNull String currencyCode, double rate) {
        _rates.put(currencyCode, rate);
        return this;
    }

    public ExchangeRateBuilderFactory setRate(@NonNull String currencyCode, @NonNull BigDecimal rate) {
        _rates.put(currencyCode, rate.doubleValue());
        return this;
    }

    public ExchangeRateBuilderFactory setRate(@NonNull WBCurrency currency, double rate) {
        _rates.put(currency.getCurrencyCode(), rate);
        return this;
    }

    public ExchangeRateBuilderFactory setRate(@NonNull WBCurrency currency, @NonNull BigDecimal rate) {
        _rates.put(currency.getCurrencyCode(), rate.doubleValue());
        return this;
    }

    @Override
    @NonNull
    public ExchangeRate build() {
        return new ExchangeRate(_baseCurrencyCode, _rates);
    }
}
