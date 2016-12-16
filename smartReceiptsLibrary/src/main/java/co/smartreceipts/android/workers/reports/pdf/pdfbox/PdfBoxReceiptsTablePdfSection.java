package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableGenerator;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private static final float EPSILON = 0.0001f;

    private List<Distance> distances;
    private List<Column<Receipt>> columns;

    private Filter<Receipt> receiptFilter;

    // TODO how to set these
    private boolean usePreTaxPrice;
    private boolean onlyUseReimbursable;
    private boolean includeTaxField;
    private boolean onlyIncludeReimbursableReceiptsInReports;
    private boolean includeCostCenter;
    private boolean printDistanceAsDailyReceipt;

    private float currentCursorPosition;

    protected PdfBoxReceiptsTablePdfSection(PdfBoxContext context,
                                            PDDocument doc,
                                            List<Distance> distances,
                                            List<Column<Receipt>> columns) {
        super(context, doc);
        this.distances = distances;
        this.columns = columns;
    }

    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) throws IOException {


        ReceiptsReportTableData data = new ReceiptsReportTableData(trip,
                receipts, distances, usePreTaxPrice, onlyUseReimbursable);

        PDPage page = new PDPage();
        doc.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(doc, page);


        contentStream.beginText();

        contentStream.newLineAtOffset(context.getPageMarginHorizontal(),
                page.getMediaBox().getHeight() - context.getPageMarginVertical());
        currentCursorPosition = page.getMediaBox().getHeight()  - context.getPageMarginVertical();

        writeHeader(contentStream, trip, data);

        contentStream.endText();

        writeTable(contentStream, trip, receipts, data);

        contentStream.close();
    }

    private void writeHeader(PDPageContentStream contentStream, Trip trip, ReceiptsReportTableData data) throws IOException {

        writeNewLine(contentStream,
                context.getFont("TITLE"),
                trip.getName()
        );


        if (!data.receiptsPrice.equals(data.netPrice)) {
            writeNewLine(contentStream,
                    context.getFont("DEFAULT"),
                    R.string.report_header_receipts_total,
                    data.receiptsPrice.getCurrencyFormattedPrice()
            );
        }

        if (includeTaxField) {
            if (usePreTaxPrice && data.taxPrice.getPriceAsFloat() > EPSILON) {
                writeNewLine(contentStream,
                        context.getFont("DEFAULT"),
                        R.string.report_header_receipts_total_tax,
                        data.taxPrice.getCurrencyFormattedPrice()
                );

            } else if (!data.noTaxPrice.equals(data.receiptsPrice) &&
                    data.noTaxPrice.getPriceAsFloat() > EPSILON) {
                writeNewLine(contentStream,
                        context.getFont("DEFAULT"),
                        R.string.report_header_receipts_total_no_tax,
                        data.noTaxPrice.getCurrencyFormattedPrice()
                );
            }
        }

        if (onlyIncludeReimbursableReceiptsInReports &&
                !data.reimbursablePrice.equals(data.receiptsPrice)) {
            writeNewLine(contentStream,
                    context.getFont("DEFAULT"),
                    R.string.report_header_receipts_total_reimbursable,
                    data.reimbursablePrice.getCurrencyFormattedPrice()
            );
        }
        if (distances.size() > 0) {
            writeNewLine(contentStream,
                    context.getFont("DEFAULT"),
                    R.string.report_header_distance_total,
                    data.distancePrice.getCurrencyFormattedPrice()
            );
        }

        writeNewLine(contentStream,
                context.getFont("DEFAULT"),
                R.string.report_header_gross_total,
                data.netPrice.getCurrencyFormattedPrice()
        );

        // TODO


        String fromToPeriod = context.getString(R.string.report_header_from,
                trip.getFormattedStartDate(context.getApplicationContext(), context.getDateSeparator()))
                + " "
                + context.getString(R.string.report_header_to,
                trip.getFormattedEndDate(context.getApplicationContext(), context.getDateSeparator()));

        writeNewLine(contentStream,
                context.getFont("DEFAULT"),
                fromToPeriod);


        if (includeCostCenter && !TextUtils.isEmpty(trip.getCostCenter())) {
            writeNewLine(contentStream,
                    context.getFont("DEFAULT"),
                    R.string.report_header_cost_center,
                    trip.getCostCenter()
            );
        }
        if (!TextUtils.isEmpty(trip.getComment())) {
            writeNewLine(contentStream,
                    context.getFont("DEFAULT"),
                    R.string.report_header_comment,
                    trip.getComment()
            );
        }

    }

    private void writeTable(PDPageContentStream contentStream,
                            Trip trip,
                            List<Receipt> receipts,
                            ReceiptsReportTableData data) {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (printDistanceAsDailyReceipt) {
            // TODO
//            receiptsTableList.addAll(new DistanceToReceiptsConverter(getContext(), getPreferences()).convert(getDatabase().getDistanceTable().getBlocking(trip, false)));
//            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }


        final PdfBoxTableGenerator<Receipt> pdfTableGenerator =
                new PdfBoxTableGenerator<>(context, contentStream, columns,
                        receiptFilter, true, true, currentCursorPosition);

        pdfTableGenerator.generate(receipts);

    }

    private void writeNewLine(PDPageContentStream contentStream,
                              PdfBoxContext.FontSpec spec,
                              @StringRes int resId,
                              Object... args) throws IOException {
        if (resId != 0) {
            writeNewLine(contentStream, spec, context.getString(resId, args));
        }
    }


    private void writeNewLine(PDPageContentStream contentStream,
                              PdfBoxContext.FontSpec spec,
                              String str) throws IOException {
        // set the font
        contentStream.setFont(spec.getFont(), spec.getSize());
        // calculate dy (font size + line spacing)
        float dy = spec.getSize() + context.getLineSpacing();
        // move the cursor by dy and update the currentCursorPosition
        contentStream.newLineAtOffset(0, -dy);
        currentCursorPosition -= dy;
        // write the text
        contentStream.showText(str);
    }

    public void setOnlyUseReimbursable(boolean onlyUseReimbursable) {
        this.onlyUseReimbursable = onlyUseReimbursable;
    }

    public void setUsePreTaxPrice(boolean usePreTaxPrice) {
        this.usePreTaxPrice = usePreTaxPrice;
    }
}
