package android.support.v4.content;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import co.smartreceipts.android.utils.log.Logger;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

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
