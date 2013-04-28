package wb.receiptslibrary;

import java.io.File;
import java.sql.Date;
import java.util.Currency;

public class ReceiptRow {

	public File img;
	public final String name, category, comment, price, tax;
	public final String extra_edittext_1, extra_edittext_2, extra_edittext_3;
	public final Date date;
	public final int id;
	public final boolean expensable, fullpage;
	public WBCurrency currency;
	
	public ReceiptRow(final int id, final File img, final String name, final String category, final long date, final String comment, 
			final String price, final String tax, final boolean expensable, final String currencyCode, final boolean fullpage, 
			final String extra_edittext_1, final String extra_edittext_2, final String extra_edittext_3) {
		this.id = id;
		this.img = img;
		this.name = name;
		this.category = category;
		this.date = new Date(date);
		this.comment = comment;
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
		if (tax == null)
			this.tax = "0.00";
		else
			this.tax = tax;
		this.expensable = expensable;
		this.fullpage = fullpage;
		this.currency = WBCurrency.getInstance(currencyCode);
		//Extras
		if (extra_edittext_1 != null) 
			this.extra_edittext_1 = extra_edittext_1.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extra_edittext_1;
		else 
			this.extra_edittext_1 = null;
		if (extra_edittext_2 != null) 
			this.extra_edittext_2 = extra_edittext_2.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extra_edittext_2;
		else 
			this.extra_edittext_2 = null;
		if (extra_edittext_3 != null) 
			this.extra_edittext_3 = extra_edittext_3.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extra_edittext_3;
		else 
			this.extra_edittext_3 = null;
	}
	
	public ReceiptRow(final int id, final File img, final String name, final String category, final Date date, final String comment, 
			final String price, final String tax, final boolean expensable, final String currency, final boolean fullpage, 
			final String extra_edittext_1, final String extra_edittext_2, final String extra_edittext_3) {
		this.id = id;
		this.img = img;
		this.name = name;
		this.category = category;
		this.date = date;
		this.comment = comment;
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
		if (tax == null)
			this.tax = "0.00";
		else
			this.tax = tax;
		this.expensable = expensable;
		this.fullpage = fullpage;
		this.currency = WBCurrency.getInstance(currency);
		//Extras
		if (extra_edittext_1 != null) 
			this.extra_edittext_1 = extra_edittext_1.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extra_edittext_1;
		else 
			this.extra_edittext_1 = null;
		if (extra_edittext_2 != null) 
			this.extra_edittext_2 = extra_edittext_2.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extra_edittext_2;
		else 
			this.extra_edittext_2 = null;
		if (extra_edittext_3 != null) 
			this.extra_edittext_3 = extra_edittext_3.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extra_edittext_3;
		else 
			this.extra_edittext_3 = null;
	}
}
