package co.smartreceipts.android.filters;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;

/**
 * A filter implementation of {@link OrFilter} for {@link ReceiptRow}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptOrFilter extends OrFilter<ReceiptRow>{

	public ReceiptOrFilter() {
		super();
	}
	
	public ReceiptOrFilter(List<Filter<ReceiptRow>> filters) {
		super(filters);
	}
	
	protected ReceiptOrFilter(JSONObject json) throws JSONException {
		super(json);
	}
	
	
	@Override
	Filter<ReceiptRow> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getReceiptFilter(json);
	}

}
