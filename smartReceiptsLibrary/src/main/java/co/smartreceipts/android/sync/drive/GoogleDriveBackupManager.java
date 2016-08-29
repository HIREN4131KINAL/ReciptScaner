package co.smartreceipts.android.sync.drive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.common.base.Preconditions;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.sync.drive.rx.SmartReceiptsDriveFolderStream;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GoogleDriveBackupManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TAG";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    private static final int REQUEST_CODE_RESOLUTION = 1;

    private final GoogleApiClient mGoogleApiClient;
    private final SmartReceiptsDriveFolderStream mSmartReceiptsDriveFolderStream;
    private final AtomicReference<WeakReference<FragmentActivity>> mActivityReference;
    private final Context mContext;

    public GoogleDriveBackupManager(@NonNull Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .useDefaultAccount()
                .build();
        mSmartReceiptsDriveFolderStream = new SmartReceiptsDriveFolderStream(mGoogleApiClient, context);
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mActivityReference = new AtomicReference<>(new WeakReference<FragmentActivity>(null));
    }

    public void initialize(@NonNull FragmentActivity activity) {
        final FragmentActivity existingActivity = mActivityReference.get().get();
        if (!activity.equals(existingActivity)) {
            mActivityReference.set(new WeakReference<>(activity));
        }
        if (!isConnectedOrConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    public void deinitialize() {
        if (isConnectedOrConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            if (!isConnectedOrConnecting()) {
                mGoogleApiClient.connect();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.w(TAG, "GoogleApiClient connection failed: " + result.toString());

        final FragmentActivity activity = mActivityReference.get().get();
        if (activity == null) {
            Log.e(TAG, "The parent activity was destroyed. Unable to resolve GoogleApiClient connection failure.");
            return;
        }

        try {
            if (!result.hasResolution()) {
                GoogleApiAvailability.getInstance().getErrorDialog(activity, result.getErrorCode(), 0).show();
                return;
            }
            try {
                result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "The parent activity is in a bad state.. Unable to resolve GoogleApiClient connection failure.");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connection succeeded.");
        mSmartReceiptsDriveFolderStream.getSmartReceiptsFolder()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<DriveFolder>() {
                    @Override
                    public void call(DriveFolder driveFolder) {
                        Log.i(TAG, "Found drive folder: " + driveFolder);
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended with cause " + cause);
    }

    private boolean isConnectedOrConnecting() {
        return mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
    }
}
