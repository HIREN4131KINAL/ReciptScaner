package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;

/**
 * Factory that allows us to rebuild our previous filter list from a {@link JSONObject}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 *
 */
public class FilterFactory {
	
	static final String CLASS_NAME = "class";

	private FilterFactory() { }
	
	/**
	 * Builds a {@link Filter} for a {@link ReceiptRow} based on a {@link JSONObject}
	 * 
	 * @param json - the {@link JSONObject} that represents this filter
	 * @return a {@link Filter} of {@link ReceiptRow}
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	public static final Filter<ReceiptRow> getReceiptFilter(JSONObject json) throws JSONException {
		final String className = json.getString(CLASS_NAME);
		if (ReceiptOrFilter.class.getName().equals(className)) {
			return new ReceiptOrFilter(json);
		}
		else if (ReceiptCategoryFilter.class.getName().equals(className)) {
			return new ReceiptCategoryFilter(json);
		}
		else {
			return null;
		}
	}
}
