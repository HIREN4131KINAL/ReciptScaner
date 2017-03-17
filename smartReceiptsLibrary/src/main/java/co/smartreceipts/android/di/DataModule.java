package co.smartreceipts.android.di;

import android.content.Context;

import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.WhiteLabelFriendlyTableDefaultsCustomizer;
import co.smartreceipts.android.settings.UserPreferenceManager;
import dagger.Module;
import dagger.Provides;
import wb.android.storage.StorageManager;

@Module
public class DataModule {

    // TODO: 15.03.2017 temporary approach for logic stability. StorageManager should be refactored
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
