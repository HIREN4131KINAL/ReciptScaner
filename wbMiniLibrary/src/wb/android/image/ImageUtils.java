package wb.android.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageUtils {

	/**
	 * Converts an image to grayscale
	 * 
	 * @param bitmap - the {@link Bitmap} to convert to grayscale. The initial copy of which
	 * will be recycled via the {@link Bitmap#recycle()} method
	 * @return - a grayscale {@link Bitmap} instance with {@link Bitmap.Config.RGB_565}. The initial
	 * bitmap instance will be returned if an {@link OutOfMemoryException} occurs
	 */
	public static Bitmap convertToGrayScale(Bitmap bitmap) {
		return convertToGrayScale(bitmap, Bitmap.Config.RGB_565);
	}
	
	/**
	 * Converts an image to grayscale
	 * 
	 * @param bitmap - the {@link Bitmap} to convert to grayscale. The initial copy of which
	 * will be recycled via the {@link Bitmap#recycle()} method
	 * @param config - the {@link Bitmap.Config} to create the new image with
	 * @return - a grayscale {@link Bitmap} instance. The initial
	 * bitmap instance will be returned if an {@link OutOfMemoryException} occurs
	 */
	public static Bitmap convertToGrayScale(Bitmap bitmap, Bitmap.Config config) {
		try {
		    Bitmap bmpGrayscale = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
		    Canvas c = new Canvas(bmpGrayscale);
		    Paint paint = new Paint();
		    ColorMatrix cm = new ColorMatrix();
		    cm.setSaturation(0);
		    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		    paint.setColorFilter(f);
		    c.drawBitmap(bitmap, 0, 0, paint);
		    if (bitmap != bmpGrayscale) {
		    	bitmap.recycle();
		    }
		    return bmpGrayscale;
		}
		catch (OutOfMemoryError e) {
			return bitmap;
		}
	}
}
