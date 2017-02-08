package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.pdf.PdfSection;

public abstract class PdfBoxSection implements PdfSection {

    protected final PdfBoxContext mContext;
    protected final Trip mTrip;


    public PdfBoxSection(@NonNull PdfBoxContext context, @NonNull Trip trip) {
        mContext = context;
        mTrip = trip;
    }
}
