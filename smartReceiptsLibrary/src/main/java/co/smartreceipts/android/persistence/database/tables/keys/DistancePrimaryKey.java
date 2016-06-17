package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;

/**
 * Defines the primary key for the {@link DistanceTable}
 */
public final class DistancePrimaryKey implements PrimaryKey<Distance, Integer> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return DistanceTable.COLUMN_ID;
    }

    @Override
    @NonNull
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @Override
    @NonNull
    public Integer getPrimaryKeyValue(@NonNull Distance distance) {
        return distance.getId();
    }
}
