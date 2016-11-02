package co.smartreceipts.android.model.comparators;

import android.support.annotation.NonNull;

import java.util.Comparator;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.utils.sorting.AlphabeticalCaseInsensitiveCharSequenceComparator;

public class ColumnNameComparator<T extends Column<?>> implements Comparator<T> {

    private final Comparator<CharSequence> mCharSequenceComparator = new AlphabeticalCaseInsensitiveCharSequenceComparator();

    @Override
    public int compare(@NonNull T column1, @NonNull T column2) {
        return mCharSequenceComparator.compare(column1.getName(), column2.getName());
    }
}
