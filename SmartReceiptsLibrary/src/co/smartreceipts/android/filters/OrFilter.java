package co.smartreceipts.android.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A filter implementation that combines multiple other {@link Filter} implementations
 * in the manner of a logical OR operation.
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class OrFilter<T> implements Filter<T> {

	private final CopyOnWriteArrayList<Filter<T>> mFilters;
	
	public OrFilter() {
		mFilters = new CopyOnWriteArrayList<Filter<T>>();
	}
	
	public OrFilter(List<Filter<T>> filters) {
		mFilters = new CopyOnWriteArrayList<Filter<T>>(filters);
	}
	
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
		for (Filter<T> filter : mFilters) {
			if (filter.accept(t)) {
				return true;
			}
		}
		return false;
	}

}
