package wb.receiptslibrary;

import java.io.File;
import java.sql.Date;
import java.util.Currency;

public final class TripRow {
	
	public final File dir;
	public String price;
	public final Date from, to;
	public Currency currency;
	
	public TripRow(final String path, final long startDate, final long endDate, final String price, final String currency) {
		this.dir = new File(path);
		this.from = new Date(startDate);
		this.to = new Date(endDate);
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
		try {
			this.currency = Currency.getInstance(currency);
		} catch (IllegalArgumentException e) {
			this.currency = null;
		}
	}
	
	public TripRow(final File dir, final Date startDate, final Date endDate, final String currency) {
		this.dir = dir;
		this.from = startDate;
		this.to = endDate;
		this.price = "0.00";
		try {
			if (!currency.equalsIgnoreCase(DatabaseHelper.MULTI_CURRENCY)) this.currency = Currency.getInstance(currency);
			else this.currency = null;
		} catch (IllegalArgumentException e) {
			this.currency = null;
		}
	}
	
	public TripRow(final File dir, final Date startDate, final Date endDate, final Currency currency) {
		this.dir = dir;
		this.from = startDate;
		this.to = endDate;
		this.price = "0.00";
		this.currency = currency;
	}
	
}
