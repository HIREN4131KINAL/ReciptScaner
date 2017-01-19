package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.text.TextUtils;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableGenerator;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private static final float EPSILON = 0.0001f;

    private List<Distance> distances;
    private List<Column<Receipt>> columns;

    // TODO null
    private Filter<Receipt> receiptFilter;

    private PdfBoxWriter writer;
    private Preferences preferences;

    protected PdfBoxReceiptsTablePdfSection(PdfBoxContext context,
                                            PDDocument doc,
                                            List<Distance> distances,
                                            List<Column<Receipt>> columns) {
        super(context, doc);
        this.distances = distances;
        this.columns = columns;
        this.preferences = context.getPreferences();
    }

    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) throws IOException {


        ReceiptsReportTableData data = new ReceiptsReportTableData(trip,
                receipts, distances, preferences);


        writer = new PdfBoxWriter(doc, context, new DefaultPdfBoxPageDecorations(context));

        writeHeader(trip, data);

        writer.verticalJump(40);

        writeTable(trip, receipts, data);

        writer.writeAndClose();
    }

    private void writeHeader(Trip trip, ReceiptsReportTableData data) throws IOException {

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

        if (preferences.onlyIncludeReimbursableReceiptsInReports() &&
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

        // TODO


        String fromToPeriod = context.getString(R.string.report_header_from,
                trip.getFormattedStartDate(context.getApplicationContext(), preferences.getDateSeparator()))
                + " "
                + context.getString(R.string.report_header_to,
                trip.getFormattedEndDate(context.getApplicationContext(), preferences.getDateSeparator()));

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

    private void writeTable(Trip trip,
                            List<Receipt> receipts,
                            ReceiptsReportTableData data) throws IOException {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (preferences.getPrintDistanceAsDailyReceipt()) {
            // TODO
//            receiptsTableList.addAll(new DistanceToReceiptsConverter(getContext(), getPreferences()).convert(getDatabase().getDistanceTable().getBlocking(trip, false)));
//            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }


        final PdfBoxTableGenerator<Receipt> pdfTableGenerator =
                new PdfBoxTableGenerator<>(context, columns,
                        receiptFilter, true, false);

        PdfBoxTable table = pdfTableGenerator.generate(receipts);

        writer.writeTable(table);
    }


}
