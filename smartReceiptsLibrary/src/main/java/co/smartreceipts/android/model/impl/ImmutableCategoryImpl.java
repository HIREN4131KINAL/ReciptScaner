package co.smartreceipts.android.model.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Category;

public class ImmutableCategoryImpl implements Category {

    private final String mName;
    private final String mCode;

    public ImmutableCategoryImpl(@NonNull String name, @NonNull String code) {
        mName = name;
        mCode = code;
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @NonNull
    @Override
    public String getCode() {
        return mCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableCategoryImpl)) return false;

        ImmutableCategoryImpl that = (ImmutableCategoryImpl) o;

        if (!mName.equals(that.mName)) return false;
        return mCode.equals(that.mCode);

    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return mName;
    }
}
