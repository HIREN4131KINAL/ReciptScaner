package co.smartreceipts.android.workers.reports.columns;

import android.content.Context;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.Preferences;

/**
 * Provides an implementation of the {@link TableColumns}
 * for a collection of {@link co.smartreceipts.android.model.Distance} objects.
 *
 * @author williambaumann
 */
public class DistanceTableColumns implements TableColumns {

    private static final int DISTANCE_COLUMN_COUNT = 7;

    private final Context mContext;
    private final Preferences mPreferences;
    private final List<Distance> mDistances;
    private int row;

    public DistanceTableColumns(@NonNull Context context, @NonNull Preferences preferences, @NonNull List<Distance> distances) {
        mContext = context;
        mPreferences = preferences;
        mDistances = distances;
        row = -2; // So next goes to -1 => Header
    }

    @Override
    public int getColumnCount() {
        return DISTANCE_COLUMN_COUNT;
    }

    @Override
    @NonNull
    public String getValueAt(int column) {
        if (row < 0) {
            return getHeaderValueAt(column);
        } else if (row >= mDistances.size()) {
            return getFooterValueAt(column);
        } else {
            final Distance distance = mDistances.get(row);
            if (column == 0) {
                return distance.getLocation();
            } else if (column == 1) {
                return distance.getCurrencyFormattedPrice();
            } else if (column == 2) {
                return distance.getDecimalFormattedDistance();
            } else if (column == 3) {
                return distance.getDecimalFormattedRate();
            } else if (column == 4) {
                return distance.getCurrencyCode();
            } else if (column == 5) {
                return distance.getFormattedDate(mContext, mPreferences.getDateSeparator());
            } else if (column == 6) {
                return distance.getComment();
            } else {
                return "";
            }
        }
    }

    private String getHeaderValueAt(int column) {
        if (column == 0) {
            return mContext.getString(R.string.distance_location_field);
        } else if (column == 1) {
            return mContext.getString(R.string.distance_price_field);
        } else if (column == 2) {
            return mContext.getString(R.string.distance_distance_field);
        } else if (column == 3) {
            return mContext.getString(R.string.distance_rate_field);
        } else if (column == 4) {
            return mContext.getString(R.string.dialog_currency_field);
        } else if (column == 5) {
            return mContext.getString(R.string.distance_date_field);
        } else if (column == 6) {
            return mContext.getString(R.string.distance_comment_field);
        } else {
            return "";
        }
    }

    private String getFooterValueAt(int column) {
        if (column == 0) {
            return mContext.getString(R.string.total_item);
        } else if (column == 1) {
            BigDecimal price = new BigDecimal(0);
            for (int i = 0; i < mDistances.size(); i++) {
                price = price.add(mDistances.get(i).getPrice());
            }
            return ModelUtils.getDecimalFormattedValue(price);
        } else if (column == 2) {
            BigDecimal distance = new BigDecimal(0);
            for (int i = 0; i < mDistances.size(); i++) {
                distance = distance.add(mDistances.get(i).getDistance());
            }
            return ModelUtils.getDecimalFormattedValue(distance);
        } else {
            return "";
        }
    }

    @Override
    public boolean nextRow() {
        return mDistances.size() >= ++row;
    }
}
