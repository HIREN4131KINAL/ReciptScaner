package co.smartreceipts.android.identity.apis.organizations;

import com.google.gson.annotations.SerializedName;

public class RemoteSettingsDefinitions {

    @SerializedName("TripDuration")
    private final String tripDuration;

    @SerializedName("isocurr")
    private final String isocurr;

    @SerializedName("dateseparator")
    private final String dateseparator;

    @SerializedName("trackcostcenter")
    private final String trackcostcenter;

    @SerializedName("PredictCats")
    private final String predictCats;

    @SerializedName("MatchNameCats")
    private final String matchNameCats;

    @SerializedName("MatchCommentCats")
    private final String matchCommentCats;

    @SerializedName("OnlyIncludeExpensable")
    private final String onlyIncludeExpensable;

    @SerializedName("ExpensableDefault")
    private final String expensableDefault;

    @SerializedName("IncludeTaxField")
    private final String includeTaxField;

    @SerializedName("TaxPercentage")
    private final String taxPercentage;

    @SerializedName("PreTax")
    private final String preTax;

    @SerializedName("EnableAutoCompleteSuggestions")
    private final String enableAutoCompleteSuggestions;

    @SerializedName("MinReceiptPrice")
    private final String minReceiptPrice;

    @SerializedName("DefaultToFirstReportDate")
    private final String defaultToFirstReportDate;

    @SerializedName("ShowReceiptID")
    private final String showReceiptID;

    @SerializedName("UseFullPage")
    private final String useFullPage;

    @SerializedName("UsePaymentMethods")
    private final String usePaymentMethods;

    @SerializedName("IncludeCSVHeaders")
    private final String includeCSVHeaders;

    @SerializedName("PrintByIDPhotoKey")
    private final String printByIDPhotoKey;

    @SerializedName("PrintCommentByPhoto")
    private final String printCommentByPhoto;

    @SerializedName("EmailTo")
    private final String emailTo;

    @SerializedName("EmailCC")
    private final String emailCC;

    @SerializedName("EmailBCC")
    private final String emailBCC;

    @SerializedName("EmailSubject")
    private final String emailSubject;

    @SerializedName("SaveBW")
    private final String saveBW;

    @SerializedName("LayoutIncludeReceiptDate")
    private final String layoutIncludeReceiptDate;

    @SerializedName("LayoutIncludeReceiptCategory")
    private final String layoutIncludeReceiptCategory;

    @SerializedName("LayoutIncludeReceiptPicture")
    private final String layoutIncludeReceiptPicture;

    @SerializedName("MileageTotalInReport")
    private final String mileageTotalInReport;

    @SerializedName("MileageRate")
    private final String mileageRate;

    @SerializedName("MileagePrintTable")
    private final String mileagePrintTable;

    @SerializedName("MileageAddToPDF")
    private final String mileageAddToPDF;

    @SerializedName("PdfFooterString")
    private final String pdfFooterString;

    public RemoteSettingsDefinitions(String tripDuration, String isocurr, String dateseparator, String trackcostcenter, String predictCats,
                                     String matchNameCats, String matchCommentCats, String onlyIncludeExpensable, String expensableDefault,
                                     String includeTaxField, String taxPercentage, String preTax, String enableAutoCompleteSuggestions,
                                     String minReceiptPrice, String defaultToFirstReportDate, String showReceiptID, String useFullPage,
                                     String usePaymentMethods, String includeCSVHeaders, String printByIDPhotoKey, String printCommentByPhoto,
                                     String emailTo, String emailCC, String emailBCC, String emailSubject, String saveBW, String layoutIncludeReceiptDate,
                                     String layoutIncludeReceiptCategory, String layoutIncludeReceiptPicture, String mileageTotalInReport,
                                     String mileageRate, String mileagePrintTable, String mileageAddToPDF, String pdfFooterString) {
        this.tripDuration = tripDuration;
        this.isocurr = isocurr;
        this.dateseparator = dateseparator;
        this.trackcostcenter = trackcostcenter;
        this.predictCats = predictCats;
        this.matchNameCats = matchNameCats;
        this.matchCommentCats = matchCommentCats;
        this.onlyIncludeExpensable = onlyIncludeExpensable;
        this.expensableDefault = expensableDefault;
        this.includeTaxField = includeTaxField;
        this.taxPercentage = taxPercentage;
        this.preTax = preTax;
        this.enableAutoCompleteSuggestions = enableAutoCompleteSuggestions;
        this.minReceiptPrice = minReceiptPrice;
        this.defaultToFirstReportDate = defaultToFirstReportDate;
        this.showReceiptID = showReceiptID;
        this.useFullPage = useFullPage;
        this.usePaymentMethods = usePaymentMethods;
        this.includeCSVHeaders = includeCSVHeaders;
        this.printByIDPhotoKey = printByIDPhotoKey;
        this.printCommentByPhoto = printCommentByPhoto;
        this.emailTo = emailTo;
        this.emailCC = emailCC;
        this.emailBCC = emailBCC;
        this.emailSubject = emailSubject;
        this.saveBW = saveBW;
        this.layoutIncludeReceiptDate = layoutIncludeReceiptDate;
        this.layoutIncludeReceiptCategory = layoutIncludeReceiptCategory;
        this.layoutIncludeReceiptPicture = layoutIncludeReceiptPicture;
        this.mileageTotalInReport = mileageTotalInReport;
        this.mileageRate = mileageRate;
        this.mileagePrintTable = mileagePrintTable;
        this.mileageAddToPDF = mileageAddToPDF;
        this.pdfFooterString = pdfFooterString;
    }

}
