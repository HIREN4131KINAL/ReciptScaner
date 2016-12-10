package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import co.smartreceipts.android.workers.reports.pdf.PdfSection;

public abstract class PdfBoxSection implements PdfSection {

    protected PDDocument doc;
    protected PdfBoxContext context;



    public PdfBoxSection(PdfBoxContext context, PDDocument doc) {
        this.context = context;
        this.doc = doc;
    }
}
