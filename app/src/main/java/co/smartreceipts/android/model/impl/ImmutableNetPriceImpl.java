package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.PriceCurrency;
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

    private static final int ROUNDING_PRECISION = PRECISION + 2;

    private final List<Price> mPrices;
    private final Map<PriceCurrency, BigDecimal> mCurrencyToPriceMap;
    private final BigDecimal mTotalPrice;
    private final BigDecimal mPossiblyIncorrectTotalPrice;
    private final PriceCurrency mCurrency;
    private final ExchangeRate mExchangeRate;
    private final boolean mAreAllExchangeRatesValid;

    public ImmutableNetPriceImpl(@NonNull PriceCurrency baseCurrency, @NonNull List<Price> prices) {
        mCurrency = baseCurrency;
        mPrices = Collections.unmodifiableList(prices);
        mCurrencyToPriceMap = new HashMap<>();
        BigDecimal possiblyIncorrectTotalPrice = new BigDecimal(0);
        BigDecimal totalPrice = new BigDecimal(0);
        boolean areAllExchangeRatesValid = true;
        for (final Price price : prices) {
            final BigDecimal priceToAdd;
            final PriceCurrency currencyForPriceToAdd;
            if (price.getExchangeRate().supportsExchangeRateFor(baseCurrency)) {
                priceToAdd = price.getPrice().multiply(price.getExchangeRate().getExchangeRate(baseCurrency));
                totalPrice = totalPrice.add(priceToAdd);
                currencyForPriceToAdd = baseCurrency;

            } else {
                // If not, let's just hope for the best with whatever we have to add
                priceToAdd = price.getPrice();
                currencyForPriceToAdd = price.getCurrency();
                areAllExchangeRatesValid = false;
            }
            possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(priceToAdd);
            final BigDecimal priceForCurrency = mCurrencyToPriceMap.containsKey(currencyForPriceToAdd) ? mCurrencyToPriceMap.get(currencyForPriceToAdd).add(priceToAdd) : priceToAdd;
            mCurrencyToPriceMap.put(currencyForPriceToAdd, priceForCurrency);
        }
        mTotalPrice = totalPrice.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        mPossiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        mAreAllExchangeRatesValid = areAllExchangeRatesValid;
        mExchangeRate = new ExchangeRateBuilderFactory().setBaseCurrency(baseCurrency).build();
    }

    private ImmutableNetPriceImpl(@NonNull Parcel in) {
        this(PriceCurrency.getInstance(in.readString()), restorePricesFromParcel(in));
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
        if (mAreAllExchangeRatesValid) {
            return mTotalPrice.floatValue();
        } else {
            return mPossiblyIncorrectTotalPrice.floatValue();
        }
    }

    @NonNull
    @Override
    public BigDecimal getPrice() {
        if (mAreAllExchangeRatesValid) {
            return mTotalPrice;
        } else {
            return mPossiblyIncorrectTotalPrice;
        }
    }

    @NonNull
    @Override
    public String getDecimalFormattedPrice() {
        if (mAreAllExchangeRatesValid) {
            return ModelUtils.getDecimalFormattedValue(mTotalPrice);
        } else {
            return ModelUtils.getDecimalFormattedValue(mPossiblyIncorrectTotalPrice);
        }
    }

    @NonNull
    @Override
    public String getCurrencyFormattedPrice() {
        if (mAreAllExchangeRatesValid) {
            return ModelUtils.getCurrencyFormattedValue(mTotalPrice, mCurrency);
        } else {
            final List<String> currencyStrings = new ArrayList<String>();
            for (PriceCurrency currency : mCurrencyToPriceMap.keySet()) {
                currencyStrings.add(ModelUtils.getCurrencyFormattedValue(mCurrencyToPriceMap.get(currency), currency));
            }
            return TextUtils.join("; ", currencyStrings);
        }
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        if (mAreAllExchangeRatesValid) {
            return ModelUtils.getCurrencyCodeFormattedValue(mTotalPrice, mCurrency);
        } else {
            final List<String> currencyStrings = new ArrayList<String>();
            for (PriceCurrency currency : mCurrencyToPriceMap.keySet()) {
                currencyStrings.add(ModelUtils.getCurrencyCodeFormattedValue(mCurrencyToPriceMap.get(currency), currency));
            }
            return TextUtils.join("; ", currencyStrings);
        }
    }

    @NonNull
    @Override
    public PriceCurrency getCurrency() {
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

    public boolean areAllExchangeRatesValid() {
        // TODO: Figure out how to expose this better
        return mAreAllExchangeRatesValid;
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
