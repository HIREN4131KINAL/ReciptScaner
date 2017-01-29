package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.Preferences;


public class PdfBoxReceiptsImagesPdfSection extends PdfBoxSection {


    private PdfBoxWriter writer;
    private final Preferences preferences;
    private final List<Receipt> receipts;


    public PdfBoxReceiptsImagesPdfSection(PdfBoxContext context, Trip trip, List<Receipt> receipts) {
        super(context, trip);
        this.preferences = context.getPreferences();
        this.receipts = receipts;
    }

    @Override
    public void writeSection(PDDocument doc) throws IOException {

        DefaultPdfBoxPageDecorations pageDecorations = new DefaultPdfBoxPageDecorations(context);
        writer = new PdfBoxWriter(doc, context, pageDecorations);

        float availableWidth = context.getPageSize().getWidth()
                - 2*context.getPageMarginHorizontal();
        float availableHeight = context.getPageSize().getHeight()
                - 2*context.getPageMarginVertical()
                - pageDecorations.getHeaderHeight()
                - pageDecorations.getFooterHeight();


        PdfBoxImageTableGenerator pdfImageTableGenerator =
                new PdfBoxImageTableGenerator(context, new LegacyReceiptFilter(preferences),
                        availableWidth, availableHeight);

        PdfBoxImageTable table = pdfImageTableGenerator.generate(receipts);
        writer.writeTable(table);


        writer.writeAndClose();
    }
}
