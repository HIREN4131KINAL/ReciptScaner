package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public interface PdfBoxSectionFactory {

    @NonNull
    PdfBoxReceiptsTablePdfSection createReceiptsTableSection(
            @NonNull Trip trip,
            @NonNull List<Receipt> receipts,
            @NonNull List<Column<Receipt>> distances,
            @NonNull List<Distance> columns,
            @NonNull List<Column<Distance>> distanceColumns);

    @NonNull
    PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection(@NonNull Trip trip,
                                                               @NonNull List<Receipt> receipts);

}
