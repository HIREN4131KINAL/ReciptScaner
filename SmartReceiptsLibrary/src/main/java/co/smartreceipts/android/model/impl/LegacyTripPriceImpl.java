package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * In order to ensure that I'm keeping the same (albeit broken for multi-currencies) price behavior
 * for {@link co.smartreceipts.android.model.Trip} objects, I built this behavior. It should be
 * removed once we get real cross-currency conversions
 *
 * @author williambaumann
 */
public class LegacyTripPriceImpl implements Price, android.os.Parcelable {

    private final BigDecimal mPrice;
    private final WBCurrency mCurrency;

    /**
     * Default constructor
     *
     * @param price    - the price as a {@link java.math.BigDecimal}
     * @param currency - the {@link co.smartreceipts.android.model.WBCurrency}. If {@code null}, we assume it's mixed currencies
     */
    public LegacyTripPriceImpl(@NonNull BigDecimal price, WBCurrency currency) {
        mPrice = price;
        mCurrency = currency;
    }

    private LegacyTripPriceImpl(Parcel in) {
        mPrice = new BigDecimal(in.readFloat());
        final String currencyCode = in.readString();
        mCurrency = currencyCode != null ? WBCurrency.getInstance(currencyCode) : null;
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
        if (mCurrency != null) {
            return ModelUtils.getCurrencyFormattedValue(mPrice, mCurrency);
        } else {
            return "Mixed";
        }
    }

    @Override
    @NonNull
    public WBCurrency getCurrency() {
        if (mCurrency != null) {
            return mCurrency;
        } else {
            return WBCurrency.MISSING_CURRENCY;
        }
    }

    @Override
    @NonNull
    public String getCurrencyCode() {
        if (mCurrency != null) {
            return mCurrency.getCurrencyCode();
        } else {
            return WBCurrency.MISSING_CURRENCY_CODE;
        }
    }

    @Override
    public String toString() {
        return getCurrencyFormattedPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LegacyTripPriceImpl that = (LegacyTripPriceImpl) o;

        if (mCurrency != null ? !mCurrency.equals(that.mCurrency) : that.mCurrency != null)
            return false;
        if (!mPrice.equals(that.mPrice)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mPrice.hashCode();
        result = 31 * result + (mCurrency != null ? mCurrency.hashCode() : 0);
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(getPriceAsFloat());
        dest.writeString(getCurrencyCode());
    }

    public static final Creator<LegacyTripPriceImpl> CREATOR = new Creator<LegacyTripPriceImpl>() {
        public LegacyTripPriceImpl createFromParcel(Parcel source) {
            return new LegacyTripPriceImpl(source);
        }

        public LegacyTripPriceImpl[] newArray(int size) {
            return new LegacyTripPriceImpl[size];
        }
    };
}
