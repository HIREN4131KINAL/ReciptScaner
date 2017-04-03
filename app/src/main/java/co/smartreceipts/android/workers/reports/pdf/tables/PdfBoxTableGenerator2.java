package co.smartreceipts.android.workers.reports.pdf.tables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.TableGenerator;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxPageDecorations;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.WidthConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.BackgroundColor;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRowRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.impl.ReceiptLabelTextRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.text.TextRenderer;
import co.smartreceipts.android.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;


public class PdfBoxTableGenerator2<DataType> implements TableGenerator<GridRenderer, DataType> {

    private static final Padding DEFAULT_PADDING = new Padding(4f);

    private final PdfBoxContext pdfBoxContext;
    private final List<Column<DataType>> columns;
    private final PDDocument pdDocument;
    private final PdfBoxPageDecorations pageDecorations;
    private final Filter<DataType> filter;
    private final boolean printHeaders;
    private final boolean printFooters;

    public PdfBoxTableGenerator2(@NonNull PdfBoxContext context,
                                 @NonNull List<Column<DataType>> columns,
                                 @NonNull PDDocument pdDocument,
                                 @NonNull PdfBoxPageDecorations pageDecorations,
                                 @Nullable Filter<DataType> receiptFilter,
                                 boolean printHeaders,
                                 boolean printFooters) {
        this.pdfBoxContext = Preconditions.checkNotNull(context);
        this.columns = Preconditions.checkNotNull(columns);
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.pageDecorations = Preconditions.checkNotNull(pageDecorations);
        this.filter = receiptFilter;
        this.printHeaders = printHeaders;
        this.printFooters = printFooters;
    }

    @NonNull
    @Override
    public GridRenderer generate(@NonNull List<DataType> list) throws IOException {
        final int colCount = columns.size();
        final List<DataType> filteredList = new ArrayList<>(list.size());

        float availableWidth = pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal();
        float availableHeight = pdfBoxContext.getPageSize().getHeight() - 2 * pdfBoxContext.getPageMarginVertical()
                - pageDecorations.getHeaderHeight() - pageDecorations.getFooterHeight();

        // calculate column widths
        // TODO: Include this as part of the measure pass
        float[] colWidths;
        ColumnWidthCalculator columnWidthCalculator = new ColumnWidthCalculator<>(
                list, columns, availableWidth, DEFAULT_PADDING.value(), pdfBoxContext.getFontManager().getFont(PdfFontStyle.TableHeader),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default));
        colWidths = columnWidthCalculator.calculate();

        final GridRenderer gridRenderer = new GridRenderer(availableWidth, availableHeight);

        // Add the header
        if (printHeaders) {
            final List<TextRenderer> headerColumns = new ArrayList<>();
            for (int i = 0; i < colCount; i++) {
                final TextRenderer textRenderer = new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        pdDocument,
                        columns.get(i).getHeader(),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.TableHeader));
                textRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                textRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(colWidths[i]));
                headerColumns.add(textRenderer);
            }
            final GridRowRenderer headerRow = new GridRowRenderer(headerColumns);
            headerRow.getRenderingFormatting().addFormatting(new BackgroundColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.TableHeader)));
            gridRenderer.addHeader(headerRow);
        }

        if (!list.isEmpty()) {

            // Add each row
            for (int j = 0; j < list.size(); j++) {
                final DataType data = list.get(j);
                if (filter == null || filter.accept(data)) {
                    filteredList.add(data);
                    final List<TextRenderer> columnRenderers = new ArrayList<>();
                    for (int i = 0; i < colCount; i++) {
                        final TextRenderer textRenderer = new TextRenderer(
                                pdfBoxContext.getAndroidContext(),
                                pdDocument,
                                HeavyHandedReplaceIllegalCharacters.getSafeString(columns.get(i).getValue(data)),
                                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default));
                        textRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                        textRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(colWidths[i]));
                        columnRenderers.add(textRenderer);
                    }
                    final GridRowRenderer rowRenderer = new GridRowRenderer(columnRenderers);
                    if (j % 2 == 0) {
                        rowRenderer.getRenderingFormatting().addFormatting(new BackgroundColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.TableCell)));
                    }
                    gridRenderer.addRow(rowRenderer);
                }
            }
        }

        // Add the footer
        if (printFooters) {
            final List<TextRenderer> footerColumns = new ArrayList<>();
            for (int i = 0; i < colCount; i++) {
                final TextRenderer textRenderer = new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        pdDocument,
                        columns.get(i).getFooter(filteredList),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default));
                textRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                textRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(colWidths[i]));
            }
            final GridRowRenderer footerRow = new GridRowRenderer(footerColumns);
            footerRow.getRenderingFormatting().addFormatting(new BackgroundColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.TableCell)));
            gridRenderer.addHeader(footerRow);
        }

        return gridRenderer;
    }

}
