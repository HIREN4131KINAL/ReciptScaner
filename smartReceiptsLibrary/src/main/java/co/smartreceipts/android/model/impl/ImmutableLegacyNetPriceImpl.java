package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.Price} interface
 * for a collection of other price objects.
 * <p/>
 * TODO: Eventually normally for a single currency. Very hacky now
 *
 * @author williambaumann
 */
public final class ImmutableLegacyNetPriceImpl extends AbstractPriceImpl {

    private final List<Price> mPrices;
    private final Map<WBCurrency, BigDecimal> mCurrencyToPriceMap;
    private final BigDecimal mPossiblyIncorrectTotalPrice;
    private final WBCurrency mCurrency;
    private final ExchangeRate mExchangeRate;

    public ImmutableLegacyNetPriceImpl(@NonNull List<Price> prices) {
        mPrices = Collections.unmodifiableList(prices);
        mCurrencyToPriceMap = new HashMap<>();
        BigDecimal possiblyIncorrectTotalPrice = new BigDecimal(0);
        WBCurrency currency = null;
        for (final Price price : prices) {
            possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(price.getPrice());
            final BigDecimal priceToAdd = mCurrencyToPriceMap.containsKey(price.getCurrency()) ? mCurrencyToPriceMap.get(price.getCurrency()).add(price.getPrice()) : price.getPrice();
            mCurrencyToPriceMap.put(price.getCurrency(), priceToAdd);
            if (currency == null) {
                currency = price.getCurrency();
            } else if (!currency.equals(price.getCurrency())) {
                currency = WBCurrency.MIXED_CURRENCY; // Mark as fixed if multiple
            }
        }
        mCurrency = currency;
        mPossiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice;
        final ExchangeRateBuilderFactory builder = new ExchangeRateBuilderFactory();
        if (mCurrency != null) {
            builder.setBaseCurrency(mCurrency);
        }
        mExchangeRate = builder.build();
    }

    private ImmutableLegacyNetPriceImpl(@NonNull Parcel in) {
        this(restorePricesFromParcel(in));
    }

    private static List<Price> restorePricesFromParcel(Parcel in) {
        final int size = in.readInt();
        final List<Price> prices = new ArrayList<Price>(size);
        for (int i = 0; i < size; i++) {
            final Price price = in.readParcelable(Price.class.getClassLoader());
            prices.add(price);
        }
        return prices;
    }

    @Override
    public float getPriceAsFloat() {
        return mPossiblyIncorrectTotalPrice.floatValue();
    }

    @NonNull
    @Override
    public BigDecimal getPrice() {
        return mPossiblyIncorrectTotalPrice;
    }

    @NonNull
    @Override
    public String getDecimalFormattedPrice() {
        return ModelUtils.getDecimalFormattedValue(mPossiblyIncorrectTotalPrice);
    }

    @NonNull
    @Override
    public String getCurrencyFormattedPrice() {
        final List<String> currencyStrings = new ArrayList<String>();
        for (WBCurrency currency : mCurrencyToPriceMap.keySet()) {
            currencyStrings.add(ModelUtils.getCurrencyFormattedValue(mCurrencyToPriceMap.get(currency), currency));
        }
        return TextUtils.join("; ", currencyStrings);
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        final List<String> currencyStrings = new ArrayList<String>();
        for (WBCurrency currency : mCurrencyToPriceMap.keySet()) {
            currencyStrings.add(ModelUtils.getCurrencyCodeFormattedValue(mCurrencyToPriceMap.get(currency), currency));
        }
        return TextUtils.join("; ", currencyStrings);
    }

    @NonNull
    @Override
    public WBCurrency getCurrency() {
        return mCurrency;
    }

    @NonNull
    @Override
    public String getCurrencyCode() {
        return mCurrency.getCurrencyCode();
    }

    @NonNull
    @Override
    public ExchangeRate getExchangeRate() {
        return mExchangeRate;
    }

    @Override
    public String toString() {
        return getCurrencyFormattedPrice();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPrices.size());
        for (Price price : mPrices) {
            dest.writeParcelable(price, 0);
        }
    }


    public static final Creator<ImmutableLegacyNetPriceImpl> CREATOR = new Creator<ImmutableLegacyNetPriceImpl>() {
        public ImmutableLegacyNetPriceImpl createFromParcel(Parcel source) {
            return new ImmutableLegacyNetPriceImpl(source);
        }

        public ImmutableLegacyNetPriceImpl[] newArray(int size) {
            return new ImmutableLegacyNetPriceImpl[size];
        }
    };
}
