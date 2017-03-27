package co.smartreceipts.android.aws.s3;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.ReplaySubject;

class S3KeyGeneratorFactory {

    private ReplaySubject<S3KeyGenerator> s3KeyGeneratorReplaySubject;

    @NonNull
    public synchronized Observable<S3KeyGenerator> get() {
        if (s3KeyGeneratorReplaySubject == null) {
            s3KeyGeneratorReplaySubject = ReplaySubject.create(1);
            Observable.create(new Observable.OnSubscribe<S3KeyGenerator>() {
                        @Override
                        public void call(Subscriber<? super S3KeyGenerator> subscriber) {
                            subscriber.onNext(new S3KeyGenerator());
                            subscriber.onCompleted();
                        }
                    })
                    .subscribe(s3KeyGeneratorReplaySubject);
        }
        return s3KeyGeneratorReplaySubject.asObservable();
    }
}
