package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.settings.UserPreferenceManager;


public class PdfBoxReceiptsImagesPdfSection extends PdfBoxSection {


    private final UserPreferenceManager mPreferences;
    private final List<Receipt> mReceipts;


    public PdfBoxReceiptsImagesPdfSection(@NonNull PdfBoxContext context, @NonNull Trip trip,
                                          @NonNull List<Receipt> receipts) {
        super(context, trip);
        mPreferences = context.getPreferences();
        mReceipts = receipts;
    }

    @Override
    public void writeSection(@NonNull PDDocument doc) throws IOException {

        DefaultPdfBoxPageDecorations pageDecorations = new DefaultPdfBoxPageDecorations(mContext);
        PdfBoxWriter writer = new PdfBoxWriter(doc, mContext, pageDecorations);

        float availableWidth = mContext.getPageSize().getWidth()
                - 2* mContext.getPageMarginHorizontal();
        float availableHeight = mContext.getPageSize().getHeight()
                - 2* mContext.getPageMarginVertical()
                - pageDecorations.getHeaderHeight()
                - pageDecorations.getFooterHeight();


        PdfBoxImageTableGenerator pdfImageTableGenerator =
                new PdfBoxImageTableGenerator(mContext, new LegacyReceiptFilter(mPreferences),
                        availableWidth, availableHeight);

        PdfBoxImageTable table = pdfImageTableGenerator.generate(mReceipts);
        writer.writeTable(table);


        writer.writeAndClose();
    }
}
