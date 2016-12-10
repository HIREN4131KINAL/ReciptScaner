package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import java.io.File;
import java.util.List;

import co.smartreceipts.android.model.Distance;

public interface PdfBoxSectionFactory {
    PdfBoxReceiptsTablePdfSection createReceiptsTableSection(List<Distance> distances);
    PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection();
    PdfBoxSignatureSection createSignatureSection(File signature);

}
