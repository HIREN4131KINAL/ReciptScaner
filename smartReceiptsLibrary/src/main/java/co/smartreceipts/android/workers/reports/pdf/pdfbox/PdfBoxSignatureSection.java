package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;

import co.smartreceipts.android.model.Trip;

public class PdfBoxSignatureSection extends PdfBoxSection {

    private File signature;



    protected PdfBoxSignatureSection(PdfBoxContext context,
                                     Trip doc,
                                     File signature) {
        super(context, doc);
        this.signature = signature;
    }


    @Override
    public void writeSection(PDDocument doc) {
        // TODO
    }
}
