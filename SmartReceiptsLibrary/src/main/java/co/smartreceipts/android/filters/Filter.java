package co.smartreceipts.android.filters;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;


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
	
	/**
	 * @return a {@link JSONObject} that represents this particular filter. This is used to enable us to
	 * reconstruct filters if persistence is desired.
	 * @throws {@link JSONException} if invalid parameters were present
	 */
	public JSONObject getJsonRepresentation() throws JSONException;


	/**
	 * @return a complete {@link List} of {@link Filter} objects that are considered as children to this {@link Filter}
	 *         or {@code null} if it does not contain any children
	 */
	public List<Filter<T>> getChildren();


	/**
	 * @return an Android {@link string} resource for this filter's display name
	 */
	public int getNameResource();


	/**
	 * @return the {@link FilterType} of this particular filter
	 */
	public FilterType getType();

}
