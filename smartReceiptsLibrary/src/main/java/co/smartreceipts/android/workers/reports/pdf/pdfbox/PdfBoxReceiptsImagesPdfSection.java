package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.pdf.PdfSection;


public class PdfBoxReceiptsImagesPdfSection extends PdfBoxSection {


    public PdfBoxReceiptsImagesPdfSection(PdfBoxContext context, PDDocument doc) {
        super(context, doc);
    }

    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) {

    }
}
