package co.smartreceipts.android.filters;



/**
 * Interface to enable filtering of a particular data type
 * 
 * @author Will Baumann
 * @since July 08, 2014
 *
 */
public interface Filter<T> {
	
	/**
	 * Apply a filter operation in order to determine if we should accept this particular object as part
	 * of our output total.
	 * @param T - the object to check
	 * @return {@link true} if it should be accepted, {@link false} otherwise
	 */
	public boolean accept(T t);

}
