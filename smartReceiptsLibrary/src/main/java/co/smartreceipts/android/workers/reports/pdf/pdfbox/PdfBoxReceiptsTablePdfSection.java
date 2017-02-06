package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.comparators.ReceiptDateComparator;
import co.smartreceipts.android.model.converters.DistanceToReceiptsConverter;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableGenerator;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private static final float EPSILON = 0.0001f;
    private final List<Receipt> receipts;
    private List<Column<Receipt>> receiptColumns;

    @Nullable
    private List<Distance> distances;
    @Nullable
    private final List<Column<Distance>> distanceColumns;

    private PdfBoxWriter writer;
    private Preferences preferences;

    protected PdfBoxReceiptsTablePdfSection(PdfBoxContext context,
                                            Trip trip,
                                            List<Receipt> receipts,
                                            List<Column<Receipt>> receiptColumns,
                                            @Nullable List<Distance> distances,
                                            @Nullable List<Column<Distance>> distanceColumns) {
        super(context, trip);
        this.receipts = receipts;
        this.distances = distances;
        this.receiptColumns = receiptColumns;
        this.preferences = context.getPreferences();
        this.distanceColumns = distanceColumns;
    }



    @Override
    public void writeSection(PDDocument doc) throws IOException {

        ReceiptsTotals totals = new ReceiptsTotals(trip,
                receipts, distances, preferences);

        // switch to landscape mode
        if (preferences.isReceiptsTableLandscapeMode()) {
            context.setPageSize(new PDRectangle(context.getPageSize().getHeight(),
                    context.getPageSize().getWidth()));
        }

        writer = new PdfBoxWriter(doc, context, new DefaultPdfBoxPageDecorations(context));

        writeHeader(trip, totals);

        writer.verticalJump(40);

        writeReceiptsTable(receipts);

        if (preferences.getPrintDistanceTable() && distances != null && !distances.isEmpty()) {
            writer.verticalJump(60);

            writeDistancesTable(distances);
        }

        writer.writeAndClose();

        // reset the page size if necessary
        if (preferences.isReceiptsTableLandscapeMode()) {
            context.setPageSize(new PDRectangle(context.getPageSize().getHeight(),
                    context.getPageSize().getWidth()));
        }
    }

    private void writeHeader(Trip trip, ReceiptsTotals data) throws IOException {

        writer.openTextBlock();

        writer.writeNewLine(context.getFont("FONT_TITLE"),
                trip.getName()
        );


        if (!data.receiptsPrice.equals(data.netPrice)) {
            writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                    R.string.report_header_receipts_total,
                    data.receiptsPrice.getCurrencyFormattedPrice()
            );
        }

        if (preferences.includeTaxField()) {
            if (preferences.usePreTaxPrice() && data.taxPrice.getPriceAsFloat() > EPSILON) {
                writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                        R.string.report_header_receipts_total_tax,
                        data.taxPrice.getCurrencyFormattedPrice()
                );

            } else if (!data.noTaxPrice.equals(data.receiptsPrice) &&
                    data.noTaxPrice.getPriceAsFloat() > EPSILON) {
                writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                        R.string.report_header_receipts_total_no_tax,
                        data.noTaxPrice.getCurrencyFormattedPrice()
                );
            }
        }

        if (!preferences.onlyIncludeReimbursableReceiptsInReports() &&
                !data.reimbursablePrice.equals(data.receiptsPrice)) {
            writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                    R.string.report_header_receipts_total_reimbursable,
                    data.reimbursablePrice.getCurrencyFormattedPrice()
            );
        }
        if (distances.size() > 0) {
            writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                    R.string.report_header_distance_total,
                    data.distancePrice.getCurrencyFormattedPrice()
            );
        }

        writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                R.string.report_header_gross_total,
                data.netPrice.getCurrencyFormattedPrice()
        );

        String fromToPeriod = context.getString(R.string.report_header_from,
                trip.getFormattedStartDate(context.getAndroidContext(), preferences.getDateSeparator()))
                + " "
                + context.getString(R.string.report_header_to,
                trip.getFormattedEndDate(context.getAndroidContext(), preferences.getDateSeparator()));

        writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                fromToPeriod);


        if (preferences.getIncludeCostCenter() && !TextUtils.isEmpty(trip.getCostCenter())) {
            writer.writeNewLine(context.getFont("FONT_DEFAULT"),
                    R.string.report_header_cost_center,
                    trip.getCostCenter()
            );
        }
        if (!TextUtils.isEmpty(trip.getComment())) {
            writer.writeNewLine(
                    context.getFont("FONT_DEFAULT"),
                    R.string.report_header_comment,
                    trip.getComment()
            );
        }

        writer.closeTextBlock();
    }

    private void writeReceiptsTable(List<Receipt> receipts) throws IOException {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (preferences.getPrintDistanceAsDailyReceipt()) {
            receiptsTableList.addAll(
                    new DistanceToReceiptsConverter(context.getAndroidContext(), preferences)
                    .convert(distances));
            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }


        final PdfBoxTableGenerator<Receipt> pdfTableGenerator =
                new PdfBoxTableGenerator<>(context, receiptColumns,
                        new LegacyReceiptFilter(preferences), true, false);

        PdfBoxTable table = pdfTableGenerator.generate(receiptsTableList);

        writer.writeTable(table);
    }

    private void writeDistancesTable(List<Distance> distances) throws IOException {


        final PdfBoxTableGenerator<Distance> pdfTableGenerator =
                new PdfBoxTableGenerator<>(context, distanceColumns,
                        null, true, true);


        PdfBoxTable table = pdfTableGenerator.generate(distances);

        writer.writeTable(table);
    }


}
