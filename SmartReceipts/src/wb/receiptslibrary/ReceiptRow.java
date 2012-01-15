package wb.receiptslibrary;

import java.io.File;
import java.sql.Date;
import java.util.Currency;

public class ReceiptRow {

	public File img;
	public final File parentDir;
	public final String name, category, comment, price;
	public final Date date;
	public final int id;
	public final boolean expensable, fullpage;
	public Currency currency;
	
	public ReceiptRow(final int id, final String imgPath, final String parentDirPath, final String name, final String category, 
			final long date, final String comment, final String price, final boolean expensable, final String currency, final boolean fullpage) {
		this.id = id;
		if (imgPath.equalsIgnoreCase(DatabaseHelper.NULL))
			this.img = null;
		else
			this.img = new File(imgPath);
		this.parentDir = new File(parentDirPath);
		this.name = name;
		this.category = category;
		this.date = new Date(date);
		this.comment = comment;
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
		this.expensable = expensable;
		this.fullpage = fullpage;
		try {
			this.currency = Currency.getInstance(currency);
		} catch (IllegalArgumentException e) {
			this.currency = null;
		}
	}
	
	public ReceiptRow(final int id, final File img, final File parentDir, final String name, final String category, 
			final Date date, final String comment, final String price, final boolean expensable, final String currency, final boolean fullpage) {
		this.id = id;
		this.img = img;
		this.parentDir = parentDir;
		this.name = name;
		this.category = category;
		this.date = date;
		this.comment = comment;
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
		this.expensable = expensable;
		this.fullpage = fullpage;
		try {
			this.currency = Currency.getInstance(currency);
		} catch (IllegalArgumentException e) {
			this.currency = null;
		}
	}
}
