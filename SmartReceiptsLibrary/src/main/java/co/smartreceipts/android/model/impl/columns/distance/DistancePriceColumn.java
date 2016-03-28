package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;

public final class DistancePriceColumn extends AbstractColumnImpl<Distance> {

    private final boolean mAllowSpecialCharacters;

    public DistancePriceColumn(int id, @NonNull String name, boolean allowSpecialCharacters) {
        super(id, name);
        mAllowSpecialCharacters = allowSpecialCharacters;
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        if (mAllowSpecialCharacters) {
            return distance.getPrice().getCurrencyFormattedPrice();
        } else {
            return distance.getPrice().getCurrencyCodeFormattedPrice();
        }
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Distance> distances) {
        final WBCurrency tripCurrency = !distances.isEmpty() ? distances.get(0).getTrip().getTripCurrency() : null;
        if (mAllowSpecialCharacters) {
            return new PriceBuilderFactory().setPriceables(distances, tripCurrency).build().getCurrencyFormattedPrice();
        } else {
            return new PriceBuilderFactory().setPriceables(distances, tripCurrency).build().getCurrencyCodeFormattedPrice();
        }
    }
}
