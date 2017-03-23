package co.smartreceipts.android.di;

import android.content.Context;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.WhiteLabelFriendlyTableDefaultsCustomizer;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import dagger.Module;
import dagger.Provides;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.storage.StorageManager;

@Module
public class BaseAppModule {

    @Provides
    @ApplicationScope
    public static Flex provideFlex(Context context) {
        return new Flex(context, new Flexable() {
            @Override
            public int getFleXML() {
                return Flexable.UNDEFINED;
            }
        });
    }

    @Provides
    public static StorageManager provideStorageManager(Context context) {
        return StorageManager.getInstance(context);
    }

    @Provides
    public static DatabaseHelper provideDatabaseHelper(Context context, StorageManager storageManager,
                                                       UserPreferenceManager preferences,
                                                       ReceiptColumnDefinitions receiptColumnDefinitions,
                                                       WhiteLabelFriendlyTableDefaultsCustomizer tableDefaultsCustomizer) {
        return DatabaseHelper.getInstance(context, storageManager, preferences, receiptColumnDefinitions, tableDefaultsCustomizer);
    }
}
