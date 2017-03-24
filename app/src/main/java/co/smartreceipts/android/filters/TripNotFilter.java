package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.Trip;

/**
 * A filter implementation of {@link NotFilter} for {@link Trip}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class TripNotFilter extends NotFilter<Trip> {

	public TripNotFilter(Filter<Trip> filter) {
		super(filter);
	}

	protected TripNotFilter(JSONObject json) throws JSONException {
		super(json);
	}

	@Override
	Filter<Trip> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getTripFilter(json);
	}

}
