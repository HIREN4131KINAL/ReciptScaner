package co.smartreceipts.android.ocr.purchases;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.utils.log.Logger;

class LocalOcrScansTracker {

    private static final String KEY_AVAILABLE_SCANS = "key_int_available_ocr_scans";

    private final SharedPreferences sharedPreferences;

    public LocalOcrScansTracker(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @VisibleForTesting
    LocalOcrScansTracker(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    /**
     * @return the locally tracked (ie possibly inaccurate) remaining scans count
     */
    public int getRemainingScans() {
        return sharedPreferences.getInt(KEY_AVAILABLE_SCANS, 0);
    }

    public void setRemainingScans(int remainingScans) {
        Logger.info(this, "Setting scans remaining as {}.", remainingScans);
        sharedPreferences.edit().putInt(KEY_AVAILABLE_SCANS, remainingScans).apply();
    }

    public void decrementRemainingScans() {
        if (getRemainingScans() > 0) {
            sharedPreferences.edit().putInt(KEY_AVAILABLE_SCANS, getRemainingScans() - 1).apply();
        }
    }
}
