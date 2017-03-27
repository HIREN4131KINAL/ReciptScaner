package co.smartreceipts.android.aws.s3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class S3Manager {

    private static final String BUCKET = "smartreceipts";

    private final S3ClientFactory s3ClientFactory;
    private final S3KeyGeneratorFactory s3KeyGeneratorFactory;
    private final Executor executor;

    public S3Manager(@NonNull Context context, @NonNull CognitoManager cognitoManager) {
        this(new S3ClientFactory(context, cognitoManager), new S3KeyGeneratorFactory(), Executors.newSingleThreadExecutor());
    }

    @VisibleForTesting
    S3Manager(@NonNull S3ClientFactory s3ClientFactory, @NonNull S3KeyGeneratorFactory s3KeyGeneratorFactory,
              @NonNull Executor executor) {
        this.s3ClientFactory = Preconditions.checkNotNull(s3ClientFactory);
        this.s3KeyGeneratorFactory = Preconditions.checkNotNull(s3KeyGeneratorFactory);
        this.executor = Preconditions.checkNotNull(executor);
    }

    /**
     * Uploads a file to this user's S3 cognito account
     *
     * @param file the {@link File} to upload
     * @param subDirectoryPath the subdirectory path to load to (either an empty string for the bucket or something more complex as desired)
     * @return an {@link Observable} that will return the URL path as a string if valid
     */
    @NonNull
    public Observable<String> upload(@NonNull final File file, @NonNull final String subDirectoryPath) {
        return s3ClientFactory.getAmazonS3()
                .flatMap(new Func1<AmazonS3Client, Observable<String>>() {
                    @Override
                    public Observable<String> call(@NonNull final AmazonS3Client amazonS3) {
                        return s3ClientFactory.getTransferUtility()
                                .flatMap(new Func1<TransferUtility, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(@NonNull final TransferUtility transferUtility) {
                                        return s3KeyGeneratorFactory.get()
                                                .flatMap(new Func1<S3KeyGenerator, Observable<String>>() {
                                                    @Override
                                                    public Observable<String> call(@NonNull final S3KeyGenerator s3KeyGenerator) {
                                                        return Observable.create(new Observable.OnSubscribe<String>() {
                                                            @Override
                                                            public void call(final Subscriber<? super String> subscriber) {
                                                                final String key = subDirectoryPath + s3KeyGenerator.getS3Key() + file.getName();
                                                                final TransferObserver transferObject = transferUtility.upload(BUCKET, key, file);
                                                                transferObject.setTransferListener(new TransferListener() {
                                                                    @Override
                                                                    public void onStateChanged(int id, final TransferState state) {
                                                                        // Note: All these callbacks occur on the UI thread, so we push them off immediately
                                                                        executor.execute(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                if (state == TransferState.CANCELED || state == TransferState.FAILED) {
                                                                                    subscriber.onError(new Exception("Transfer failed or was cancelled"));
                                                                                    transferObject.cleanTransferListener();
                                                                                } else if (state == TransferState.COMPLETED) {
                                                                                    subscriber.onNext(amazonS3.getResourceUrl(BUCKET, key));
                                                                                    subscriber.onCompleted();
                                                                                    transferObject.cleanTransferListener();
                                                                                }
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                                                        // Note: All these callbacks occur on the UI thread, so we push them off immediately
                                                                        // No-op
                                                                    }

                                                                    @Override
                                                                    public void onError(int id, final Exception ex) {
                                                                        // Note: All these callbacks occur on the UI thread, so we push them off immediately
                                                                        executor.execute(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                subscriber.onError(ex);
                                                                                transferObject.cleanTransferListener();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

}
