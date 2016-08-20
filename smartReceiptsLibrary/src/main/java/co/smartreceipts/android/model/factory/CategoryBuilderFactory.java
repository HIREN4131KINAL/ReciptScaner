package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.impl.ImmutableCategoryImpl;
import co.smartreceipts.android.model.impl.ImmutablePaymentMethodImpl;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link PaymentMethod} objects
 */
public class CategoryBuilderFactory implements BuilderFactory<Category> {

    private String mName;
    private String mCode;
    private SyncState mSyncState;

    /**
     * Default constructor for this class
     */
    public CategoryBuilderFactory() {
        mName = "";
        mCode = "";
        mSyncState = new DefaultSyncState();
    }

    /**
     * Defines the "name" for this category
     *
     * @param name - the name
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setName(@NonNull String name) {
        mName = Preconditions.checkNotNull(name);
        return this;
    }

    /**
     * Defines the "code" for this category
     *
     * @param code - the category code
     * @return this {@link CategoryBuilderFactory} for method chaining
     */
    public CategoryBuilderFactory setCode(@NonNull String code) {
        mCode = Preconditions.checkNotNull(code);
        return this;
    }

    public CategoryBuilderFactory setSyncState(@NonNull SyncState syncState) {
        mSyncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    /**
     * @return - the {@link Category} object as set by the setter methods in this class
     */
    @NonNull
    public Category build() {
        return new ImmutableCategoryImpl(mName, mCode, mSyncState);
    }
}
