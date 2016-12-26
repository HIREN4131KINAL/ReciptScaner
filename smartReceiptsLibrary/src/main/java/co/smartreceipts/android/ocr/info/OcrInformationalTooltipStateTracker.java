package co.smartreceipts.android.ocr.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import rx.Observable;
import rx.Subscriber;

public class OcrInformationalTooltipStateTracker {

    private static final String KEY_SHOW_OCR_PRE_RELEASE_INFO = "key_show_ocr_pre_release_info";

    private final SharedPreferences mSharedPreferences;

    public OcrInformationalTooltipStateTracker(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public OcrInformationalTooltipStateTracker(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    public Observable<Boolean> shouldShowPreReleaseQuestions() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(mSharedPreferences.getBoolean(KEY_SHOW_OCR_PRE_RELEASE_INFO, true));
                subscriber.onCompleted();
            }
        });
    }

    public void setShouldShowPreReleaseQuestions(boolean shouldShow) {
        mSharedPreferences.edit().putBoolean(KEY_SHOW_OCR_PRE_RELEASE_INFO, shouldShow).apply();
    }

}
