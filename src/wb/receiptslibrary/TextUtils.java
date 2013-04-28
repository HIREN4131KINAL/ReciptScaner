package wb.receiptslibrary;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class TextUtils {
	
	private TextUtils() {}
	
	/**
	 * Formats a String, which represents a decimal number, as a strict Decimal (2 decimal points)
	 * @param input - A String that represents a decimal number. 
	 * @return A string representation of a number with two decimal digits. 0.00 is returned if the String is null or not a number
	 */
	public static final String formatStringAsStrictDecimal(String input) {
		BigDecimal amnt = stringToBigDecimal(input);
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setGroupingUsed(false);
		return decimalFormat.format(amnt.doubleValue());
	}
	
	/**
	 * Formats a BidDecimal as a strict Decimal (2 decimal points)
	 * @param bigDecimal - A BigDecimal number
	 * @return A String of a number with two decimal digits
	 */
	public static final String formatStringAsStrictDecimal(BigDecimal bigDecimal) {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setGroupingUsed(false);
		return decimalFormat.format(bigDecimal.doubleValue());
	}
	
	/**
	 * Converts a String to a Big Decimal
	 * @param input - A String that represents a decimal number.
	 * @return A big decimal representation of the String number. BigDecimal(0) is returned if the String is null or not a number
	 */
	public static final BigDecimal stringToBigDecimal(String input) {
		try {
	    	if (input == null || input.length() == 0)
	    		return new BigDecimal(0);
	    	else
	    		return new BigDecimal(input);
		}
		catch (NumberFormatException e) {
			return new BigDecimal(0);
		}
	}

}
