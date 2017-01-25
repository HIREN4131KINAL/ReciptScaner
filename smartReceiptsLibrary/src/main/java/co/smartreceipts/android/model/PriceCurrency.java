package co.smartreceipts.android.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public final class PriceCurrency {

    private Currency currency;
    private String code;

    // Saved to reduce Memory Allocs for heavy calls
    private NumberFormat numberFormat;

    public static final String MISSING_CURRENCY_CODE = "NUL";
    public static final PriceCurrency MISSING_CURRENCY = new PriceCurrency(MISSING_CURRENCY_CODE);

    public static final String MIXED_CURRENCY_CODE = "MIXED";
    public static final PriceCurrency MIXED_CURRENCY = new PriceCurrency(MIXED_CURRENCY_CODE);

    private PriceCurrency(Currency currency) {
        this.currency = currency;
    }

    private PriceCurrency(String code) {
        this.code = code;
    }

    public static PriceCurrency getInstance(String currencyCode) {
        try {
            return new PriceCurrency(Currency.getInstance(currencyCode));
        } catch (IllegalArgumentException e) {
            // If a currency isn't found, just use the 3 letter code
            return new PriceCurrency(currencyCode);
        }
    }

    public static PriceCurrency getDefault() {
        return new PriceCurrency(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
    }

    public final String getCurrencyCode() {
        if (currency != null) {
            return currency.getCurrencyCode();
        }

        else {
            return code;
        }
    }

    public final String format(final String price) {
        return format(stringToBigDecimal(price));
    }

    public final String format(final float price) {
        return format(new BigDecimal(price));
    }

    public final String format(final BigDecimal price) {
        try {
            if (currency != null) {
                if (numberFormat == null) {
                    numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    numberFormat.setCurrency(currency);
                }
                if (price != null) {
                    return numberFormat.format(price.doubleValue());
                } else {
                    return numberFormat.format(new BigDecimal(0));
                }
            } else {
                return code + formatStringAsStrictDecimal(price);
            }
        } catch (java.lang.NumberFormatException e) {
            return "$0.00";
        }
    }


    private BigDecimal stringToBigDecimal(String input) {
        try {
            if (input == null || input.length() == 0)
                return new BigDecimal(0);
            else
                return new BigDecimal(input);
        } catch (NumberFormatException e) {
            return new BigDecimal(0);
        }
    }

    public static final String formatStringAsStrictDecimal(BigDecimal bigDecimal) {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(bigDecimal.doubleValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriceCurrency that = (PriceCurrency) o;

        final String currencyCode = getCurrencyCode();

        if (currencyCode != null ? !currencyCode.equals(that.getCurrencyCode()) : that.getCurrencyCode() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getCurrencyCode() != null ? getCurrencyCode().hashCode() : 0;
    }

}
