package co.smartreceipts.android.persistence.database.tables.ordering;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class OrderBy {

    private final String mSortByColumn;
    private final boolean mIsDescending;

    public OrderBy(@Nullable String sortByColumn, boolean isDescending) {
        mSortByColumn = sortByColumn;
        mIsDescending = isDescending;
    }

    @Nullable
    public final String getOrderByPredicate() {
        if (!TextUtils.isEmpty(mSortByColumn)) {
            return mSortByColumn + ((mIsDescending) ? " DESC" : " ASC");
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public String toString() {
        return getOrderByPredicate();
    }

}
