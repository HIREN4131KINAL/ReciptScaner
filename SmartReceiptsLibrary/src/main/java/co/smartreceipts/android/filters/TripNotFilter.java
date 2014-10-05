package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.TripRow;

/**
 * A filter implementation of {@link NotFilter} for {@link TripRow}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class TripNotFilter extends NotFilter<TripRow> {

	public TripNotFilter(Filter<TripRow> filter) {
		super(filter);
	}

	protected TripNotFilter(JSONObject json) throws JSONException {
		super(json);
	}

	@Override
	Filter<TripRow> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getTripFilter(json);
	}

}
