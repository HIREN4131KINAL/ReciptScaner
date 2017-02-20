package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

/**
 * Provides some core behavior that multiple {@link co.smartreceipts.android.workers.reports.Report} implementations use
 */
abstract class AbstractReport implements Report {

    private final Context mContext;
    private final DatabaseHelper mDB;
    private final UserPreferenceManager mPreferences;
    private final StorageManager mStorageManager;
    private final Flex mFlex;

    protected AbstractReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex) {
        this(context, persistenceManager.getDatabase(), persistenceManager.getPreferenceManager(), persistenceManager.getStorageManager(), flex);
    }

    protected AbstractReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull UserPreferenceManager preferences, @NonNull StorageManager storageManager, Flex flex) {
        mContext = context;
        mDB = db;
        mPreferences = preferences;
        mStorageManager = storageManager;
        mFlex = flex;
    }

    protected final Context getContext() {
        return mContext;
    }

    protected final DatabaseHelper getDatabase() {
        return mDB;
    }

    protected final UserPreferenceManager getPreferences() {
        return mPreferences;
    }

    public StorageManager getStorageManager() {
        return mStorageManager;
    }

    protected final Flex getFlex() {
        return mFlex;
    }

}