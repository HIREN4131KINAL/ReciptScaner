package co.smartreceipts.android.utils.sorting;

import java.util.Comparator;

import co.smartreceipts.android.model.Category;

public class CategoryNameComparator implements Comparator<Category> {

    private final AlphabeticalCaseInsensitiveCharSequenceComparator mCharSequenceComparator = new AlphabeticalCaseInsensitiveCharSequenceComparator();

    @Override
    public int compare(Category category1, Category category2) {
        return mCharSequenceComparator.compare(category1.getName(), category2.getName());
    }
}
