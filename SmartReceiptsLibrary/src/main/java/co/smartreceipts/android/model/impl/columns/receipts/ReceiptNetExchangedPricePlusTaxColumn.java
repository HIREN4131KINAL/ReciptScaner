package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.persistence.Preferences;

/**
 * Provides a column that returns the total of the price and tax fields based on user settings
 */
public final class ReceiptNetExchangedPricePlusTaxColumn extends AbstractExchangedPriceColumn {

    private final Preferences mPreferences;

    public ReceiptNetExchangedPricePlusTaxColumn(int id, @NonNull String name, @NonNull Context context, @NonNull Preferences preferences) {
        super(id, name, context);
        mPreferences = preferences;
    }

    @NonNull
    @Override
    protected Price getPrice(@NonNull Receipt receipt) {
        if (mPreferences.usePreTaxPrice()) {
            final PriceBuilderFactory factory = new PriceBuilderFactory();
            factory.setPrices(Arrays.asList(receipt.getPrice(), receipt.getTax()), receipt.getTrip().getTripCurrency());
            return factory.build();
        } else {
            return receipt.getPrice();
        }
    }
}
