package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.pdf.PdfSection;

public abstract class PdfBoxSection implements PdfSection {

    protected final PdfBoxContext context;
    protected final Trip trip;


    public PdfBoxSection(PdfBoxContext context, Trip trip) {
        this.context = context;
        this.trip = trip;
    }
}
