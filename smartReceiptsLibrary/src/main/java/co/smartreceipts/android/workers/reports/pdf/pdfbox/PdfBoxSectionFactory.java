package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import java.io.File;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;

public interface PdfBoxSectionFactory {
    PdfBoxReceiptsTablePdfSection createReceiptsTableSection(List<Distance> distances, List<Column<Receipt>> columns);
    PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection();
    PdfBoxSignatureSection createSignatureSection(File signature);

}
