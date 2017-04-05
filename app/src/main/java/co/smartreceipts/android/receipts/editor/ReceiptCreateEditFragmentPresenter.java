package co.smartreceipts.android.receipts.editor;

import java.io.File;
import java.sql.Date;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

@FragmentScope
public class ReceiptCreateEditFragmentPresenter {

    @Inject
    ReceiptCreateEditFragment fragment;
    @Inject
    UserPreferenceManager preferenceManager;
    @Inject
    PurchaseManager purchaseManager;
    @Inject
    PurchaseWallet purchaseWallet;
    @Inject
    ReceiptTableController receiptTableController;

    @Inject
    public ReceiptCreateEditFragmentPresenter() {
    }

    public boolean isIncludeTaxField() {
        return preferenceManager.get(UserPreference.Receipts.IncludeTaxField);
    }

    public boolean isUsePreTaxPrice() {
        return preferenceManager.get(UserPreference.Receipts.UsePreTaxPrice);
    }

    public float getDefaultTaxPercentage() {
        return preferenceManager.get(UserPreference.Receipts.DefaultTaxPercentage);
    }

    public boolean isReceiptDateDefaultsToReportStartDate() {
        return preferenceManager.get(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate);
    }

    public boolean isReceiptsDefaultAsReimbursable() {
        return preferenceManager.get(UserPreference.Receipts.ReceiptsDefaultAsReimbursable);
    }

    public boolean isMatchReceiptCommentToCategory() {
        return preferenceManager.get(UserPreference.Receipts.MatchReceiptCommentToCategory);
    }

    public boolean isMatchReceiptNameToCategory() {
        return preferenceManager.get(UserPreference.Receipts.MatchReceiptNameToCategory);
    }

    public String getDefaultCurrency() {
        return preferenceManager.get(UserPreference.General.DefaultCurrency);
    }

    public boolean isDefaultToFullPage() {
        return preferenceManager.get(UserPreference.Receipts.DefaultToFullPage);
    }

    public String getDateSeparator() {
        return preferenceManager.get(UserPreference.General.DateSeparator);
    }

    public boolean isPredictCategories() {
        return preferenceManager.get(UserPreference.Receipts.PredictCategories);
    }

    public boolean isUsePaymentMethods() {
        return preferenceManager.get(UserPreference.Receipts.UsePaymentMethods);
    }

    public boolean isShowReceiptId() {
        return preferenceManager.get(UserPreference.Receipts.ShowReceiptID);
    }

    public boolean isEnableAutoCompleteSuggestions() {
        return preferenceManager.get(UserPreference.Receipts.EnableAutoCompleteSuggestions);
    }

    public void initiatePurchase() {
        purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
    }

    public boolean hasActivePlusPurchase() {
        return purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
    }

    public boolean checkReceipt(Date date, Trip parentTrip) {
        if (date == null) {
            fragment.showDateError();
            return false;
        }

        if (!parentTrip.isDateInsideTripBounds(date)) {
            fragment.showDateWarning();
        }

        return true;
    }

    public void saveReceipt(Receipt receipt, Trip parentTrip, Date date, String price, String tax,
                            String exchangeRate, String comment, PaymentMethod paymentMethod,
                            boolean isReimursable, boolean isFullpage,
                            String name, Category category, String currency,
                            String extraText1, String extraText2, String extraText3, File file) {

        final ReceiptBuilderFactory builderFactory = (receipt == null) ? new ReceiptBuilderFactory(-1) : new ReceiptBuilderFactory(receipt);
        builderFactory.setName(name)
                .setTrip(parentTrip)
                .setDate((Date) date.clone())
                .setPrice(price)
                .setTax(tax)
                .setExchangeRate(new ExchangeRateBuilderFactory().setBaseCurrency(currency)
                        .setRate(parentTrip.getTripCurrency(), exchangeRate)
                        .build())
                .setCategory(category)
                .setCurrency(currency)
                .setComment(comment)
                .setPaymentMethod(paymentMethod)
                .setIsReimbursable(isReimursable)
                .setIsFullPage(isFullpage)
                .setExtraEditText1(extraText1)
                .setExtraEditText2(extraText2)
                .setExtraEditText3(extraText3);

        if (receipt == null) {
            receiptTableController.insert(builderFactory.setFile(file).build(), new DatabaseOperationMetadata());
        } else {
            receiptTableController.update(receipt, builderFactory.build(), new DatabaseOperationMetadata());
        }
    }
}
