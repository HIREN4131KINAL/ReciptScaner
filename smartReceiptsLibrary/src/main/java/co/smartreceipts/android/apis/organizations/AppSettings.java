package co.smartreceipts.android.apis.organizations;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;

public class AppSettings {

    @SerializedName("Configurations")
    private final Configurations configurations;
    @SerializedName("Settings")
    private final RemoteSettingsDefinitions mRemoteSettingsDefinitions;
    @SerializedName("Categories")
    private final List<Category> categories;
    @SerializedName("PaymentMethods")
    private final List<PaymentMethod> paymentMethods;
    @SerializedName("CSVColumns")
    private final List<Column<Receipt>> csvColumns;
    @SerializedName("PDFColumns")
    private final List<Column<Receipt>> pdfColumns;

    public AppSettings(Configurations configurations, RemoteSettingsDefinitions remoteSettingsDefinitions, List<Category> categories, List<PaymentMethod> paymentMethods, List<Column<Receipt>> csvColumns, List<Column<Receipt>> pdfColumns) {
        this.configurations = configurations;
        this.mRemoteSettingsDefinitions = remoteSettingsDefinitions;
        this.categories = categories;
        this.paymentMethods = paymentMethods;
        this.csvColumns = csvColumns;
        this.pdfColumns = pdfColumns;
    }

    @Nullable
    public Configurations getConfigurations() {
        return configurations;
    }

    @Nullable
    public RemoteSettingsDefinitions getRemoteSettingsDefinitions() {
        return mRemoteSettingsDefinitions;
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
