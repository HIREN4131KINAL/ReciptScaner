package co.smartreceipts.android.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A filter implementation that combines multiple other {@link Filter} implementations
 * in the manner of a logical OR operation.
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public abstract class OrFilter<T> implements Filter<T> {

	private static final String OR_FILTERS = "or_filters";
	
	private final CopyOnWriteArrayList<Filter<T>> mFilters;
	
	/**
	 * Additional logical OR calls may be added to this composite {@link Filter} via 
	 * the {@link #or(Filter)} method.
	 */
	public OrFilter() {
		mFilters = new CopyOnWriteArrayList<Filter<T>>();
	}
	
	/**
	 * A preset list of logical OR filters may be added to this constructor so that 
	 * chaining via the {@link #or(Filter)} method is not required
	 * 
	 * @param filters - the {@link List} of {@link Filter} to add
	 */
	public OrFilter(List<Filter<T>> filters) {
		mFilters = new CopyOnWriteArrayList<Filter<T>>(filters);
	}
	
	/**
	 * A package-private constructor that enables us to recreate this filter via a
	 * {@link JSONObject} representation
	 * 
	 * @param json - the {@link JSONObject} representation of this filter
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	protected OrFilter(JSONObject json) throws JSONException {
		final List<Filter<T>> filters = new ArrayList<Filter<T>>();
		final JSONArray filtersArray = json.getJSONArray(OR_FILTERS);
		for (int i=0; i < filtersArray.length(); i++) {
			filters.add(getFilter(json));
		}
		mFilters = new CopyOnWriteArrayList<Filter<T>>(filters);
	}
	
	/**
	 * Retrieves a {@link Filter} implementation from a given JSON object. This is required in order 
	 * to properly reconstruct our filters from JSON
	 * 
	 * @param json - the {@link JSONObject} representing a particular filter
	 * @return a {@link Filter} implementation
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	abstract Filter<T> getFilter(JSONObject json) throws JSONException;
	
	/**
	 * Adds another filter for the logical OR comparison that will be performed
	 * via the {@link #accept(Object)} method is called.
	 * 
	 * @param filter - the {@link Filter} to add
	 * @return this instance of {@link OrFilter} for method chaining
	 */
	public OrFilter<T> or(final Filter<T> filter) {
		mFilters.add(filter);
		return this;
	}
	
	@Override
	public boolean accept(final T t) {
		for (final Filter<T> filter : mFilters) {
			if (filter.accept(t)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONArray filtersArray = new JSONArray();
		for (final Filter<T> filter : mFilters) {
			filtersArray.put(filter.getJsonRepresentation());
		}
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(OR_FILTERS, filtersArray);
		return json;
	}

}
