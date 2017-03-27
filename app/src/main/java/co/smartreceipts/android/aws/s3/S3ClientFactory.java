package co.smartreceipts.android.aws.s3;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Preconditions;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

class S3ClientFactory {

    private final Context context;
    private final CognitoManager cognitoManager;

    private ReplaySubject<AmazonS3Client> amazonS3ReplaySubject;
    private ReplaySubject<TransferUtility> transferUtilityReplaySubject;

    public S3ClientFactory(@NonNull Context context, @NonNull CognitoManager cognitoManager) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.cognitoManager = Preconditions.checkNotNull(cognitoManager);
    }

    /**
     * @return an instance of an {@link TransferUtility} client. This should be treated as a singleton for the
     * lifetime of the parent {@link S3ClientFactory} object, since we use a replay subject
     */
    @NonNull
    public synchronized Observable<TransferUtility> getTransferUtility() {
        if (transferUtilityReplaySubject == null) {
            transferUtilityReplaySubject = ReplaySubject.create(1);
            getAmazonS3().map(new Func1<AmazonS3Client, TransferUtility>() {
                        @Override
                        public TransferUtility call(@NonNull AmazonS3Client amazonS3) {
                            return new TransferUtility(amazonS3, context);
                        }
                    })
                    .subscribe(transferUtilityReplaySubject);
        }
        return transferUtilityReplaySubject.asObservable();
    }

    /**
     * @return an instance of an {@link AmazonS3} client. This should be treated as a singleton for the
     * lifetime of the parent {@link S3ClientFactory} object, since we use a replay subject
     */
    @NonNull
    public synchronized Observable<AmazonS3Client> getAmazonS3() {
        if (amazonS3ReplaySubject == null) {
            amazonS3ReplaySubject = ReplaySubject.create(1);
            cognitoManager.getCognitoCachingCredentialsProvider()
                    .map(new Func1<CognitoCachingCredentialsProvider, AmazonS3Client>() {
                        @Override
                        public AmazonS3Client call(@NonNull CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider) {
                            return new AmazonS3Client(cognitoCachingCredentialsProvider);
                        }
                    })
                    .subscribe(amazonS3ReplaySubject);
        }
        return amazonS3ReplaySubject.asObservable();
    }


}
