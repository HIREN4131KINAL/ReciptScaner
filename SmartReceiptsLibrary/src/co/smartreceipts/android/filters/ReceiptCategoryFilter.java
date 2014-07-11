package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.model.ReceiptRow;

/**
 * A filter implementation that tests if the category of a {@link ReceiptRow} as determined
 * by {@link ReceiptRow#getCategory()} is equal to a predefined value
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptCategoryFilter implements Filter<ReceiptRow> {

	private static final String CATEGORY = "category";
	
	private final String mCategory;
	
	/**
	 * Default constructor for this {@link Filter} that takes in a specific category {@link String}
	 * that our {@link ReceiptRow#getCategory()} must equal for the {@link #accept(ReceiptRow)} to
	 * return true
	 * 
	 * @param category - the category to check
	 */
	public ReceiptCategoryFilter(final String category) {
		if (category == null) {
			throw new IllegalArgumentException("ReceiptCategoryFilter requires a non-null category");
		}
		mCategory = category;
	}
	
	/**
	 * A package-private constructor that enables us to recreate this filter via a
	 * {@link JSONObject} representation
	 * 
	 * @param json - the {@link JSONObject} representation of this filter
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	ReceiptCategoryFilter(JSONObject json) throws JSONException {
		mCategory = json.getString(CATEGORY);
	}
	
	@Override
	public boolean accept(ReceiptRow t) {
		return mCategory.equals(t.getCategory());
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(CATEGORY, mCategory);
		return json;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mCategory == null) ? 0 : mCategory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		ReceiptCategoryFilter other = (ReceiptCategoryFilter) obj;
		if (mCategory == null) {
			if (other.mCategory != null) {
				return false;
			}
		} 
		else if (!mCategory.equals(other.mCategory)) {
			return false;
		}
		return true;
	}
	
	

}
