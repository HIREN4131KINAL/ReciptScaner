package co.smartreceipts.android.identity.apis.organizations;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;

public class AppSettings {

    @SerializedName("Configurations")
    private Configurations configurations;
    @SerializedName("Settings")
    private String settings;
    @SerializedName("Categories")
    private List<Category> categories;
    @SerializedName("PaymentMethods")
    private List<PaymentMethod> paymentMethods;
    @SerializedName("CSVColumns")
    private List<Column<Receipt>> csvColumns;
    @SerializedName("PDFColumns")
    private List<Column<Receipt>> pdfColumns;

    @Nullable
    public Configurations getConfigurations() {
        return configurations;
    }

    @Nullable
    public String getSettings() {
        return settings;
    }

    @Nullable
    public List<Category> getCategories() {
        return categories;
    }

    @Nullable
    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    @Nullable
    public List<Column<Receipt>> getCsvColumns() {
        return csvColumns;
    }

    @Nullable
    public List<Column<Receipt>> getPdfColumns() {
        return pdfColumns;
    }
}
