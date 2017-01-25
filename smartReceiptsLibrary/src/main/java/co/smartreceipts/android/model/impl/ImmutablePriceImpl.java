package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.Price} interface
 *
 * @author williambaumann
 */
public final class ImmutablePriceImpl extends AbstractPriceImpl {

    private static final int ROUNDING_PRECISION = PRECISION + 2;

    private final BigDecimal mPrice;
    private final PriceCurrency mCurrency;
    private final ExchangeRate mExchangeRate;

    public ImmutablePriceImpl(@NonNull BigDecimal price, @NonNull PriceCurrency currency, @NonNull ExchangeRate exchangeRate) {
        mPrice = price.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        mCurrency = currency;
        mExchangeRate = exchangeRate;
    }

    private ImmutablePriceImpl(@NonNull Parcel in) {
        this.mPrice = new BigDecimal(in.readFloat());
        this.mCurrency = PriceCurrency.getInstance(in.readString());
        this.mExchangeRate = (ExchangeRate) in.readSerializable();
    }

    @Override
    public float getPriceAsFloat() {
        return mPrice.floatValue();
    }

    @Override
    @NonNull
    public BigDecimal getPrice() {
        return mPrice;
    }

    @Override
    @NonNull
    public String getDecimalFormattedPrice() {
        return ModelUtils.getDecimalFormattedValue(mPrice);
    }

    @Override
    @NonNull
    public String getCurrencyFormattedPrice() {
        return ModelUtils.getCurrencyFormattedValue(mPrice, mCurrency);
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        return ModelUtils.getCurrencyCodeFormattedValue(mPrice, mCurrency);
    }

    @Override
    @NonNull
    public PriceCurrency getCurrency() {
        return mCurrency;
    }

    @Override
    @NonNull
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
        dest.writeFloat(getPriceAsFloat());
        dest.writeString(getCurrencyCode());
        dest.writeSerializable(mExchangeRate);
    }

    public static final Creator<ImmutablePriceImpl> CREATOR = new Creator<ImmutablePriceImpl>() {
        public ImmutablePriceImpl createFromParcel(Parcel source) {
            return new ImmutablePriceImpl(source);
        }

        public ImmutablePriceImpl[] newArray(int size) {
            return new ImmutablePriceImpl[size];
        }
    };

}
