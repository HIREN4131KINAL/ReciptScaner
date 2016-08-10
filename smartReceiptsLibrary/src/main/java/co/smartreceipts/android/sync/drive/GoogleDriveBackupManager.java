package co.smartreceipts.android.sync.drive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.lang.ref.WeakReference;

public class GoogleDriveBackupManager implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TAG";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    private final GoogleApiClient mGoogleApiClient;
    private final WeakReference<FragmentActivity> mActivityReference;

    public GoogleDriveBackupManager(@NonNull FragmentActivity activity) {
        mGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                .enableAutoManage(activity, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .useDefaultAccount()
                .build();
        mActivityReference = new WeakReference<>(activity);
    }

    public GoogleDriveBackupManager(@NonNull GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
        mActivityReference = new WeakReference<>(null);
    }

    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(mActivityReference.get(), result.getErrorCode(), 0).show();
            return;
        }
        try {
            final Activity activity = mActivityReference.get();
            if (activity != null) {
                result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            }
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

}
