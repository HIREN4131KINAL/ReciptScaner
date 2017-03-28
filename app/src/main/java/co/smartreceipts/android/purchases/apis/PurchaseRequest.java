package co.smartreceipts.android.purchases.apis;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.smartreceipts.android.purchases.model.ManagedProduct;

public class PurchaseRequest {

    private String signature;
    private JsonObject receipt;
    private String pay_service;
    private String goal;

    public PurchaseRequest(@NonNull ManagedProduct managedProduct, @NonNull String goal) {
        this.signature = Preconditions.checkNotNull(managedProduct.getInAppDataSignature());
        this.goal = Preconditions.checkNotNull(goal);
        this.receipt = new JsonParser().parse(Preconditions.checkNotNull(managedProduct.getPurchaseData())).getAsJsonObject();
        this.pay_service = "Google Play";
    }

}
