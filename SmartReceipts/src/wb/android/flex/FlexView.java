package wb.android.flex;

import org.xml.sax.Attributes;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

class FlexView {
	
	private static final String TAG = "FlexView";
	private static final boolean D = Flex.D;
	
	private final FlexViews.Element element;
	
	//View Tags
	private String android_checked, android_layout_height, android_layout_weight, android_layout_width, android_id, android_tag, android_visibility;
	
	//Cached from Tags
	private LinearLayout.LayoutParams params;
	private boolean checked;
	
	//EditText Tags
	private String android_hint;
	
	FlexView(Attributes attributes, FlexViews.Element element) { 
		this.element = element;
		android_hint = attributes.getValue(FlexViews.Attribute.HINT.tagName());
		android_layout_height = attributes.getValue(FlexViews.Attribute.LAYOUT_HEIGHT.tagName());
		android_layout_weight = attributes.getValue(FlexViews.Attribute.LAYOUT_WEIGHT.tagName());
		android_layout_width = attributes.getValue(FlexViews.Attribute.LAYOUT_WIDTH.tagName());
		android_id = attributes.getValue(FlexViews.Attribute.ID.tagName());
		android_tag = attributes.getValue(FlexViews.Attribute.TAG.tagName());
		android_visibility = attributes.getValue(FlexViews.Attribute.VISIBILITY.tagName());
		android_checked = attributes.getValue(FlexViews.Attribute.CHECKED.tagName());
	}
	
	void buildAndAddToParent(Context context, ViewGroup parent) throws FlexFailedException {
		//You can use "element.getViewClass().getConstructor(Context.class).newInstance(context);" to build these views. I don't think Android 2.2 is optimized to handle this yet (need to test with a real phone)
		if (element == FlexViews.Element.EDIT_TEXT) {
			EditText editText = new EditText(context);
			updateView(editText);
			if (android_hint != null) editText.setHint(android_hint);
			if (params == null)
				parent.addView(editText);
			else 
				parent.addView(editText, params);
		}
		else {
			throw new FlexFailedException("Unsupported Flex View Type");
		}
	}
	
	private static final String VISIBLE = "visible";
	private static final String INVISIBLE = "invisible";
	private static final String GONE = "gone";
	private void updateView(View view) {
		if (android_tag != null) view.setTag(android_tag);
		if (params == null) {
			if (android_layout_width != null && android_layout_height != null) {
				if (android_layout_weight == null) {
					try {
						int width = getLayoutInt(android_layout_width);
						int height = getLayoutInt(android_layout_height);
						if (D) Log.d(TAG, "width: " + width + "; height: " + height);
						params = new LayoutParams(width, height);
					} catch (FlexFailedException e) {
						Log.e(TAG, e.toString());
					}
				}
				else {
					try {
						int width = getLayoutInt(android_layout_width);
						int height = getLayoutInt(android_layout_height);
						int weight = parsePixelValue(android_layout_weight);
						if (D) Log.d(TAG, "width: " + width + "; height: " + height + "; weight: " + weight);
						params = new LayoutParams(width, height, weight);
					} catch (FlexFailedException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		}
		if (android_visibility != null) {
			if (android_visibility.equalsIgnoreCase(VISIBLE))
				view.setVisibility(View.VISIBLE);
			else if (android_visibility.equalsIgnoreCase(INVISIBLE))
				view.setVisibility(View.INVISIBLE);
			else if (android_visibility.equalsIgnoreCase(GONE))
				view.setVisibility(View.GONE);
		}
	}
	
	private static final String FILL_PARENT = "fill_parent";
	private static final String WRAP_CONTENT = "wrap_content";
	private static final String MATCH_PARENT = "match_parent"; //Deprecated
	private int getLayoutInt(String param) throws FlexFailedException {
		if (param.equalsIgnoreCase(FILL_PARENT) || param.equalsIgnoreCase(MATCH_PARENT))
			return LayoutParams.FILL_PARENT;
		else if (param.equalsIgnoreCase(WRAP_CONTENT))
			return LayoutParams.WRAP_CONTENT;
		else {
			return parsePixelValue(param);
		}
	}
	
	private static final String DP = "dp";
	private static final String DIP = "dip";
	private int parsePixelValue(String value) throws FlexFailedException {
		if (value.contains(DP)) {
			String size = value.substring(0, value.indexOf(DP));
			//TODO: TypedValue Conversion
			try {
				return Integer.parseInt(size);
			} catch (Exception e) {
				throw new FlexFailedException("Failed to derive layout data for \"" + value +"\" - " + e.toString());
			}
		}
		else if (value.contains(DIP)) {
			String size = value.substring(0, value.indexOf(DIP));
			//TODO: TypedValue Conversion
			try {
				return Integer.parseInt(size);
			} catch (Exception e) {
				throw new FlexFailedException("Failed to derive layout data for \"" + value +"\" - " + e.toString());
			}
		}
		else {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				throw new FlexFailedException("Failed to derive layout data for \"" + value +"\" - " + e.toString());
			}
		}
	}
	
	void update(View view) {
		updateView(view);
	}
	
	void update(EditText editText) {
		updateView(editText);
		if (android_hint != null) editText.setHint(android_hint);
	}
	
	void update(CheckBox checkBox) {
		updateView(checkBox);
		if (android_checked != null) {
			if (android_checked.equalsIgnoreCase("true")) checkBox.setChecked(true);
			else if (android_checked.equalsIgnoreCase("false")) checkBox.setChecked(false);
		}
	}
	
	FlexViews.Element element() {
		return this.element;
	}

}
