package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Priceable;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.impl.ImmutableNetPriceImpl;
import co.smartreceipts.android.model.impl.ImmutablePriceImpl;
import co.smartreceipts.android.model.impl.LegacyTripPriceImpl;

/**
 * A {@link co.smartreceipts.android.model.Price} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Price} objects
 */
public class PriceBuilderFactory implements BuilderFactory<Price> {

    private BigDecimal mPrice;
    private WBCurrency mCurrency;
    private List<Priceable> mPriceables;

    public PriceBuilderFactory setPrice(String price) {
        mPrice = tryParse(price);
        return this;
    }

    public PriceBuilderFactory setPrice(double price) {
        mPrice = new BigDecimal(price);
        return this;
    }

    public PriceBuilderFactory setPrice(BigDecimal price) {
        mPrice = price;
        return this;
    }

    public PriceBuilderFactory setCurrency(WBCurrency currency) {
        mCurrency = currency;
        return this;
    }

    public PriceBuilderFactory setCurrency(String currencyCode) {
        mCurrency = WBCurrency.getInstance(currencyCode);
        return this;
    }

    public PriceBuilderFactory setPriceables(List<Priceable> prices) {
        mPriceables = new ArrayList<Priceable>(prices);
        return this;
    }

    @NonNull
    @Override
    public Price build() {
        if (mPriceables != null) {
            final int size = mPriceables.size();
            final ArrayList<Price> actualPrices = new ArrayList<Price>(size);
            for (int i = 0; i < size; i++) {
                actualPrices.add(mPriceables.get(i).getPrice());
            }
            return new ImmutableNetPriceImpl(actualPrices);
        }
        else if (mPrice != null) {
            if (mCurrency != null) {
                return new ImmutablePriceImpl(mPrice, mCurrency);
            }
            else {
                return new LegacyTripPriceImpl(mPrice, mCurrency);
            }
        }
        else {
            throw new IllegalArgumentException("Not enough arguments have been supplied to create a price");
        }
    }

    private BigDecimal tryParse(String number) {
        if (TextUtils.isEmpty(number)) {
            return new BigDecimal(0);
        }
        try {
            return new BigDecimal(number.replace(",", "."));
        } catch (NumberFormatException e) {
            return new BigDecimal(0);
        }
    }
}
