package co.smartreceipts.android.ocr.purchases;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;

class LocalOcrScansTracker {

    private static final String KEY_AVAILABLE_SCANS = "key_int_available_ocr_scans";

    private final SharedPreferences sharedPreferences;
    private final BehaviorSubject<Integer> remainingScansSubject;

    public LocalOcrScansTracker(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @VisibleForTesting
    LocalOcrScansTracker(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
        this.remainingScansSubject = BehaviorSubject.create(getRemainingScans());
    }

    /**
     * @return the remaining Ocr scan count that is allowed for this user. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan. Additionally, please note that this {@link Observable}
     * will only call {@link Subscriber#onNext(Object)} with the latest value (and never onComplete or
     * onError) to allow us to continually get the updated value
     */
    public Observable<Integer> getRemainingScansStream() {
        return remainingScansSubject.asObservable();
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
        remainingScansSubject.onNext(remainingScans);
    }

    public void decrementRemainingScans() {
        if (getRemainingScans() > 0) {
            sharedPreferences.edit().putInt(KEY_AVAILABLE_SCANS, getRemainingScans() - 1).apply();
            remainingScansSubject.onNext(getRemainingScans());
        }
    }
}
