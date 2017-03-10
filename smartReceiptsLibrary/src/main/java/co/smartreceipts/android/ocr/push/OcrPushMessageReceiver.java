package co.smartreceipts.android.ocr.push;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.push.PushMessageReceiver;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class OcrPushMessageReceiver implements PushMessageReceiver {

    private final Subject<OcrResponse, OcrResponse> pushResultSubject = PublishSubject.create();
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
                .map(new Func1<RemoteMessage, OcrResponse>() {
                    @Override
                    public OcrResponse call(RemoteMessage remoteMessage) {
                        // TODO: Map me
                        return null;
                    }
                })
                .subscribe(new Action1<OcrResponse>() {
                    @Override
                    public void call(OcrResponse ocrResponse) {
                        pushResultSubject.onNext(ocrResponse);
                    }
                });
    }
}
