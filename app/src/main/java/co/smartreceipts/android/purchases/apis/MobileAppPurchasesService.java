package co.smartreceipts.android.purchases.apis;

import android.support.annotation.NonNull;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface MobileAppPurchasesService {

    @POST("api/mobile_app_purchases")
    Observable<PurchaseResponse> addPurchase(@NonNull @Body PurchaseRequest purchaseRequest);
}
