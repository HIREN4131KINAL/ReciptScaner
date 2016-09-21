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
import com.google.android.gms.drive.Drive;
import com.google.common.base.Preconditions;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableControllerManager;
import co.smartreceipts.android.sync.drive.listeners.ReceiptBackupListener;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;

public class GoogleDriveBackupManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GoogleDriveBackupManager.class.getSimpleName();

    /**
     * Request code for auto Google Play Services error resolution.
     */
    private static final int REQUEST_CODE_RESOLUTION = 1;

    private final GoogleApiClient mGoogleApiClient;
    private final DriveStreamsManager mDriveTaskManager;
    private final AtomicReference<WeakReference<FragmentActivity>> mActivityReference;
    private final TableControllerManager mTableControllerManager;
    private final Context mContext;

    public GoogleDriveBackupManager(@NonNull Context context, @NonNull DatabaseHelper databaseHelper, @NonNull TableControllerManager tableControllerManager) {
        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .useDefaultAccount()
                .build();
        mTableControllerManager = Preconditions.checkNotNull(tableControllerManager);
        mDriveTaskManager = new DriveStreamsManager(context, mGoogleApiClient);
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
        /*
        final ReceiptBackupListener receiptBackupListener = new ReceiptBackupListener(mDriveTaskManager, mTableControllerManager.getReceiptTableController());
        mTableControllerManager.getReceiptTableController().subscribe(receiptBackupListener);
        */
        mDriveTaskManager.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mDriveTaskManager.onConnectionSuspended(cause);
    }

    private boolean isConnectedOrConnecting() {
        return mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
    }
}
