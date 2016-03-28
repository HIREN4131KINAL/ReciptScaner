package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Converts the {@link co.smartreceipts.android.model.Receipt#getPrice()} based on the current exchange rate
 */
public final class ReceiptExchangedPriceColumn extends AbstractExchangedPriceColumn {

    public ReceiptExchangedPriceColumn(int id, @NonNull String name, @NonNull Context context) {
        super(id, name, context);
    }

    @NonNull
    protected Price getPrice(@NonNull Receipt receipt) {
        return receipt.getPrice();
    }
}
