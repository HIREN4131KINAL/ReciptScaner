package co.smartreceipts.android.purchases.apis;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PurchaseRequest {

    private String signature;
    private JsonObject receipt;
    private String pay_service;
    private String goal;

    public PurchaseRequest(@NonNull String signature, @NonNull String inAppPurchaseData, @NonNull String goal) {
        this.signature = Preconditions.checkNotNull(signature);
        this.goal = Preconditions.checkNotNull("Recognition");
        this.receipt = new JsonParser().parse(Preconditions.checkNotNull(inAppPurchaseData)).getAsJsonObject();
        this.pay_service = "Google Play";
    }

}
