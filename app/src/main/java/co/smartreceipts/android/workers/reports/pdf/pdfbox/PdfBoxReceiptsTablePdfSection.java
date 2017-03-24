package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
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
import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTableGenerator;

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
        mReceipts = Preconditions.checkNotNull(receipts);
        mDistances = Preconditions.checkNotNull(distances);
        mReceiptColumns = Preconditions.checkNotNull(receiptColumns);
        mPreferences = Preconditions.checkNotNull(context.getPreferences());
        mDistanceColumns = Preconditions.checkNotNull(distanceColumns);
    }



    @Override
    public void writeSection(@NonNull PDDocument doc, @NonNull PdfBoxWriter writer) throws IOException {

        final ReceiptsTotals totals = new ReceiptsTotals(trip, mReceipts, mDistances, mPreferences);

        // switch to landscape mode
        if (mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            pdfBoxContext.setPageSize(new PDRectangle(pdfBoxContext.getPageSize().getHeight(),
                    pdfBoxContext.getPageSize().getWidth()));
        }

        mWriter = writer;
        mWriter.newPage();

        writeHeader(trip, totals);

        mWriter.verticalJump(40);

        writeReceiptsTable(mReceipts);

        if (mPreferences.get(UserPreference.Distance.PrintDistanceTableInReports) && !mDistances.isEmpty()) {
            mWriter.verticalJump(60);

            writeDistancesTable(mDistances);
        }

        // reset the page size if necessary
        if (mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            pdfBoxContext.setPageSize(new PDRectangle(pdfBoxContext.getPageSize().getHeight(),
                    pdfBoxContext.getPageSize().getWidth()));
        }
    }

    private void writeHeader(@NonNull Trip trip, @NonNull ReceiptsTotals data) throws IOException {

        mWriter.openTextBlock();
        mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Title), trip.getName());
        
        if (!data.receiptsPrice.equals(data.netPrice)) {
            mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total, data.receiptsPrice.getCurrencyFormattedPrice());
        }

        if (mPreferences.get(UserPreference.Receipts.IncludeTaxField)) {
            if (mPreferences.get(UserPreference.Receipts.UsePreTaxPrice) && data.taxPrice.getPriceAsFloat() > EPSILON) {
                mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total_tax, data.taxPrice.getCurrencyFormattedPrice());
            } else if (!data.noTaxPrice.equals(data.receiptsPrice) && data.noTaxPrice.getPriceAsFloat() > EPSILON) {
                mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total_no_tax, data.noTaxPrice.getCurrencyFormattedPrice());
            }
        }

        if (!mPreferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) && !data.reimbursablePrice.equals(data.receiptsPrice)) {
            mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_receipts_total_reimbursable, data.reimbursablePrice.getCurrencyFormattedPrice());
        }
        if (!mDistances.isEmpty()) {
            mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_distance_total, data.distancePrice.getCurrencyFormattedPrice());
        }

        mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default), R.string.report_header_gross_total, data.netPrice.getCurrencyFormattedPrice());

        String fromToPeriod = pdfBoxContext.getString(R.string.report_header_from,
                trip.getFormattedStartDate(pdfBoxContext.getAndroidContext(), mPreferences.get(UserPreference.General.DateSeparator)))
                + " "
                + pdfBoxContext.getString(R.string.report_header_to,
                trip.getFormattedEndDate(pdfBoxContext.getAndroidContext(), mPreferences.get(UserPreference.General.DateSeparator)));

        mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default),
                fromToPeriod);


        if (mPreferences.get(UserPreference.General.IncludeCostCenter) && !TextUtils.isEmpty(trip.getCostCenter())) {
            mWriter.writeNewLine(pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default),
                    R.string.report_header_cost_center,
                    trip.getCostCenter()
            );
        }
        if (!TextUtils.isEmpty(trip.getComment())) {
            mWriter.writeNewLine(
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default),
                    R.string.report_header_comment,
                    trip.getComment()
            );
        }

        mWriter.closeTextBlock();
    }

    private void writeReceiptsTable(@NonNull List<Receipt> receipts) throws IOException {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (mPreferences.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)) {
            receiptsTableList.addAll(new DistanceToReceiptsConverter(pdfBoxContext.getAndroidContext(), mPreferences).convert(mDistances));
            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }

        final PdfBoxTableGenerator<Receipt> pdfTableGenerator = new PdfBoxTableGenerator<>(pdfBoxContext, mReceiptColumns,
                new LegacyReceiptFilter(mPreferences), true, false);

        final PdfBoxTable table = pdfTableGenerator.generate(receiptsTableList);
        mWriter.writeTable(table);
    }

    private void writeDistancesTable(@NonNull List<Distance> distances) throws IOException {
        final PdfBoxTableGenerator<Distance> pdfTableGenerator = new PdfBoxTableGenerator<>(pdfBoxContext, mDistanceColumns, null, true, true);
        final PdfBoxTable table = pdfTableGenerator.generate(distances);
        mWriter.writeTable(table);
    }


}
