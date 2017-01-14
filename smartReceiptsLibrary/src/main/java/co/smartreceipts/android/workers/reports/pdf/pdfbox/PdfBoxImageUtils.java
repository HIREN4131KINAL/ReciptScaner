package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;


public class PdfBoxImageUtils {


    /**
     * Scales the <code>ximage</code> up so that it fills the rectangle or down so that it fits
     * inside it, maintaining its aspect ration. Returns a {@link PDRectangle} with the dimensions
     * that the scaled image should have, and positions on the top of the containing
     * <code>rectangle</code>, centered horizontally.
     * @param ximage
     * @param rectangle
     * @return
     */
    public static PDRectangle scaleImageInsideRectangle(PDImageXObject ximage, PDRectangle rectangle) {

        float imageRatio = ((float) ximage.getWidth())/((float) ximage.getHeight());
        float rectRatio = rectangle.getWidth()/rectangle.getHeight();

        float factor;
        if (ximage.getHeight() < rectangle.getHeight()) {
            if (ximage.getWidth() < rectangle.getWidth()) {
                // Scale up both

                if (imageRatio > rectRatio) {
                    factor = rectangle.getWidth() / ximage.getWidth();
                } else {
                    factor = rectangle.getHeight() / ximage.getHeight();
                }
            } else {

                // scale down width
                factor = rectangle.getWidth() / ximage.getWidth();
            }
        } else { // ximage.getHeight() > rectangle.getHeight
            if (ximage.getWidth() > rectangle.getWidth()) {
                // scale down both
                if (imageRatio > rectRatio) {
                    // scale first width
                    factor = rectangle.getWidth() / ximage.getWidth();
                } else {
                    factor = rectangle.getHeight() / ximage.getHeight();
                }
            } else {
                // scale down width
                factor = rectangle.getWidth()/ ximage.getWidth();
            }
        }


        float scaledImageWidth = ximage.getWidth() * factor;
        float scaledImageHeight = ximage.getHeight() * factor;

        float unusedWidth = (rectangle.getWidth() - scaledImageWidth) / 2.0f;
        float unusedHeight = rectangle.getHeight() - scaledImageHeight;


        return new PDRectangle(rectangle.getLowerLeftX() + unusedWidth,
                rectangle.getLowerLeftY() + unusedHeight, scaledImageWidth, scaledImageHeight);

    }
}
