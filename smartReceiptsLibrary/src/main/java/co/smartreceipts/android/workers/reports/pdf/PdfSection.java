package co.smartreceipts.android.workers.reports.pdf;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public interface PdfSection {
    void writeSection(Trip trip, List<Receipt> receipts) throws IOException;
}
