package co.smartreceipts.android.ocr;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.io.File;

import co.smartreceipts.android.R;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.apis.OcrService;
import co.smartreceipts.android.ocr.apis.OcrServiceHostConfiguration;
import co.smartreceipts.android.utils.Feature;
import co.smartreceipts.android.utils.FeatureFlags;
import co.smartreceipts.android.utils.MultiDependencyFeature;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Func1;

public class OcrInteractor {

    private static final String PART_NAME = "file";

    private final Context context;
    private final ServiceManager ocrServiceManager;
    private final Feature ocrFeature;

    public OcrInteractor(@NonNull final Context context) {
        this(context, new ServiceManager(new OcrServiceHostConfiguration(context)),
                new MultiDependencyFeature(FeatureFlags.Ocr, new Feature() {
                    @Override
                    public boolean isEnabled() {
                        // Temporary work around until we get this moved server-side
                        final String key = context.getString(R.string.ocr_api_key);
                        return !TextUtils.isEmpty(key) && key.length() > 10;
                    }
                }));
    }



    public OcrInteractor(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull Feature ocrFeature) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.ocrServiceManager = Preconditions.checkNotNull(serviceManager);
        this.ocrFeature = Preconditions.checkNotNull(ocrFeature);
    }

    @NonNull
    public Observable<OcrResponse> scan(@NonNull File file) {
        Preconditions.checkNotNull(file);

        if (ocrFeature.isEnabled()) {
            Logger.info(OcrInteractor.this, "Initiating scan of {}.", file);
            final String mimeType = UriUtils.getMimeType(Uri.fromFile(file), context.getContentResolver());
            final RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
            final MultipartBody.Part filePart = MultipartBody.Part.createFormData(PART_NAME, file.getName(), requestBody);
            return ocrServiceManager.getService(OcrService.class)
                    .scanReceipt(filePart)
                    .onErrorReturn(new Func1<Throwable, OcrResponse>() {
                        @Override
                        public OcrResponse call(Throwable throwable) {
                            return new OcrResponse();
                        }
                    });
        } else {
            Logger.debug(OcrInteractor.this, "Ocr is disabled. Ignoring scan");
            return Observable.just(new OcrResponse());
        }
    }
}
