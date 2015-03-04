package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.utils.ModelUtils;

public final class DistanceDistanceColumn extends AbstractColumnImpl<Distance> {

    public DistanceDistanceColumn(int id, @NonNull String name) {
        super(id, name);
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getDecimalFormattedDistance();
    }

    @Override
    public String getFooter(@NonNull List<Distance> distances) {
        BigDecimal distance = new BigDecimal(0);
        for (int i = 0; i < distances.size(); i++) {
            distance = distance.add(distances.get(i).getDistance());
        }
        return ModelUtils.getDecimalFormattedValue(distance);
    }
}
