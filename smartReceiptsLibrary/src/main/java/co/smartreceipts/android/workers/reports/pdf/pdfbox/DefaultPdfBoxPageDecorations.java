package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;


public class DefaultPdfBoxPageDecorations implements PdfBoxPageDecorations {


    private static final float HEADER_HEIGHT = 15.0f;
    private static final float HEADER_LINE_HEIGHT = 5.0f;

    private static final float FOOTER_LINE_HEIGHT = 3.0f;
    private static final float FOOTER_PADDING = 5.0f;
    private static final float FOOTER_HEIGHT = 24.0f;


    private final PdfBoxContext context;
    private String footerText;


    public DefaultPdfBoxPageDecorations(PdfBoxContext context) {
        this.context = context;
        footerText = context.getPreferences().getPdfFooterText();
    }

    /**
     * Prints out a colored rectangle of height <code>HEADER_LINE_HEIGHT</code>
     * on the top part of the space reserved for the header, which has height
     * <code>HEADER_HEIGHT</code>.
     * <code>HEADER_LINE_HEIGHT</code> must be smaller than <code>HEADER_HEIGHT</code>
     * so that there is some padding space left naturally before the page content starts.
     * @param contentStream
     * @throws IOException
     */
    @Override
    public void writeHeader(PDPageContentStream contentStream) throws IOException {

        PDRectangle rect = new PDRectangle(
                context.getPageMarginHorizontal(),
                context.getPageSize().getHeight()
                        - context.getPageMarginVertical()
                        - HEADER_LINE_HEIGHT,
                context.getPageSize().getWidth() - 2 * context.getPageMarginHorizontal(),
                HEADER_LINE_HEIGHT
        );
        contentStream.setNonStrokingColor(context.getColor(DefaultPdfBoxContext.COLOR_DARK_BLUE));
        contentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
        contentStream.fill();
        contentStream.setNonStrokingColor(AWTColor.BLACK);
    }


    /**
     * Footer with height <code>FOOTER_HEIGHT</code> which consists of some padding of height
     * <code>FOOTER_PADDING</code>, a rectangle of height <code>FOOTER_LINE_HEIGHT</code>, followed
     * by a text message.
     * @param contentStream
     * @throws IOException
     */
    @Override
    public void writeFooter(PDPageContentStream contentStream) throws IOException {
        PDRectangle rect = new PDRectangle(
                context.getPageMarginHorizontal(),
                context.getPageMarginVertical()
                        + FOOTER_HEIGHT
                        - FOOTER_LINE_HEIGHT
                        - FOOTER_PADDING,
                context.getPageSize().getWidth() - 2 * context.getPageMarginHorizontal(),
                FOOTER_LINE_HEIGHT
        );
        contentStream.setNonStrokingColor(context.getColor(DefaultPdfBoxContext.COLOR_DARK_BLUE));
        contentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
        contentStream.fill();
        contentStream.setNonStrokingColor(AWTColor.BLACK);


        contentStream.beginText();
        contentStream.newLineAtOffset(context.getPageMarginHorizontal(), context.getPageMarginVertical());
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        contentStream.showText(footerText);
        contentStream.endText();
    }

    @Override
    public float getHeaderHeight() {
        return HEADER_HEIGHT;
    }

    @Override
    public float getFooterHeight() {
        return FOOTER_HEIGHT;
    }
}
