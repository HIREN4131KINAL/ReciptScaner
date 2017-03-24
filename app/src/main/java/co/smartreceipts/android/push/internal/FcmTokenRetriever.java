package co.smartreceipts.android.push.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;

import rx.Observable;
import rx.Subscriber;

public class FcmTokenRetriever {

    @Nullable
    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    @NonNull
    public Observable<String> getFcmTokenObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                final String token = getToken();
                if (token != null) {
                    subscriber.onNext(token);
                }
                subscriber.onCompleted();
            }
        });
    }
}
