package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.itextpdf.text.Paragraph;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

import static java.security.AccessController.getContext;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private List<Distance> distances;
    private boolean usePreTaxPrice;
    private boolean onlyUseReimbursable;

    protected PdfBoxReceiptsTablePdfSection(PdfBoxContext context,
                                            PDDocument doc,
                                            List<Distance> distances) {
        super(context, doc);
        this.distances = distances;
    }

    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) throws IOException {


        ReceiptsReportTableData data = new ReceiptsReportTableData(trip,
                receipts, distances, usePreTaxPrice, onlyUseReimbursable);

        PDPage page = new PDPage();
        doc.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(doc, page);

        contentStream.setFont(context.getFont(), context.getFontSize());
        contentStream.beginText();

        contentStream.newLineAtOffset(100, 100);
        contentStream.showText(trip.getName());

        if (!data.receiptsPrice.equals(data.netPrice)) {
            contentStream.newLine();
            contentStream.showText(context.getString(R.string.report_header_receipts_total,
                    data.receiptsPrice.getCurrencyFormattedPrice()));
        }

        contentStream.newLine();
        contentStream.showText("AAAA");

        contentStream.newLine();
        contentStream.showText("BBBB");

        contentStream.endText();
        contentStream.close();

    }

    public void setOnlyUseReimbursable(boolean onlyUseReimbursable) {
        this.onlyUseReimbursable = onlyUseReimbursable;
    }

    public void setUsePreTaxPrice(boolean usePreTaxPrice) {
        this.usePreTaxPrice = usePreTaxPrice;
    }
}
