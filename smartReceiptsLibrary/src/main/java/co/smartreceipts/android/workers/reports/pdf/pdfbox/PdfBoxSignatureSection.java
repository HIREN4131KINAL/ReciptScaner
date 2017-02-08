package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;

import co.smartreceipts.android.model.Trip;

public class PdfBoxSignatureSection extends PdfBoxSection {

    private final File mSignature;



    protected PdfBoxSignatureSection(@NonNull PdfBoxContext context,
                                     @NonNull Trip doc,
                                     @NonNull File signature) {
        super(context, doc);
        mSignature = signature;
    }


    @Override
    public void writeSection(@NonNull PDDocument doc) {
        throw new UnsupportedOperationException("Signature section is not implemented yet");
    }
}
