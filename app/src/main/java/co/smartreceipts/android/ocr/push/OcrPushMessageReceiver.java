package co.smartreceipts.android.ocr.push;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;

import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.push.PushMessageReceiver;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.AsyncOnSubscribe;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class OcrPushMessageReceiver implements PushMessageReceiver {

    private static final int TIMEOUT_SECONDS = 10;

    private final Subject<Object, Object> pushResultSubject = PublishSubject.create();
    private final Scheduler subscribeOnScheduler;

    public OcrPushMessageReceiver() {
        this(Schedulers.io());
    }

    public OcrPushMessageReceiver(@NonNull Scheduler subscribeOnScheduler) {
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Observable.just(remoteMessage)
                .subscribeOn(subscribeOnScheduler)
                .map(new Func1<RemoteMessage, Object>() {
                    @Override
                    public Object call(RemoteMessage remoteMessage) {
                        return new Object();
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object next) {
                        pushResultSubject.onNext(next);
                        pushResultSubject.onCompleted();
                    }
                });
    }

    public Observable<Object> getOcrPushResponse() {
        return pushResultSubject.asObservable()
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }


}
