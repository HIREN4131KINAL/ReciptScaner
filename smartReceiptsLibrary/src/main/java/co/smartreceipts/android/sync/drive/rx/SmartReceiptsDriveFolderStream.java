package co.smartreceipts.android.sync.drive.rx;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.common.base.Preconditions;

import java.io.IOException;

import co.smartreceipts.android.sync.drive.DeviceMetadata;
import co.smartreceipts.android.sync.drive.GoogleDriveSyncMetadata;
import rx.Observable;
import rx.Subscriber;

public class SmartReceiptsDriveFolderStream {

    private static final String TAG = SmartReceiptsDriveFolderStream.class.getSimpleName();

    private static final String SMART_RECEIPTS_FOLDER = "Smart Receipts";
    private static final CustomPropertyKey SMART_RECEIPTS_FOLDER_KEY = new CustomPropertyKey("smart_receipts_id", CustomPropertyKey.PRIVATE);

    private final GoogleApiClient mGoogleApiClient;
    private final GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;
    private final Context mContext;
    private final DeviceMetadata mDeviceMetadata;

    public SmartReceiptsDriveFolderStream(@NonNull GoogleApiClient googleApiClient, @NonNull Context context) {
        this(googleApiClient, context, new GoogleDriveSyncMetadata(context), new DeviceMetadata(context));
    }

    public SmartReceiptsDriveFolderStream(@NonNull GoogleApiClient googleApiClient, @NonNull Context context, @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                                          @NonNull DeviceMetadata deviceMetadata) {
        mGoogleApiClient = Preconditions.checkNotNull(googleApiClient);
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mGoogleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        mDeviceMetadata = Preconditions.checkNotNull(deviceMetadata);
    }

    public Observable<DriveFolder> getSmartReceiptsFolder() {
        return Observable.create(new Observable.OnSubscribe<DriveFolder>() {
            @Override
            public void call(final Subscriber<? super DriveFolder> subscriber) {
                final Query folderQuery = new Query.Builder().addFilter(Filters.eq(SMART_RECEIPTS_FOLDER_KEY, mGoogleDriveSyncMetadata.getDeviceIdentifier().getId())).build();
                Drive.DriveApi.query(mGoogleApiClient, folderQuery).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        DriveId folderId = null;
                        for (final Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                            if (metadata.isFolder()) {
                                folderId = metadata.getDriveId();
                                break;
                            }
                        }

                        if (folderId != null) {
                            Log.i(TAG, "Found an existing Google Drive folder for Smart Receipts");
                            subscriber.onNext(folderId.asDriveFolder());
                            subscriber.onCompleted();
                        } else {
                            Log.i(TAG, "Failed to find an existing Smart Receipts folder for this device. Creating a new one...");
                            final MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(SMART_RECEIPTS_FOLDER).setDescription(mDeviceMetadata.getDeviceName()).setCustomProperty(SMART_RECEIPTS_FOLDER_KEY, mGoogleDriveSyncMetadata.getDeviceIdentifier().getId()).build();
                            Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallbacks<DriveFolder.DriveFolderResult>() {
                                @Override
                                public void onSuccess(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                                    subscriber.onNext(driveFolderResult.getDriveFolder());
                                    subscriber.onCompleted();
                                }

                                @Override
                                public void onFailure(@NonNull Status status) {
                                    Log.e(TAG, "Failed to create a home folder with status: " + status);
                                    subscriber.onError(new IOException(status.getStatusMessage()));
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Status status) {
                        Log.e(TAG, "Failed to query a Smart Receipts folder with status: " + status);
                        subscriber.onError(new IOException(status.getStatusMessage()));
                    }
                });
            }
        });
    }

}
