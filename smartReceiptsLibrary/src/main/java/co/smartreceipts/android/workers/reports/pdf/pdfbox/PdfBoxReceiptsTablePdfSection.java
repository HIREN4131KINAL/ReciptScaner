package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.comparators.ReceiptDateComparator;
import co.smartreceipts.android.model.converters.DistanceToReceiptsConverter;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableGenerator;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private static final float EPSILON = 0.0001f;
    
    private final List<Receipt> mReceipts;
    private final List<Column<Receipt>> mReceiptColumns;

    private final List<Distance> mDistances;
    private final List<Column<Distance>> mDistanceColumns;

    private PdfBoxWriter mWriter;
    private final UserPreferenceManager mPreferences;

    protected PdfBoxReceiptsTablePdfSection(@NonNull PdfBoxContext context,
                                            @NonNull Trip trip,
                                            @NonNull List<Receipt> receipts,
                                            @NonNull List<Column<Receipt>> receiptColumns,
                                            @NonNull List<Distance> distances,
                                            @NonNull List<Column<Distance>> distanceColumns) {
        super(context, trip);
        mReceipts = receipts;
        mDistances = distances;
        mReceiptColumns = receiptColumns;
        mPreferences = context.getPreferences();
        mDistanceColumns = distanceColumns;
    }



    @Override
    public void writeSection(@NonNull PDDocument doc) throws IOException {

        final ReceiptsTotals totals = new ReceiptsTotals(mTrip, mReceipts, mDistances, mPreferences);

        // switch to landscape mode
        if (mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            mContext.setPageSize(new PDRectangle(mContext.getPageSize().getHeight(),
                    mContext.getPageSize().getWidth()));
        }

        mWriter = new PdfBoxWriter(doc, mContext, new DefaultPdfBoxPageDecorations(mContext));

        writeHeader(mTrip, totals);

        mWriter.verticalJump(40);

        writeReceiptsTable(mReceipts);

        if (mPreferences.get(UserPreference.Distance.PrintDistanceTableInReports) && !mDistances.isEmpty()) {
            mWriter.verticalJump(60);

            writeDistancesTable(mDistances);
        }

        mWriter.writeAndClose();

        // reset the page size if necessary
        if (mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            mContext.setPageSize(new PDRectangle(mContext.getPageSize().getHeight(),
                    mContext.getPageSize().getWidth()));
        }
    }

    private void writeHeader(@NonNull Trip trip, @NonNull ReceiptsTotals data) throws IOException {

        mWriter.openTextBlock();
        mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Title), trip.getName());
        
        if (!data.mReceiptsPrice.equals(data.mNetPrice)) {
            mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total, data.mReceiptsPrice.getCurrencyFormattedPrice());
        }

        if (mPreferences.get(UserPreference.Receipts.IncludeTaxField)) {
            if (mPreferences.get(UserPreference.Receipts.UsePreTaxPrice) && data.mTaxPrice.getPriceAsFloat() > EPSILON) {
                mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total_tax, data.mTaxPrice.getCurrencyFormattedPrice());
            } else if (!data.mNoTaxPrice.equals(data.mReceiptsPrice) && data.mNoTaxPrice.getPriceAsFloat() > EPSILON) {
                mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total_no_tax, data.mNoTaxPrice.getCurrencyFormattedPrice());
            }
        }

        if (!mPreferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) && !data.mReimbursablePrice.equals(data.mReceiptsPrice)) {
            mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total_reimbursable, data.mReimbursablePrice.getCurrencyFormattedPrice());
        }
        if (mDistances.size() > 0) {
            mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_distance_total, data.mDistancePrice.getCurrencyFormattedPrice());
        }

        mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_gross_total, data.mNetPrice.getCurrencyFormattedPrice());

        String fromToPeriod = mContext.getString(R.string.report_header_from,
                trip.getFormattedStartDate(mContext.getAndroidContext(), mPreferences.get(UserPreference.General.DateSeparator)))
                + " "
                + mContext.getString(R.string.report_header_to,
                trip.getFormattedEndDate(mContext.getAndroidContext(), mPreferences.get(UserPreference.General.DateSeparator)));

        mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default),
                fromToPeriod);


        if (mPreferences.get(UserPreference.General.IncludeCostCenter) && !TextUtils.isEmpty(trip.getCostCenter())) {
            mWriter.writeNewLine(mContext.getFontManager().getFont(PdfFontStyle.Default),
                    R.string.report_header_cost_center,
                    trip.getCostCenter()
            );
        }
        if (!TextUtils.isEmpty(trip.getComment())) {
            mWriter.writeNewLine(
                    mContext.getFontManager().getFont(PdfFontStyle.Default),
                    R.string.report_header_comment,
                    trip.getComment()
            );
        }

        mWriter.closeTextBlock();
    }

    private void writeReceiptsTable(@NonNull List<Receipt> receipts) throws IOException {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (mPreferences.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)) {
            receiptsTableList.addAll(
                    new DistanceToReceiptsConverter(mContext.getAndroidContext(), mPreferences)
                    .convert(mDistances));
            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }


        final PdfBoxTableGenerator<Receipt> pdfTableGenerator =
                new PdfBoxTableGenerator<>(mContext, mReceiptColumns,
                        new LegacyReceiptFilter(mPreferences), true, false);

        PdfBoxTable table = pdfTableGenerator.generate(receiptsTableList);

        mWriter.writeTable(table);
    }

    private void writeDistancesTable(@NonNull List<Distance> distances) throws IOException {


        final PdfBoxTableGenerator<Distance> pdfTableGenerator =
                new PdfBoxTableGenerator<>(mContext, mDistanceColumns,
                        null, true, true);


        PdfBoxTable table = pdfTableGenerator.generate(distances);

        mWriter.writeTable(table);
    }


}
