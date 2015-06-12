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
 *
 * @author williambaumann
 */
public final class ImmutableNetPriceImpl extends AbstractPriceImpl {

    private final List<Price> mPrices;
    private final Map<WBCurrency, BigDecimal> mCurrencyToPriceMap;
    private final BigDecimal mTotalPrice;
    private final BigDecimal mPossiblyIncorrectTotalPrice;
    private final WBCurrency mCurrency;
    private final ExchangeRate mExchangeRate;

    public ImmutableNetPriceImpl(@NonNull WBCurrency baseCurrency, @NonNull List<Price> prices) {
        mCurrency = baseCurrency;
        mPrices = Collections.unmodifiableList(prices);
        mCurrencyToPriceMap = new HashMap<WBCurrency, BigDecimal>();
        BigDecimal possiblyIncorrectTotalPrice = new BigDecimal(0);
        BigDecimal totalPrice = new BigDecimal(0);
        for (final Price price : prices) {
            if (price.getExchangeRate().supportsExchangeRateFor(baseCurrency)) {
                final BigDecimal priceToAdd = price.getPrice().multiply(price.getExchangeRate().getExchangeRate(baseCurrency));
                totalPrice = totalPrice.add(priceToAdd);
                possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(priceToAdd);
            } else {
                // Let's create a map of all the currencies we have
                possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(price.getPrice());
                final BigDecimal priceToAdd = mCurrencyToPriceMap.containsKey(price.getCurrency()) ? mCurrencyToPriceMap.get(price.getCurrency()).add(price.getPrice()) : price.getPrice();
                mCurrencyToPriceMap.put(price.getCurrency(), priceToAdd);
            }
        }
        mTotalPrice = totalPrice;
        mPossiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice;
        mExchangeRate = new ExchangeRateBuilderFactory().setBaseCurrency(baseCurrency).build();
    }

    private ImmutableNetPriceImpl(@NonNull Parcel in) {
        this(WBCurrency.getInstance(in.readString()), restorePricesFromParcel(in));
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
        if (mCurrencyToPriceMap.isEmpty()) {
            return mTotalPrice.floatValue();
        } else {
            return mPossiblyIncorrectTotalPrice.floatValue();
        }
    }

    @NonNull
    @Override
    public BigDecimal getPrice() {
        if (mCurrencyToPriceMap.isEmpty()) {
            return mTotalPrice;
        } else {
            return mPossiblyIncorrectTotalPrice;
        }
    }

    @NonNull
    @Override
    public String getDecimalFormattedPrice() {
        if (mCurrencyToPriceMap.isEmpty()) {
            return ModelUtils.getDecimalFormattedValue(mTotalPrice);
        } else {
            return ModelUtils.getDecimalFormattedValue(mPossiblyIncorrectTotalPrice);
        }
    }

    @NonNull
    @Override
    public String getCurrencyFormattedPrice() {
        if (mCurrencyToPriceMap.isEmpty()) {
            return ModelUtils.getCurrencyFormattedValue(mTotalPrice, mCurrency);
        } else {
            final List<String> currencyStrings = new ArrayList<String>();
            for (WBCurrency currency : mCurrencyToPriceMap.keySet()) {
                currencyStrings.add(ModelUtils.getCurrencyFormattedValue(mCurrencyToPriceMap.get(currency), currency));
            }
            return TextUtils.join("; ", currencyStrings);
        }
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        if (mCurrencyToPriceMap.isEmpty()) {
            return ModelUtils.getCurrencyCodeFormattedValue(mTotalPrice, mCurrency);
        } else {
            final List<String> currencyStrings = new ArrayList<String>();
            for (WBCurrency currency : mCurrencyToPriceMap.keySet()) {
                currencyStrings.add(ModelUtils.getCurrencyCodeFormattedValue(mCurrencyToPriceMap.get(currency), currency));
            }
            return TextUtils.join("; ", currencyStrings);
        }
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
        dest.writeString(mCurrency.getCurrencyCode());
        dest.writeInt(mPrices.size());
        for (Price price : mPrices) {
            dest.writeParcelable(price, 0);
        }
    }


    public static final Creator<ImmutableNetPriceImpl> CREATOR = new Creator<ImmutableNetPriceImpl>() {
        public ImmutableNetPriceImpl createFromParcel(Parcel source) {
            return new ImmutableNetPriceImpl(source);
        }

        public ImmutableNetPriceImpl[] newArray(int size) {
            return new ImmutableNetPriceImpl[size];
        }
    };
}
