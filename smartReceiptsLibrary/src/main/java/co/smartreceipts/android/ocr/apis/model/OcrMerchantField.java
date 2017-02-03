package co.smartreceipts.android.ocr.apis.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class OcrMerchantField implements Serializable {

    private final String name;
    private final String address;
    private final Double confidenceLevel;

    public OcrMerchantField(String name, String address, Double confidenceLevel) {
        this.name = name;
        this.address = address;
        this.confidenceLevel = confidenceLevel;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getAddress() {
        return address;
    }

    @Nullable
    public Double getConfidenceLevel() {
        return confidenceLevel;
    }

    @Override
    public String toString() {
        return "OcrMerchantField{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", confidenceLevel=" + confidenceLevel +
                '}';
    }
}
