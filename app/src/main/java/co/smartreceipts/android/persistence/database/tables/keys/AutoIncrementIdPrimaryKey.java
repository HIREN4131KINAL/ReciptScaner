package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * A special {@link PrimaryKey} implementation to be used in conjunction with "INTEGER PRIMARY KEY AUTOINCREMENT"
 * database tables, since we won't know the key until after the insert completes...
 *
 * @param <ModelType> the model object with an AutoIncrement Integer Primary Key
 */
public final class AutoIncrementIdPrimaryKey<ModelType> implements PrimaryKey<ModelType, Integer> {

    private final PrimaryKey<ModelType, Integer> mPrimaryKey;
    private final Integer mId;

    public AutoIncrementIdPrimaryKey(@NonNull PrimaryKey<ModelType, Integer> primaryKey, @NonNull Integer id) {
        mPrimaryKey = Preconditions.checkNotNull(primaryKey);
        mId = Preconditions.checkNotNull(id);
    }

    @NonNull
    @Override
    public String getPrimaryKeyColumn() {
        return mPrimaryKey.getPrimaryKeyColumn();
    }

    @NonNull
    @Override
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @NonNull
    @Override
    public Integer getPrimaryKeyValue(@NonNull ModelType modelType) {
        return mId;
    }
}
