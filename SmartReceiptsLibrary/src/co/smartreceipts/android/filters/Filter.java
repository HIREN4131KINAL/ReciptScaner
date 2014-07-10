package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;



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

}
