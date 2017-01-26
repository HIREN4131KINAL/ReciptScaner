package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableGenerator;

public class PdfBoxDistancesTablePdfSection extends PdfBoxSection {

    private final List<Distance> distances;
    private final List<Column<Distance>> columns;
    private PdfBoxWriter writer;

    public PdfBoxDistancesTablePdfSection(DefaultPdfBoxContext context,
                                          PDDocument doc,
                                          List<Distance> distances,
                                          List<Column<Distance>> columns) {
        super(context, doc);
        this.distances = distances;
        this.columns = columns;
    }


    @Override
    public void writeSection(Trip trip, List<Receipt> receipts) throws IOException {
        writer = new PdfBoxWriter(doc, context, new DefaultPdfBoxPageDecorations(context));

        final PdfBoxTableGenerator<Distance> pdfTableGenerator =
                new PdfBoxTableGenerator<>(context, columns,
                        null, true, true);

        PdfBoxTable table = pdfTableGenerator.generate(distances);

        writer.writeTable(table);

        writer.writeAndClose();
    }
}
