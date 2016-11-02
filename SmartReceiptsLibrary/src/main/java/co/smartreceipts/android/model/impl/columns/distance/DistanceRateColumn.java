package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceRateColumn extends AbstractColumnImpl<Distance> {

    public DistanceRateColumn(int id, @NonNull String name, @NonNull SyncState syncState) {
        super(id, name, syncState);
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getDecimalFormattedRate();
    }

}
