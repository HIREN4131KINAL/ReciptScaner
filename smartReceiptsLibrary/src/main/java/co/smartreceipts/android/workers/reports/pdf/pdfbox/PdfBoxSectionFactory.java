package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import java.io.File;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public interface PdfBoxSectionFactory {

    PdfBoxReceiptsTablePdfSection createReceiptsTableSection(
            Trip trip, List<Receipt> receipts, List<Column<Receipt>> distances, List<Distance> columns, List<Column<Distance>> distanceColumns);

    PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection(Trip trip, List<Receipt> receipts);

    PdfBoxSignatureSection createSignatureSection(Trip trip, File signature);

}
