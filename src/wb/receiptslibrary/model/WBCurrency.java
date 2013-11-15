package wb.receiptslibrary.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WBCurrency {
	
	private Currency currency;
	private String code;
	
	public static final String MISSING_CURRENCY = "NUL";
	private static final Set<String> EXTRA_CODES = new HashSet<String>(Arrays.asList(new String[] {"DRC","XOF", "BSF"}));

	private WBCurrency(Currency currency) {
		this.currency = currency;
	}
	
	private WBCurrency(String code) {
		this.code = code;
	}
	
	public final static WBCurrency getInstance(String currencyCode) {
		try {
			return new WBCurrency(Currency.getInstance(currencyCode));
		} catch (IllegalArgumentException e) {
			if (EXTRA_CODES.contains(currencyCode))
				return new WBCurrency(currencyCode);
			else
				return null;
		}
	}
	
	public final String getCurrencyCode() {
		if (currency != null)
			return currency.getCurrencyCode();
		else 
			return code;
	}
	
	public final String format(final String price) {
		BigDecimal amnt = stringToBigDecimal(price);
    	try {
    		if (currency != null) {
				NumberFormat numFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
				numFormat.setCurrency(currency);
				return numFormat.format(amnt.doubleValue());
    		}
    		else {
    			return code + formatStringAsStrictDecimal(amnt);
    		}
    	} catch (java.lang.NumberFormatException e) {
    		return "$0.00";
    	}
	}
	
	private BigDecimal stringToBigDecimal(String input) {
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
	
	public static final String formatStringAsStrictDecimal(BigDecimal bigDecimal) {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setGroupingUsed(false);
		return decimalFormat.format(bigDecimal.doubleValue());
	}
	
}
