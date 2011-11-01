package wb.receiptspro;

import java.io.File;
import java.sql.Date;

public final class TripRow {
	
	public final File dir;
	public String price;
	public final Date from, to;
	
	public TripRow(final String path, final long startDate, final long endDate, final String price) {
		this.dir = new File(path);
		this.from = new Date(startDate);
		this.to = new Date(endDate);
		if (price == null)
			this.price = "0.00";
		else
			this.price = price;
	}
	
	public TripRow(final File dir, final Date startDate, final Date endDate) {
		this.dir = dir;
		this.from = startDate;
		this.to = endDate;
		this.price = "0.00";
	}
	
}
