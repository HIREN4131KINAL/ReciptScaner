package co.smartreceipts.android.purchases.apis;

import android.support.annotation.NonNull;

import co.smartreceipts.android.identity.apis.login.LoginPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface MobileAppPurchasesService {

    @POST("api/mobile_app_purchases")
    Observable<PurchaseResponse> addPurchase(@NonNull @Body PurchaseRequest purchaseRequest);
}
