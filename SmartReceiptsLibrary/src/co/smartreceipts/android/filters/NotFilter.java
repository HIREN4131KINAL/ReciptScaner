package co.smartreceipts.android.filters;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.R;

/**
 * A filter implementation that combines multiple other {@link Filter}
 * implementations in the manner of a logical NOT operation.
 * 
 * @author Will Baumann
 * @since July 12, 2014
 * 
 */
public abstract class NotFilter<T> implements Filter<T> {

	private static final String NOT_FILTER = "not_filter";

	private final Filter<T> mFilter;

	/**
	 * A preset list of logical NOT filters may be added to this constructor so
	 * that chaining via the {@link #not(Filter)} method is not required
	 * 
	 * @param filters - the {@link List} of {@link Filter} to add
	 */
	public NotFilter(Filter<T> filter) {
		mFilter = filter;
	}

	/**
	 * A package-private constructor that enables us to recreate this filter via
	 * a {@link JSONObject} representation
	 * 
	 * @param json - the {@link JSONObject} representation of this filter
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	protected NotFilter(JSONObject json) throws JSONException {
		final JSONObject filterJson = json.getJSONObject(NOT_FILTER);
		mFilter = getFilter(filterJson);
	}

	/**
	 * Retrieves a {@link Filter} implementation from a given JSON object. This
	 * is required in order to properly reconstruct our filters from JSON
	 * 
	 * @param json - the {@link JSONObject} representing a particular filter
	 * @return a {@link Filter} implementation
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	abstract Filter<T> getFilter(JSONObject json) throws JSONException;

	@Override
	public boolean accept(T t) {
		return !mFilter.accept(t);
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(NOT_FILTER, mFilter.getJsonRepresentation());
		return json;
	}

	@Override
	public List<Filter<T>> getChildren() {
		final ArrayList<Filter<T>> children = new ArrayList<Filter<T>>();
		children.add(mFilter);
		return children;
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_not;
	}

	@Override
	public FilterType getType() {
		return FilterType.Composite;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((mFilter == null) ? 0 : mFilter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		NotFilter<?> other = (NotFilter<?>) obj;
		
		if (mFilter == null) {
			if (other.mFilter != null)
				return false;
		} else if (!mFilter.equals(other.mFilter))
			return false;
		
		return true;
	}

}
