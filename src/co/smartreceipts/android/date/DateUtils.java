package co.smartreceipts.android.date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.format.DateFormat;

public class DateUtils {

	public static final String DEFAULT_SEPARATOR = "/";
	private static String separator;
	
	private DateUtils() {
		throw new RuntimeException("This class uses static calls only. It cannot be instantianted");
	}
	
	public static String getDateSeparator(Context context) {
		if (separator == null) {
	        String dateString = DateFormat.getDateFormat(context).format(new java.util.Date());
	        Matcher matcher = Pattern.compile("[^\\w]").matcher(dateString);
	        if (!matcher.find()) {
	        	separator = DEFAULT_SEPARATOR;
	        }
	        else {
	        	separator = matcher.group(0);
	        }
		}
		return separator;
    }
}
