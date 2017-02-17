package wb.android.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.Log;

public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    private ImageUtils() {
    }

    /**
     * Converts an image to grayscale
     *
     * @param bitmap - the {@link Bitmap} to convert to grayscale. The initial copy of which
     *               will be recycled via the {@link Bitmap#recycle()} method
     * @return - a grayscale {@link Bitmap} instance with {@link Bitmap.Config#RGB_565}. The initial
     * bitmap instance will be returned if an {@link java.lang.OutOfMemoryError} occurs
     */
    public static Bitmap convertToGrayScale(Bitmap bitmap) {
        return convertToGrayScale(bitmap, Bitmap.Config.RGB_565);
    }

    /**
     * Converts an image to grayscale
     *
     * @param bitmap - the {@link Bitmap} to convert to grayscale. The initial copy of which
     *               will be recycled via the {@link Bitmap#recycle()} method
     * @param config - the {@link Bitmap.Config} to create the new image with
     * @return - a grayscale {@link Bitmap} instance. The initial
     * bitmap instance will be returned if an {@link java.lang.OutOfMemoryError} occurs
     */
    public static Bitmap convertToGrayScale(Bitmap bitmap, Bitmap.Config config) {
        if (bitmap == null || config == null) {
            return bitmap;
        }
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
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }


    /**
     * Rotates the {@link Bitmap} to a given orientation
     *
     * @param bitmap      - bitmap to rotate
     * @param exifOrientation - the orientation to move to. This should not be a direct
     *                    value but rather one that is gathered via the {@link android.media.ExifInterface#getAttributeInt(String, int)}
     *                    value with the key {@link android.media.ExifInterface#TAG_ORIENTATION}.
     * @return - the rotated version of the original {@link Bitmap}. Please note that the initial one will
     * be recycled via the {@link Bitmap#recycle()} method if a rotation occurs. The original bitmap will be returned if an {@link java.lang.OutOfMemoryError} occurs
     * or it is already in the proper rotation
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int exifOrientation) {
        Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.toString());
            return bitmap;
        }
    }
}
