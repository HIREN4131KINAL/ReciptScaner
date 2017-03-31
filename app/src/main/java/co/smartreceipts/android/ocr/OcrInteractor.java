package co.smartreceipts.android.ocr;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.io.File;

import javax.inject.Inject;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.aws.s3.S3Manager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.ocr.apis.OcrService;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.apis.model.RecognitionResponse;
import co.smartreceipts.android.ocr.apis.model.RecongitionRequest;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.ocr.push.OcrPushMessageReceiver;
import co.smartreceipts.android.push.PushManager;
import co.smartreceipts.android.utils.Feature;
import co.smartreceipts.android.utils.FeatureFlags;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@ApplicationScope
public class OcrInteractor {

    private static final String OCR_FOLDER = "ocr/";

    private final Context context;
    private final S3Manager s3Manager;
    private final IdentityManager identityManager;
    private final ServiceManager ocrServiceManager;
    private final PushManager pushManager;
    private final OcrPurchaseTracker ocrPurchaseTracker;
    private final OcrPushMessageReceiver pushMessageReceiver;
    private final Feature ocrFeature;

    public OcrInteractor(@NonNull Context context, @NonNull S3Manager s3Manager, @NonNull IdentityManager identityManager,
                         @NonNull ServiceManager serviceManager, @NonNull PushManager pushManager, @NonNull OcrPurchaseTracker ocrPurchaseTracker) {
        this(context, s3Manager, identityManager, serviceManager, pushManager, ocrPurchaseTracker, new OcrPushMessageReceiver(), FeatureFlags.Ocr);
    }

    @VisibleForTesting
    OcrInteractor(@NonNull Context context, @NonNull S3Manager s3Manager, @NonNull IdentityManager identityManager,
                  @NonNull ServiceManager serviceManager, @NonNull PushManager pushManager, @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                  @NonNull OcrPushMessageReceiver pushMessageReceiver, @NonNull Feature ocrFeature) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.s3Manager = Preconditions.checkNotNull(s3Manager);
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.ocrServiceManager = Preconditions.checkNotNull(serviceManager);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);
        this.pushManager = Preconditions.checkNotNull(pushManager);
        this.pushMessageReceiver = Preconditions.checkNotNull(pushMessageReceiver);
        this.ocrFeature = Preconditions.checkNotNull(ocrFeature);
    }

    public void initialize() {
        ocrPurchaseTracker.initialize();
    }

    @NonNull
    public Observable<OcrResponse> scan(@NonNull File file) {
        Preconditions.checkNotNull(file);

        if (ocrFeature.isEnabled() && identityManager.isLoggedIn() && ocrPurchaseTracker.hasAvailableScans()) {
            Logger.info(OcrInteractor.this, "Initiating scan of {}.", file);
            return s3Manager.upload(file, OCR_FOLDER)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(new Func1<String, Observable<RecognitionResponse>>() {
                        @Override
                        public Observable<RecognitionResponse> call(@NonNull String s3Url) {
                            return ocrServiceManager.getService(OcrService.class).scanReceipt(new RecongitionRequest(s3Url));
                        }
                    })
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            pushManager.registerReceiver(pushMessageReceiver);
                        }
                    })
                    .flatMap(new Func1<RecognitionResponse, Observable<OcrResponse>>() {
                        @Override
                        public Observable<OcrResponse> call(RecognitionResponse recognitionResponse) {
                            // TODO: Here's where we should wait for the push result to come in to validate everything
                            // TODO: Also include a timeout here so it doesn't take more than say 7 seconds or so
                            return Observable.just(new OcrResponse());
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, OcrResponse>() {
                        @Override
                        public OcrResponse call(Throwable throwable) {
                            return new OcrResponse();
                        }
                    })
                    .doOnTerminate(new Action0() {
                        @Override
                        public void call() {
                            pushManager.unregisterReceiver(pushMessageReceiver);
                        }
                    });
        } else {
            Logger.debug(OcrInteractor.this, "Ignoring ocr scan of as: isFeatureEnabled = {}, isLoggedIn = {}, hasAvailableScans = {}.", ocrFeature.isEnabled(), identityManager.isLoggedIn(), ocrPurchaseTracker.hasAvailableScans());
            return Observable.just(new OcrResponse());
        }
    }
}
