package wb.receiptslibrary;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Currency;

public final class TripRow {
	
	public final File dir;
	public final String name;
	public String price;
	public final Date from, to;
	public WBCurrency currency;
	public float miles;
	
	public TripRow(final String name, final File dir, final long startDate, final long endDate, final String price, final String currency, final float miles) {
		this.name = name;
		this.dir = dir;
		this.from = new Date(startDate);
		this.to = new Date(endDate);
		this.currency = WBCurrency.getInstance(currency);
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
		this.miles = miles;
	}
	
	public TripRow(final File dir, final Date startDate, final Date endDate, final String currency) {
		this.name = dir.getName();
		this.dir = dir;
		this.from = startDate;
		this.to = endDate;
		this.price = "0.00";
		this.currency = WBCurrency.getInstance(currency);
		this.miles = 0;
	}
	
	public TripRow(final File dir, final Date startDate, final Date endDate, final WBCurrency currency) {
		this.name = dir.getName();
		this.dir = dir;
		this.from = startDate;
		this.to = endDate;
		this.price = "0.00";
		this.currency = currency;
		this.miles = 0;
	}
	
	public String getMilesString() {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setGroupingUsed(false);
		return decimalFormat.format(this.miles);
	}
	
}
