package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Priceable;
import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutableLegacyNetPriceImpl;
import co.smartreceipts.android.model.impl.ImmutableNetPriceImpl;
import co.smartreceipts.android.model.impl.ImmutablePriceImpl;
import co.smartreceipts.android.model.impl.LegacyTripPriceImpl;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * A {@link co.smartreceipts.android.model.Price} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Price} objects
 */
public final class PriceBuilderFactory implements BuilderFactory<Price> {

    private BigDecimal mPriceDecimal;
    private ExchangeRate mExchangeRate;
    private PriceCurrency mCurrency;
    private List<Priceable> mPriceables;
    private List<Price> mPrices;

    public PriceBuilderFactory setPrice(Price price) {
        mPriceDecimal = price.getPrice();
        mCurrency = price.getCurrency();
        mExchangeRate = price.getExchangeRate();
        return this;
    }

    public PriceBuilderFactory setPrice(String price) {
        mPriceDecimal = ModelUtils.tryParse(price);
        return this;
    }

    public PriceBuilderFactory setPrice(double price) {
        mPriceDecimal = new BigDecimal(price);
        return this;
    }

    public PriceBuilderFactory setPrice(BigDecimal price) {
        mPriceDecimal = price;
        return this;
    }

    public PriceBuilderFactory setCurrency(PriceCurrency currency) {
        mCurrency = currency;
        return this;
    }

    public PriceBuilderFactory setCurrency(String currencyCode) {
        mCurrency = PriceCurrency.getInstance(currencyCode);
        return this;
    }

    public PriceBuilderFactory setExchangeRate(ExchangeRate exchangeRate) {
        mExchangeRate = exchangeRate;
        return this;
    }

    public PriceBuilderFactory setPrices(@NonNull List<? extends Price> prices, @Nullable PriceCurrency desiredCurrency) {
        mPrices = new ArrayList<>(prices);
        mCurrency = desiredCurrency;
        return this;
    }

    public PriceBuilderFactory setPriceables(@NonNull List<? extends Priceable> priceables, @Nullable PriceCurrency desiredCurrency) {
        mPriceables = new ArrayList<>(priceables);
        mCurrency = desiredCurrency;
        return this;
    }

    @NonNull
    @Override
    public Price build() {
        if (mPrices != null && !mPrices.isEmpty()) {
            if (mCurrency != null) {
                return new ImmutableNetPriceImpl(mCurrency, mPrices);
            } else {
                return new ImmutableLegacyNetPriceImpl(mPrices);
            }
        }
        else if (mPriceables != null && !mPriceables.isEmpty()) {
            final int size = mPriceables.size();
            final ArrayList<Price> actualPrices = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                actualPrices.add(mPriceables.get(i).getPrice());
            }
            if (mCurrency != null) {
                return new ImmutableNetPriceImpl(mCurrency, actualPrices);
            } else {
                return new ImmutableLegacyNetPriceImpl(actualPrices);
            }
        }
        else {
            final BigDecimal price = mPriceDecimal != null ? mPriceDecimal : new BigDecimal(0);
            if (mCurrency != null) {
                if (mExchangeRate != null) {
                    return new ImmutablePriceImpl(price, mCurrency, mExchangeRate);
                } else {
                    return new ImmutablePriceImpl(price, mCurrency, new ExchangeRateBuilderFactory().setBaseCurrency(mCurrency).build());
                }
            }
            else {
                return new LegacyTripPriceImpl(price, null);
            }
        }
    }

}
