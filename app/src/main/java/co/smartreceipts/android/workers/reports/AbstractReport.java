package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

/**
 * Provides some core behavior that multiple {@link Report} implementations use
 */
public abstract class AbstractReport implements Report {

    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final UserPreferenceManager userPreferenceManager;
    private final StorageManager storageManager;
    private final Flex flex;

    protected AbstractReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager, @NonNull Flex flex) {
        this(context, persistenceManager.getDatabase(), persistenceManager.getPreferenceManager(), persistenceManager.getStorageManager(), flex);
    }

    protected AbstractReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull UserPreferenceManager preferences,
                             @NonNull StorageManager storageManager, @NonNull Flex flex) {
        this.context = Preconditions.checkNotNull(context);
        this.databaseHelper = Preconditions.checkNotNull(db);
        this.userPreferenceManager = Preconditions.checkNotNull(preferences);
        this.storageManager = Preconditions.checkNotNull(storageManager);
        this.flex = Preconditions.checkNotNull(flex);
    }

    @NonNull
    protected final Context getContext() {
        return context;
    }

    @NonNull
    protected final DatabaseHelper getDatabase() {
        return databaseHelper;
    }

    @NonNull
    protected final UserPreferenceManager getPreferences() {
        return userPreferenceManager;
    }

    @NonNull
    protected final StorageManager getStorageManager() {
        return storageManager;
    }

    @NonNull
    protected final Flex getFlex() {
        return flex;
    }

}