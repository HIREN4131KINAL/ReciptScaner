package co.smartreceipts.android.utils;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class MultiDependencyFeature implements Feature {

    private final Feature first;
    private final Feature second;

    public MultiDependencyFeature(@NonNull Feature first, @NonNull Feature second) {
        this.first = Preconditions.checkNotNull(first);
        this.second = Preconditions.checkNotNull(second);
    }

    @Override
    public boolean isEnabled() {
        return first.isEnabled() && second.isEnabled();
    }
}
