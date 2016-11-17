package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceCurrencyColumn extends AbstractColumnImpl<Distance> {

    public DistanceCurrencyColumn(int id, @NonNull String name, @NonNull SyncState syncState) {
        super(id, name, syncState);
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getPrice().getCurrencyCode();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Distance> distances) {
        return new PriceBuilderFactory().setPriceables(distances, null).build().getCurrencyCode();
    }
}
