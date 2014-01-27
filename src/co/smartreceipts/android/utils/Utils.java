package co.smartreceipts.android.utils;

import java.lang.reflect.Field;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.StrictMode;
import android.util.DisplayMetrics;

public class Utils {

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void enableStrictMode() {
		if (ApiHelper.hasGingerbread()) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		        .detectAll()
		        .penaltyLog()
		        .build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		        .detectAll()
		        .penaltyLog()
		        .build());
		}
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp){
	    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * From: http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into dp
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px){
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * From: http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}

	public static class ApiHelper {
	
	    public static boolean hasFroyo() {
	        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	    }
	
	    public static boolean hasGingerbread() {
	        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	    }
	
	    public static boolean hasHoneycomb() {
	        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	    }
	
	    public static boolean hasHoneycombMR1() {
	        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	    }
	    
	    public static boolean hasICS() {
	        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	    }
	
	    public static boolean hasJellyBean() {
	        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	    }
	    
	    public static boolean supportsPreferenceHeaders() {
	    	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	    }
	    
	    public static int getIntFieldIfExists(Class<?> klass, String fieldName, Class<?> obj, int defaultVal) {
	        try {
	            Field f = klass.getDeclaredField(fieldName);
	            return f.getInt(obj);
	        } catch (Exception e) {
	            return defaultVal;
	        }
	    }

	    public static boolean hasField(Class<?> klass, String fieldName) {
	        try {
	            klass.getDeclaredField(fieldName);
	            return true;
	        } catch (NoSuchFieldException e) {
	            return false;
	        }
	    }

	    public static boolean hasMethod(String className, String methodName, Class<?>... parameterTypes) {
	        try {
	            Class<?> klass = Class.forName(className);
	            klass.getDeclaredMethod(methodName, parameterTypes);
	            return true;
	        } catch (Throwable th) {
	            return false;
	        }
	    }

	    public static boolean hasMethod(Class<?> klass, String methodName, Class<?> ... paramTypes) {
	        try {
	            klass.getDeclaredMethod(methodName, paramTypes);
	            return true;
	        } catch (NoSuchMethodException e) {
	            return false;
	        }
	    }
	}
	
}