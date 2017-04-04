package co.smartreceipts.android.identity.apis.organizations;

import com.google.gson.annotations.SerializedName;

public class RemoteSettingsDefinitions {

    @SerializedName("TripDuration")
    private String tripDuration;

    @SerializedName("isocurr")
    private String isocurr;

    @SerializedName("dateseparator")
    private String dateseparator;

    @SerializedName("trackcostcenter")
    private String trackcostcenter;

    @SerializedName("PredictCats")
    private String predictCats;

    @SerializedName("MatchNameCats")
    private String matchNameCats;

    @SerializedName("MatchCommentCats")
    private String matchCommentCats;

    @SerializedName("OnlyIncludeExpensable")
    private String onlyIncludeExpensable;

    @SerializedName("ExpensableDefault")
    private String expensableDefault;

    @SerializedName("IncludeTaxField")
    private String includeTaxField;

    @SerializedName("TaxPercentage")
    private String taxPercentage;

    @SerializedName("PreTax")
    private String preTax;

    @SerializedName("EnableAutoCompleteSuggestions")
    private String enableAutoCompleteSuggestions;

    @SerializedName("MinReceiptPrice")
    private String minReceiptPrice;

    @SerializedName("DefaultToFirstReportDate")
    private String defaultToFirstReportDate;

    @SerializedName("ShowReceiptID")
    private String showReceiptID;

    @SerializedName("UseFullPage")
    private String useFullPage;

    @SerializedName("UsePaymentMethods")
    private String usePaymentMethods;

    @SerializedName("IncludeCSVHeaders")
    private String includeCSVHeaders;

    @SerializedName("PrintByIDPhotoKey")
    private String printByIDPhotoKey;

    @SerializedName("PrintCommentByPhoto")
    private String printCommentByPhoto;

    @SerializedName("EmailTo")
    private String emailTo;

    @SerializedName("EmailCC")
    private String emailCC;

    @SerializedName("EmailBCC")
    private String emailBCC;

    @SerializedName("EmailSubject")
    private String emailSubject;

    @SerializedName("SaveBW")
    private String saveBW;

    @SerializedName("LayoutIncludeReceiptDate")
    private String layoutIncludeReceiptDate;

    @SerializedName("LayoutIncludeReceiptCategory")
    private String layoutIncludeReceiptCategory;

    @SerializedName("LayoutIncludeReceiptPicture")
    private String layoutIncludeReceiptPicture;

    @SerializedName("MileageTotalInReport")
    private String mileageTotalInReport;

    @SerializedName("MileageRate")
    private String mileageRate;

    @SerializedName("MileagePrintTable")
    private String mileagePrintTable;

    @SerializedName("MileageAddToPDF")
    private String mileageAddToPDF;

    @SerializedName("PdfFooterString")
    private String pdfFooterString;

}
