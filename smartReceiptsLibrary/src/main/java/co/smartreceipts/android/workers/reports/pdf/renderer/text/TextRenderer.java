package co.smartreceipts.android.workers.reports.pdf.renderer.text;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.workers.reports.pdf.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxWriter;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.HeightConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.WidthConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.XPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.YPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Alignment;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Color;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Font;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;
import co.smartreceipts.android.workers.reports.pdf.tables.FixedWidthTextCell;

/**
 * Renders text using a {@link PdfBoxWriter}
 */
public class TextRenderer extends Renderer {

    private final String string;

    public TextRenderer(@NonNull String string, @NonNull AWTColor color, @NonNull PdfFontSpec fontSpec) {
        this(string, new Color(color), new Font(fontSpec));
    }

    public TextRenderer(@NonNull String string, @NonNull Color color, @NonNull Font font) {
        this.string = Preconditions.checkNotNull(string);

        this.width = WRAP_CONTENT;
        this.height = WRAP_CONTENT;

        this.getRenderingFormatting().addFormatting(color);
        this.getRenderingFormatting().addFormatting(font);
    }

    @Override
    public void measure() throws IOException {
        final AWTColor color = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Color.class));
        final PdfFontSpec fontSpec = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Font.class));

        final float padding = getRenderingFormatting().getFormatting(Padding.class, 0f);
        final Float heightConstraint = getRenderingConstraints().getConstraint(HeightConstraint.class);
        final Float widthConstraint = getRenderingConstraints().getConstraint(WidthConstraint.class);

        Preconditions.checkArgument(heightConstraint == null, "Height constraints are currently unsupported");

        final float measuredHeight;
        final float measuredWidth;

        final List<String> lines;
        if (widthConstraint != null) {
            lines = new FixedWidthTextCell(widthConstraint, padding, string, fontSpec, color).getLines();
            measuredWidth = widthConstraint;
        } else {
            lines = Collections.singletonList(string);
            measuredWidth = PdfBoxUtils.getStringWidth(string, fontSpec);
        }
        measuredHeight = lines.size() * PdfBoxUtils.getFontHeight(fontSpec) + 2 * padding;

        this.width = measuredWidth;
        this.height = measuredHeight;
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        final AWTColor color = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Color.class));
        final PdfFontSpec fontSpec = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Font.class));

        float x = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(XPositionConstraint.class));
        float y = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(YPositionConstraint.class));

        final float padding = getRenderingFormatting().getFormatting(Padding.class, 0f);

        final Alignment.Type alignment = getRenderingFormatting().getFormatting(Alignment.class, Alignment.Type.Centered);
        Preconditions.checkArgument(alignment == Alignment.Type.Centered, "Only center alignment is currently supported");

        final List<String> lines = new FixedWidthTextCell(width, padding, string, fontSpec, color).getLines();

        // Attempt to center our cursor
        y = y + padding;

        for (final String line : lines) {
            final float stringWidth = PdfBoxUtils.getStringWidth(line, fontSpec);
            final float dx = (width - stringWidth) / 2.0f;

            // Move our cursor down
            y += PdfBoxUtils.getFontHeight(fontSpec);

            // Write the text
            writer.printText(line, fontSpec, color, x + dx, y);
        }

    }
}
