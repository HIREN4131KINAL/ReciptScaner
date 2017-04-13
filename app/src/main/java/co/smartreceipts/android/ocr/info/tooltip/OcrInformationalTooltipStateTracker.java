package co.smartreceipts.android.ocr.info.tooltip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.di.scopes.FragmentScope;
import rx.Observable;
import rx.Subscriber;

@FragmentScope
public class OcrInformationalTooltipStateTracker {

    private static final String KEY_SHOW_OCR_RELEASE_INFO = "key_show_ocr_release_info";
    private static final String KEY_SHOW_OCR_RELEASE_SET_DATE = "key_show_ocr_release_info_set_date";

    private final SharedPreferences preferences;

    public OcrInformationalTooltipStateTracker(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public OcrInformationalTooltipStateTracker(@NonNull SharedPreferences sharedPreferences) {
        preferences = Preconditions.checkNotNull(sharedPreferences);
    }

    public Observable<Boolean> shouldShowOcrInfo() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(preferences.getBoolean(KEY_SHOW_OCR_RELEASE_INFO, true));
                subscriber.onCompleted();
            }
        });
    }

    public void setShouldShowOcrInfo(boolean shouldShow) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_SHOW_OCR_RELEASE_INFO, shouldShow);
        editor.putLong(KEY_SHOW_OCR_RELEASE_SET_DATE, System.currentTimeMillis());
        editor.apply();
    }

}
