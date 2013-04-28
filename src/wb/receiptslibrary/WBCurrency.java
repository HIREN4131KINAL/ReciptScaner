package wb.receiptslibrary;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import com.itextpdf.text.Utilities;

public class WBCurrency {
	
	private Currency currency;
	private String code;
	
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
		BigDecimal amnt = TextUtils.stringToBigDecimal(price);
    	try {
    		if (currency != null) {
				NumberFormat numFormat = NumberFormat.getCurrencyInstance(SRUtils.LOCALE);
				numFormat.setCurrency(currency);
				return numFormat.format(amnt.doubleValue());
    		}
    		else {
    			return code + TextUtils.formatStringAsStrictDecimal(amnt);
    		}
    	} catch (java.lang.NumberFormatException e) {
    		return "$0.00";
    	}
	}
}
