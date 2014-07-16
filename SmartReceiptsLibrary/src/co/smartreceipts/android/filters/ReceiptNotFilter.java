package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;

/**
 * A filter implementation of {@link NotFilter} for {@link ReceiptRow}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptNotFilter extends NotFilter<ReceiptRow> {

	public ReceiptNotFilter(Filter<ReceiptRow> filter) {
		super(filter);
	}

	protected ReceiptNotFilter(JSONObject json) throws JSONException {
		super(json);
	}

	@Override
	Filter<ReceiptRow> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getReceiptFilter(json);
	}

}
