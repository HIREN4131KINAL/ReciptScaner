package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Receipt;

@Deprecated // Doubtful whether I need this
public class ReceitptsImagesReportTableData {


    public static final String SEPARATOR = "  \u2022  ";
    private final ArrayList<File> images;
    private final ArrayList<String> headers;
    private boolean includeReceiptIdInsteadOfIndexByPhoto;
    private boolean getIncludeCommentByReceiptPhoto;

    public ReceitptsImagesReportTableData(List<Receipt> receipts) {
        images = new ArrayList<>(receipts.size());
        headers = new ArrayList<>(receipts.size());
        for (Receipt receipt : receipts) {
            File image = receipt.getImage();
            if (image != null) {
                images.add(image);
                headers.add(buildHeaderText(receipt));
            }
        }
    }

    private String buildHeaderText(Receipt receipt) {
        final int num = includeReceiptIdInsteadOfIndexByPhoto ? receipt.getId() : receipt.getIndex();
        final String extra = (getIncludeCommentByReceiptPhoto && !TextUtils.isEmpty(receipt.getComment()))
                ? SEPARATOR + receipt.getComment()
                : "";
        return num + SEPARATOR + receipt.getName() + SEPARATOR;
//                + receipt.getFormattedDate(getContext(), getPreferences().getDateSeparator()) + extra;
    }
}
