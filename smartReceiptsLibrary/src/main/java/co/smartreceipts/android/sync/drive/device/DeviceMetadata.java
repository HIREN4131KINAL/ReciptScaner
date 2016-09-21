package co.smartreceipts.android.sync.drive.device;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.UUID;

public class DeviceMetadata {

    private final Context mContext;

    public DeviceMetadata(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @NonNull
    public String getUniqueDeviceId() {
        final String id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id != null) {
            return id;
        } else {
            return UUID.randomUUID().toString();
        }
    }

    @NonNull
    public String getDeviceName() {
        final String name = Build.MODEL;
        if (name != null) {
            return name;
        } else {
            return "";
        }
    }
}
