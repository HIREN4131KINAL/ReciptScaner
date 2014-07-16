package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.TripRow;

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
		else if (ReceiptAndFilter.class.getName().equals(className)) {
			return new ReceiptAndFilter(json);
		} 
		else if (ReceiptNotFilter.class.getName().equals(className)) {
			return new ReceiptNotFilter(json);
		} 
		else if (ReceiptIsExpensableFilter.class.getName().equals(className)) {
			return new ReceiptIsExpensableFilter(json);
		} 
		else if (ReceiptSelectedFilter.class.getName().equals(className)) {
			return new ReceiptSelectedFilter(json);
		} 
		else if (ReceiptCategoryFilter.class.getName().equals(className)) {
			return new ReceiptCategoryFilter(json);
		} 
		else if (ReceiptMaximumPriceFilter.class.getName().equals(className)) {
			return new ReceiptMaximumPriceFilter(json);
		} 
		else if (ReceiptMinimumPriceFilter.class.getName().equals(className)) {
			return new ReceiptMinimumPriceFilter(json);
		} 
		else if (ReceiptOnOrAfterDayFilter.class.getName().equals(className)) {
			return new ReceiptOnOrAfterDayFilter(json);
		} 
		else if (ReceiptOnOrBeforeDayFilter.class.getName().equals(className)) {
			return new ReceiptOnOrBeforeDayFilter(json);
		} 
		else {
			return null;
		}
	}
	
	/**
	 * Builds a {@link Filter} for a {@link TripRow} based on a {@link JSONObject}
	 * 
	 * @param json - the {@link JSONObject} that represents this filter
	 * @return a {@link Filter} of {@link TripRow}
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	public static final Filter<TripRow> getTripFilter(JSONObject json) throws JSONException {
		final String className = json.getString(CLASS_NAME);
		
		if(TripAndFilter.class.getName().equals(className)){
			return new TripAndFilter(json); 
		} 
		else if(TripEndsOnOrAfterDayFilter.class.getName().equals(className)){
			return new TripEndsOnOrAfterDayFilter(json); 
		} 
		else if(TripEndsOnOrBeforeDayFilter.class.getName().equals(className)){
			return new TripEndsOnOrBeforeDayFilter(json); 
		} 
		else if(TripMaximumPriceFilter.class.getName().equals(className)){
			return new TripMaximumPriceFilter(json); 
		} 
		else if(TripMinimumPriceFilter.class.getName().equals(className)){
			return new TripMinimumPriceFilter(json); 
		} 
		else if(TripNotFilter.class.getName().equals(className)){
			return new TripNotFilter(json); 
		} 
		else if(TripOrFilter.class.getName().equals(className)){
			return new TripOrFilter(json); 
		} 
		else if(TripStartsOnOrAfterDayFilter.class.getName().equals(className)){
			return new TripStartsOnOrAfterDayFilter(json); 
		} 
		else if(TripStartsOnOrBeforeDayFilter.class.getName().equals(className)){
			return new TripStartsOnOrBeforeDayFilter(json); 
		} 
		else {
			return null;
		}
	}
}
