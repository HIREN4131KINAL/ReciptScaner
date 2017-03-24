package co.smartreceipts.android.ocr.apis.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class OcrResponse implements Serializable {

    private OcrResponseField<Double> totalAmount;
    private OcrResponseField<Double> taxAmount;
    private OcrResponseField<String> currency;
    private OcrResponseField<String> date;
    private OcrMerchantField merchant;
    private Double confidenceLevel;
    private String error;

    public OcrResponse() {
        this.totalAmount = null;
        this.taxAmount = null;
        this.currency = null;
        this.date = null;
        this.merchant = null;
        this.confidenceLevel = null;
        this.error = null;
    }

    @Nullable
    public OcrResponseField<Double> getTotalAmount() {
        return totalAmount;
    }

    @Nullable
    public OcrResponseField<Double> getTaxAmount() {
        return taxAmount;
    }

    @Nullable
    public OcrResponseField<String> getCurrency() {
        return currency;
    }

    @Nullable
    public OcrResponseField<String> getDate() {
        return date;
    }

    @Nullable
    public OcrMerchantField getMerchant() {
        return merchant;
    }

    @Nullable
    public Double getConfidenceLevel() {
        return confidenceLevel;
    }

    @Nullable
    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "OcrResponse{" +
                "totalAmount=" + totalAmount +
                ", taxAmount=" + taxAmount +
                ", currency='" + currency + '\'' +
                ", date='" + date + '\'' +
                ", confidenceLevel=" + confidenceLevel +
                ", error='" + error + '\'' +
                '}';
    }
}
