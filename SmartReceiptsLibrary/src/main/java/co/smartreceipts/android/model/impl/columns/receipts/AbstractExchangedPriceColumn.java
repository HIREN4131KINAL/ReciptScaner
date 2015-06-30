package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Priceable;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Allows us to genericize how different prices are converted to a trip's base currency
 */
public abstract class AbstractExchangedPriceColumn extends AbstractColumnImpl<Receipt> {

    private final Context mContext;

    public AbstractExchangedPriceColumn(int id, @NonNull String name, @NonNull Context context) {
        super(id, name);
        mContext = context;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        final Price price = getPrice(receipt);
        final ExchangeRate exchangeRate = price.getExchangeRate();
        final WBCurrency baseCurrency = receipt.getTrip().getDefaultCurrency();
        if (exchangeRate.supportsExchangeRateFor(baseCurrency)) {
            return ModelUtils.getDecimalFormattedValue(price.getPrice().multiply(exchangeRate.getExchangeRate(baseCurrency)));
        } else {
            return mContext.getString(R.string.undefined);
        }
    }

    @NonNull
    protected abstract Price getPrice(@NonNull Receipt receipt);
}
