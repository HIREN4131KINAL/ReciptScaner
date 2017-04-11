package co.smartreceipts.android.aws.s3;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

class S3ClientFactory {

    private final Context context;
    private final CognitoManager cognitoManager;

    private ReplaySubject<Optional<AmazonS3Client>> amazonS3ReplaySubject;
    private Subscription amazonS3Subscription;

    public S3ClientFactory(@NonNull Context context, @NonNull CognitoManager cognitoManager) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.cognitoManager = Preconditions.checkNotNull(cognitoManager);
    }

    /**
     * @return an {@link Optional} instance of the {@link AmazonS3} client. Once we fetch a valid
     * entry, this should be treated as a singleton for the lifetime of the parent
     * {@link S3ClientFactory} object, since we use a replay subject
     */
    @NonNull
    public synchronized Observable<Optional<AmazonS3Client>> getAmazonS3() {
        if (amazonS3ReplaySubject == null) {
            amazonS3ReplaySubject = ReplaySubject.createWithSize(1);
            amazonS3Subscription = cognitoManager.getCognitoCachingCredentialsProvider()
                    .map(new Func1<Optional<CognitoCachingCredentialsProvider>, Optional<AmazonS3Client>>() {
                        @Override
                        public Optional<AmazonS3Client> call(@NonNull Optional<CognitoCachingCredentialsProvider> cognitoCachingCredentialsProvider) {
                            if (cognitoCachingCredentialsProvider.isPresent()) {
                                return Optional.of(new AmazonS3Client(cognitoCachingCredentialsProvider.get()));
                            } else {
                                return Optional.absent();
                            }
                        }
                    })
                    .subscribe(new Subscriber<Optional<AmazonS3Client>>() {
                        @Override
                        public void onCompleted() {
                            amazonS3ReplaySubject.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            amazonS3ReplaySubject.onError(e);
                        }

                        @Override
                        public void onNext(Optional<AmazonS3Client> amazonS3ClientOptional) {
                            amazonS3ReplaySubject.onNext(amazonS3ClientOptional);
                            if (amazonS3ClientOptional.isPresent()) {
                                amazonS3ReplaySubject.onCompleted();
                                if (amazonS3Subscription != null) {
                                    amazonS3Subscription.unsubscribe();
                                }
                            }
                        }
                    });
        }
        return amazonS3ReplaySubject.asObservable();
    }


}
