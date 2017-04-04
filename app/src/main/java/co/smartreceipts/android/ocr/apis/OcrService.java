package co.smartreceipts.android.ocr.apis;

import android.support.annotation.NonNull;

import co.smartreceipts.android.ocr.apis.model.RecognitionResponse;
import co.smartreceipts.android.ocr.apis.model.RecongitionRequest;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface OcrService {

    @POST("api/recognitions")
    Observable<RecognitionResponse> scanReceipt(@NonNull @Body RecongitionRequest request);
}
