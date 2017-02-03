package co.smartreceipts.android.ocr.apis;

import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

public interface OcrService {

    @Multipart
    @POST("/api/receipt/v1/simple/file")
    Observable<OcrResponse> scanReceipt(@Part MultipartBody.Part filePart);
}
