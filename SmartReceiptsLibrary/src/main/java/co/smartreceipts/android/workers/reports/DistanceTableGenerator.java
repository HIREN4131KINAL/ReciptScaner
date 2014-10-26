package co.smartreceipts.android.workers.reports;

import android.content.Context;
import android.support.annotation.NonNull;

import com.itextpdf.text.pdf.PdfPTable;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.persistence.Preferences;

public class DistanceTableGenerator {

    private static final int DISTANCE_COLUMN_COUNT = 6;

    private final Context mContext;
    private final Preferences mPreferences;

    public DistanceTableGenerator(@NonNull Context context, @NonNull Preferences preferences) {
        mContext = context;
        mPreferences = preferences;
    }

    
    public PdfPTable generate(@NonNull List<Distance> distances) {
        if (!distances.isEmpty()) {
            final PdfPTable table = new PdfPTable(DISTANCE_COLUMN_COUNT);
            table.setWidthPercentage(100);

            // First, add the headers to the table:
            table.addCell(mContext.getString(R.string.distance_distance_field));
            table.addCell(mContext.getString(R.string.distance_date_field));
            table.addCell(mContext.getString(R.string.distance_location_field));
            table.addCell(mContext.getString(R.string.distance_rate_field));
            table.addCell(mContext.getString(R.string.dialog_currency_field));
            table.addCell(mContext.getString(R.string.distance_comment_field));

            // Next, let's add the actual data
            for (int i = 0; i < distances.size(); i++) {
                final Distance distance = distances.get(i);
                table.addCell(distance.getDecimalFormattedDistance());
                table.addCell(distance.getFormattedDate(mContext, mPreferences.getDateSeparator()));
                table.addCell(distance.getLocation());
                table.addCell(distance.getDecimalFormattedRate());
                table.addCell(distance.getCurrencyCode());
                table.addCell(distance.getComment());
            }
            return table;
        }
        else {
            return new PdfPTable(1); // Just return an empty table if we don't have any objects
        }
    }



}
