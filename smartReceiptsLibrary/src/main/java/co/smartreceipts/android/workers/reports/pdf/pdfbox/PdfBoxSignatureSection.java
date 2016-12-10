package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.pdf.PdfSection;

public class PdfBoxSignatureSection extends PdfBoxSection {

    private File signature;



    protected PdfBoxSignatureSection(PdfBoxContext context,
                                     PDDocument doc,
                                     File signature) {
        super(context, doc);
        this.signature = signature;
    }


    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) {
        // TODO
    }
}
