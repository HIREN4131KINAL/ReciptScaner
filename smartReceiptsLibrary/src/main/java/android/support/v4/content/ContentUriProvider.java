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
    private static final List<String> KNOWN_FAULTY_BUILD_PRODUCTS_PREFIXES = Arrays.asList("ALE", "KIW", "FRD", "H1611", "VNS", "GRA");
    private static final List<String> FILE_URI_SCHEME_FALLBACK_DEVICES = Arrays.asList("KIW-L24");

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
            Logger.warn(ContentUriProvider.class, "Using a Huawei device. Increased likelihood of failure...");
            for (final String buildProductPrefix : KNOWN_FAULTY_BUILD_PRODUCTS_PREFIXES) {
                if (Build.PRODUCT != null && Build.PRODUCT.startsWith(buildProductPrefix)) {
                    Logger.info(ContentUriProvider.class, "Found a known 'bad' huawei device: {}.", Build.PRODUCT);
                    if (FILE_URI_SCHEME_FALLBACK_DEVICES.contains(Build.PRODUCT) ) {
                        Logger.error(ContentUriProvider.class, "Presumed unfixable device. Not sure how to procede");
                    }
                    Logger.info(ContentUriProvider.class, "Attempting to use reflection to fix the Huawei bug");
                    adjustHuaweiStaticCache(context, authority);
                }
            }
            return FileProvider.getUriForFile(context, authority, file);
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }

    /**
     * Here, we try to use reflection to solve the problem. Please note that this will only work for devices
     * that do NOT have a {@link SecurityManager} present that prevents this type of behavior
     */
    @SuppressWarnings("unchecked")
    private static void adjustHuaweiStaticCache(@NonNull Context context, @NonNull String authority) {
        try {
            final Field field = FileProvider.class.getDeclaredField("sCache");
            field.setAccessible(true);
            final HashMap<String, FileProvider.PathStrategy> fileProviderCache = (HashMap<String, FileProvider.PathStrategy>) field.get(HashMap.class);
            final FileProvider.PathStrategy strategy = parsePathStrategy(context, authority);
            fileProviderCache.put(authority, strategy);
        } catch (Exception e) {
            Logger.error(ContentUriProvider.class, "Failed to reflectively fix Huawei", e);
        }
    }

    /**
     * Code below is copied from: https://github.com/android/platform_frameworks_support/blob/master/v4/java/android/support/v4/content/FileProvider.java
     */

    private static final String META_DATA_FILE_PROVIDER_PATHS = "android.support.FILE_PROVIDER_PATHS";

    private static final String TAG_ROOT_PATH = "root-path";
    private static final String TAG_FILES_PATH = "files-path";
    private static final String TAG_CACHE_PATH = "cache-path";
    private static final String TAG_EXTERNAL = "external-path";
    private static final String TAG_EXTERNAL_FILES = "external-files-path";
    private static final String TAG_EXTERNAL_CACHE = "external-cache-path";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";

    private static final File DEVICE_ROOT = new File("/");

    /**
     * A copy of the FileProvider's public method with a tweak for Huawei
     */
    private static FileProvider.PathStrategy parsePathStrategy(Context context, String authority)
            throws IOException, XmlPullParserException {
        final FileProvider.SimplePathStrategy strat = new FileProvider.SimplePathStrategy(authority);

        final ProviderInfo info = context.getPackageManager()
                .resolveContentProvider(authority, PackageManager.GET_META_DATA);
        final XmlResourceParser in = info.loadXmlMetaData(
                context.getPackageManager(), META_DATA_FILE_PROVIDER_PATHS);
        if (in == null) {
            throw new IllegalArgumentException(
                    "Missing " + META_DATA_FILE_PROVIDER_PATHS + " meta-data");
        }

        int type;
        while ((type = in.next()) != END_DOCUMENT) {
            if (type == START_TAG) {
                final String tag = in.getName();

                final String name = in.getAttributeValue(null, ATTR_NAME);
                String path = in.getAttributeValue(null, ATTR_PATH);

                File target = null;
                if (TAG_ROOT_PATH.equals(tag)) {
                    target = DEVICE_ROOT;
                } else if (TAG_FILES_PATH.equals(tag)) {
                    target = context.getFilesDir();
                } else if (TAG_CACHE_PATH.equals(tag)) {
                    target = context.getCacheDir();
                } else if (TAG_EXTERNAL.equals(tag)) {
                    target = Environment.getExternalStorageDirectory();
                } else if (TAG_EXTERNAL_FILES.equals(tag)) {
                    File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);
                    if (externalFilesDirs.length > 0) {
                        target = externalFilesDirs[0];
                    }
                } else if (TAG_EXTERNAL_CACHE.equals(tag)) {
                    File[] externalCacheDirs = ContextCompat.getExternalCacheDirs(context);
                    if (externalCacheDirs.length == 1) {
                        target = externalCacheDirs[0];
                    } else if (externalCacheDirs.length == 2) {
                        // TODO//Highlight: Note: This is to fix Huawei's annoying behavior
                        target = externalCacheDirs[1];
                    } else if (externalCacheDirs.length > 2) {
                        target = externalCacheDirs[0];
                    }
                }

                if (target != null) {
                    strat.addRoot(name, buildPath(target, path));
                }
            }
        }

        return strat;
    }

    private static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (segment != null) {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

}
