package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

/**
 * Encapsulates the calculations and data of receipts totals.
 * Independent of the pdf library implementation.
 */
public class ReceiptsTotals {

    public final Price netPrice;
    public final Price receiptsPrice;
    public final Price reimbursablePrice;
    public final Price noTaxPrice;
    public final Price taxPrice;
    public final Price distancePrice;

    public ReceiptsTotals(@NonNull Trip trip, @NonNull List<Receipt> receipts, @NonNull List<Distance> distances, @NonNull UserPreferenceManager preferences) {
        Preconditions.checkNotNull(trip);
        Preconditions.checkNotNull(receipts);
        Preconditions.checkNotNull(distances);
        Preconditions.checkNotNull(preferences);

        final ArrayList<Price> netTotal = new ArrayList<>(receipts.size());
        final ArrayList<Price> receiptTotal = new ArrayList<>(receipts.size());
        final ArrayList<Price> reimbursableTotal = new ArrayList<>(receipts.size());
        final ArrayList<Price> noTaxesTotal = new ArrayList<>(receipts.size() * 2);
        final ArrayList<Price> taxesTotal = new ArrayList<>(receipts.size() * 2);
        final ArrayList<Price> distanceTotal = new ArrayList<>(distances.size());

        // Sum up our receipt totals for various conditions
        final int len = receipts.size();
        for (int i = 0; i < len; i++) {
            final Receipt receipt = receipts.get(i);
            if (!preferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) || receipt.isReimbursable()) {
                netTotal.add(receipt.getPrice());
                receiptTotal.add(receipt.getPrice());
                // Treat taxes as negative prices for the sake of this conversion
                noTaxesTotal.add(receipt.getPrice());
                noTaxesTotal.add(new PriceBuilderFactory().setCurrency(receipt.getTax().getCurrency()).setPrice(receipt.getTax().getPrice().multiply(new BigDecimal(-1))).build());
                taxesTotal.add(receipt.getTax());
                if (preferences.get(UserPreference.Receipts.UsePreTaxPrice)) {
                    netTotal.add(receipt.getTax());
                }
                if (receipt.isReimbursable()) {
                    reimbursableTotal.add(receipt.getPrice());
                }
            }
        }

        // Sum up our distance totals
        for (int i = 0; i < distances.size(); i++) {
            final Distance distance = distances.get(i);
            netTotal.add(distance.getPrice());
            distanceTotal.add(distance.getPrice());
        }


        final PriceCurrency tripCurrency = trip.getTripCurrency();
        netPrice = new PriceBuilderFactory().setPrices(netTotal, tripCurrency).build();
        receiptsPrice = new PriceBuilderFactory().setPrices(receiptTotal, tripCurrency).build();
        reimbursablePrice = new PriceBuilderFactory().setPrices(reimbursableTotal, tripCurrency).build();
        noTaxPrice = new PriceBuilderFactory().setPrices(noTaxesTotal, tripCurrency).build();
        taxPrice = new PriceBuilderFactory().setPrices(taxesTotal, tripCurrency).build();
        distancePrice = new PriceBuilderFactory().setPrices(distanceTotal, tripCurrency).build();


    }
}
