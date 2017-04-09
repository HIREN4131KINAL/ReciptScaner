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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OcrResponse)) return false;

        OcrResponse that = (OcrResponse) o;

        if (totalAmount != null ? !totalAmount.equals(that.totalAmount) : that.totalAmount != null)
            return false;
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null)
            return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null)
            return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (merchant != null ? !merchant.equals(that.merchant) : that.merchant != null)
            return false;
        if (confidenceLevel != null ? !confidenceLevel.equals(that.confidenceLevel) : that.confidenceLevel != null)
            return false;
        return error != null ? error.equals(that.error) : that.error == null;

    }

    @Override
    public int hashCode() {
        int result = totalAmount != null ? totalAmount.hashCode() : 0;
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (merchant != null ? merchant.hashCode() : 0);
        result = 31 * result + (confidenceLevel != null ? confidenceLevel.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
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
