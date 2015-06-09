package co.smartreceipts.android.model.gson;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class ExchangeRate implements Serializable {

    private final String base;
    private final String date;
    private final Map<String, Float> rates;

    public ExchangeRate(String base, String date, Map<String, Float> rates) {
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    @Nullable
    public String getBase() {
        return base;
    }

    @Nullable
    public String getDate() {
        return date;
    }

    @Nullable
    public BigDecimal getExchangeRate(@NonNull String exchangeCurrencyCode) {
        if (rates == null) {
            return null;
        } else {
            if (rates.containsKey(exchangeCurrencyCode)) {
                return new BigDecimal(rates.get(exchangeCurrencyCode));
            } else {
                return null;
            }
        }
    }
}
