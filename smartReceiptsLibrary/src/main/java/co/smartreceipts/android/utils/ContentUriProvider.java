package co.smartreceipts.android.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;

import co.smartreceipts.android.utils.log.Logger;

/** 
 * Some (ie just one) phone manufacturers (ie Huawei) break the Android contract for calls to {@link Context#getExternalFilesDirs(String)}. 
 * Rather than returning {@link Context#getExternalFilesDir(String)} (ie the default entry) as the first entry in the 
 * array returned by the former call, it always returns the external sd card if present. This class provides a reflection-based
 * hack to protect against Huawei device weirdness in these cases
 */
public class ContentUriProvider {

    private static final String HUAWEI_MANUFACTURER = "Huawei";

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER) && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Logger.warn(ContentUriProvider.class, "Using a Huawei device on pre-N. Increased likelihood of failure...");
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                Logger.warn(ContentUriProvider.class, "Returning Uri.fromFile to avoid Huawei 'external-files-path' bug", e);
                return Uri.fromFile(file);
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }

}
