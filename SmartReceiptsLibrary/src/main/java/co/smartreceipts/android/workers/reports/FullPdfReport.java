package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.comparators.ReceiptDateComparator;
import co.smartreceipts.android.model.converters.DistanceToReceiptsConverter;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.distance.DistanceColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.tables.PdfTableGenerator;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

/**
 * Creates a full pdf report
 */
public final class FullPdfReport extends AbstractPdfImagesReport {

    private static final float EPSILON = 0.0001f;

    public FullPdfReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex) {
        super(context, persistenceManager, flex);
    }

    public FullPdfReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull Preferences preferences, @NonNull StorageManager storageManager, Flex flex) {
        super(context, db, preferences, storageManager, flex);
    }

    @Override
    protected String getFileName(@NonNull Trip trip) {
        return trip.getDirectory().getName() + ".pdf";
    }

    @Override
    protected void generateInitialPages(@NonNull Document document, @NonNull List<Receipt> receipts, @NonNull Trip trip) throws ReportGenerationException {
        final List<Distance> distances = new ArrayList<Distance>(getDatabase().getDistanceSerial(trip));
        Collections.reverse(distances); // Reverse the list, so we start with the earliest one

        // Pre-tax => receipt total does not include price
        final boolean usePrexTaxPrice = getPreferences().getUsesPreTaxPrice();
        final boolean onlyUseExpensable = getPreferences().onlyIncludeExpensableReceiptsInReports();
        final ArrayList<Price> netTotal = new ArrayList<Price>(receipts.size());
        final ArrayList<Price> receiptTotal = new ArrayList<Price>(receipts.size());
        final ArrayList<Price> expensableTotal = new ArrayList<Price>(receipts.size());
        final ArrayList<Price> noTaxesTotal = new ArrayList<Price>(receipts.size() * 2);
        final ArrayList<Price> taxesTotal = new ArrayList<Price>(receipts.size() * 2);
        final ArrayList<Price> distanceTotal = new ArrayList<Price>(distances.size());

        // Sum up our receipt totals for various conditions
        final int len = receipts.size();
        for (int i = 0; i < len; i++) {
            final Receipt receipt = receipts.get(i);
            if (!onlyUseExpensable || receipt.isExpensable()) {
                netTotal.add(receipt.getPrice());
                receiptTotal.add(receipt.getPrice());
                // Treat taxes as negative prices for the sake of this conversion
                noTaxesTotal.add(receipt.getPrice());
                noTaxesTotal.add(new PriceBuilderFactory().setCurrency(receipt.getTax().getCurrency()).setPrice(receipt.getTax().getPrice().multiply(new BigDecimal(-1))).build());
                taxesTotal.add(receipt.getTax());
                if (usePrexTaxPrice) {
                    netTotal.add(receipt.getTax());
                }
                if (receipt.isExpensable()) {
                    expensableTotal.add(receipt.getPrice());
                }
            }
        }

        // Sum up our distance totals
        for (int i = 0; i < distances.size(); i++) {
            final Distance distance = distances.get(i);
            netTotal.add(distance.getPrice());
            distanceTotal.add(distance.getPrice());
        }

        final WBCurrency tripCurrency = trip.getTripCurrency();
        final Price netPrice = new PriceBuilderFactory().setPrices(netTotal, tripCurrency).build();
        final Price receiptsPrice = new PriceBuilderFactory().setPrices(receiptTotal, tripCurrency).build();
        final Price expensablePrice = new PriceBuilderFactory().setPrices(expensableTotal, tripCurrency).build();
        final Price noTaxPrice = new PriceBuilderFactory().setPrices(noTaxesTotal, tripCurrency).build();
        final Price taxPrice = new PriceBuilderFactory().setPrices(taxesTotal, tripCurrency).build();
        final Price distancePrice = new PriceBuilderFactory().setPrices(distanceTotal, tripCurrency).build();


        try {
            // Add the table (TODO: Use formatting at some point so it doesn't look like crap)
            document.add(new Paragraph(trip.getName() + "\n"));
            if (!receiptsPrice.equals(netPrice)) {
                document.add(new Paragraph(getContext().getString(R.string.report_header_receipts_total, receiptsPrice.getCurrencyFormattedPrice()) + "\n"));
            }
            if (getPreferences().includeTaxField()) {
                if (usePrexTaxPrice && taxPrice.getPriceAsFloat() > EPSILON) {
                    document.add(new Paragraph(getContext().getString(R.string.report_header_receipts_total_tax, taxPrice.getCurrencyFormattedPrice()) + "\n"));
                } else if (!noTaxPrice.equals(receiptsPrice) && noTaxPrice.getPriceAsFloat() > EPSILON) {
                    document.add(new Paragraph(getContext().getString(R.string.report_header_receipts_total_no_tax, noTaxPrice.getCurrencyFormattedPrice()) + "\n"));
                }
            }
            if (!getPreferences().onlyIncludeExpensableReceiptsInReports() && !expensablePrice.equals(receiptsPrice)) {
                document.add(new Paragraph(getContext().getString(R.string.report_header_receipts_total_expensable, expensablePrice.getCurrencyFormattedPrice()) + "\n"));
            }
            if (distances.size() > 0) {
                document.add(new Paragraph(getContext().getString(R.string.report_header_distance_total, distancePrice.getCurrencyFormattedPrice()) + "\n"));
            }
            document.add(new Paragraph(getContext().getString(R.string.report_header_gross_total, netPrice.getCurrencyFormattedPrice()) + "\n"));
            document.add(new Paragraph(getContext().getString(R.string.report_header_from, trip.getFormattedStartDate(getContext(), getPreferences().getDateSeparator())) + " "
                    + getContext().getString(R.string.report_header_to, trip.getFormattedEndDate(getContext(), getPreferences().getDateSeparator())) + "\n"));
            if (getPreferences().getIncludeCostCenter() && !TextUtils.isEmpty(trip.getCostCenter())) {
                document.add(new Paragraph(getContext().getString(R.string.report_header_cost_center, trip.getCostCenter()) + "\n"));
            }
            if (!TextUtils.isEmpty(trip.getComment())) {
                document.add(new Paragraph(getContext().getString(R.string.report_header_comment, trip.getComment()) + "\n"));
            }
            document.add(new Paragraph("\n\n")); // Add the line break before our table

            // Now build the table
            final List<Column<Receipt>> columns = getDatabase().getPDFColumns();
            final List<Receipt> receiptsTableList = new ArrayList<Receipt>(receipts);
            if (getPreferences().getPrintDistanceAsDailyReceipt()) {
                receiptsTableList.addAll(new DistanceToReceiptsConverter(getContext(), getPreferences()).convert(getDatabase().getDistanceSerial(trip)));
                Collections.sort(receiptsTableList, new ReceiptDateComparator());
            }
            final PdfTableGenerator<Receipt> pdfTableGenerator = new PdfTableGenerator<Receipt>(columns, new LegacyReceiptFilter(getPreferences()), true, false);
            document.add(pdfTableGenerator.generate(receiptsTableList));

            if (getPreferences().getPrintDistanceTable() && !distances.isEmpty()) {
                // PDFs can print special characters
                final ColumnDefinitions<Distance> distanceColumnDefinitions = new DistanceColumnDefinitions(getContext(), getDatabase(), getPreferences(), getFlex(), true);
                final List<Column<Distance>> distanceColumns = distanceColumnDefinitions.getAllColumns();
                document.add(new Paragraph("\n\n"));
                document.add(new PdfTableGenerator<>(distanceColumns, true, true).generate(distances));
            }
            document.newPage();
        } catch (DocumentException e) {
            throw new ReportGenerationException(e);
        }
    }
}
