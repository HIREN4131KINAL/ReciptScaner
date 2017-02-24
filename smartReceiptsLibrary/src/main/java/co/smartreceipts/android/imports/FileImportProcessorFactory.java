package co.smartreceipts.android.imports;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.StorageManager;

public class FileImportProcessorFactory {

    private final Context context;
    private final Trip trip;
    private final StorageManager storageManager;
    private final UserPreferenceManager preferenceManager;

    public FileImportProcessorFactory(@NonNull Context context, @NonNull Trip trip, @NonNull PersistenceManager persistenceManager) {
        this(context, trip, persistenceManager.getStorageManager(), persistenceManager.getPreferenceManager());
    }

    public FileImportProcessorFactory(@NonNull Context context, @NonNull Trip trip, @NonNull StorageManager storageManager,
                                      @NonNull UserPreferenceManager preferenceManager) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.trip = Preconditions.checkNotNull(trip);
        this.storageManager = Preconditions.checkNotNull(storageManager);
        this.preferenceManager = Preconditions.checkNotNull(preferenceManager);
    }

    @NonNull
    public FileImportProcessor get(int requestCode) {
        if (RequestCodes.PHOTO_REQUESTS.contains(requestCode)) {
            return new ImageImportProcessor(trip, storageManager, preferenceManager, context);
        } else if (RequestCodes.PDF_REQUESTS.contains(requestCode)) {
            return new GenericFileImportProcessor(trip, storageManager, context);
        } else {
            return new AutoFailImportProcessor();
        }
    }
}
