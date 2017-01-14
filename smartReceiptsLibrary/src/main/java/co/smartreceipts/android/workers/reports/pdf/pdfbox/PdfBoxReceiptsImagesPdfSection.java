package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;


public class PdfBoxReceiptsImagesPdfSection extends PdfBoxSection {


    private PdfBoxWriter writer;
    // TODO null
    private Filter<Receipt> receiptFilter;


    public PdfBoxReceiptsImagesPdfSection(PdfBoxContext context, PDDocument doc) {
        super(context, doc);
    }

    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) throws IOException {

        DefaultPdfBoxPageDecorations pageDecorations = new DefaultPdfBoxPageDecorations(context);
        writer = new PdfBoxWriter(doc, context, pageDecorations);

        float availableWidth = context.getPageSize().getWidth()
                - 2*context.getPageMarginHorizontal();
        float availableHeight = context.getPageSize().getHeight()
                - 2*context.getPageMarginVertical()
                - pageDecorations.getHeaderHeight()
                - pageDecorations.getFooterHeight();


        PdfBoxImageTableGenerator pdfImageTableGenerator =
                new PdfBoxImageTableGenerator(context, receiptFilter,
                        availableWidth, availableHeight);

        PdfBoxImageTable table = pdfImageTableGenerator.generate(receipts);
        writer.writeTable(table);


        writer.writeAndClose();
    }
}
