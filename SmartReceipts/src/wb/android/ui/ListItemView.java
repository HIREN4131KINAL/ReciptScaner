package wb.android.ui;

import android.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListItemView extends LinearLayout {

	public static final LinearLayout.LayoutParams ICON_PARAMS = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	public static final LinearLayout.LayoutParams TEXT_PARAMS = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	
	public ListItemView(final Context context, final String left, final int leftWidth, final String top, final String bottom) {
		super(context);
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(0, 6, 0, 6);
		setBackgroundResource(R.drawable.list_selector_background);
		ICON_PARAMS.setMargins(6, 0, 6, 0);
		
		TextView leftText = new TextView(context);
		leftText.setTextSize(24);
		leftText.setTypeface(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
		leftText.setText(left);
		leftText.setGravity(Gravity.CENTER_VERTICAL);
		if (leftWidth > 0)
			leftText.setWidth(leftWidth);
		 
		TextView topText = new TextView(context);
		topText.setTextSize(18);
		topText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		topText.setText(top);
		topText.setGravity(Gravity.LEFT);
		
		TextView bottomText = new TextView(context);
		bottomText.setTextSize(14);
		bottomText.setText(bottom);
		bottomText.setGravity(Gravity.LEFT);
		
		LinearLayout panel = new LinearLayout(context);
		panel.setOrientation(LinearLayout.VERTICAL);
		panel.addView(topText); 
		panel.addView(bottomText);  
		
		addView(leftText, ICON_PARAMS);
		addView(panel, TEXT_PARAMS);
		setGravity(Gravity.CENTER_VERTICAL);
	}
	
	public ListItemView(final Context context, final String left, final String top, final String bottom) {
		super(context);
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(0, 6, 0, 6);
		setBackgroundResource(R.drawable.list_selector_background);
		ICON_PARAMS.setMargins(6, 0, 6, 0);
		
		TextView leftText = new TextView(context);
		leftText.setTextSize(24);
		leftText.setTypeface(Typeface.DEFAULT_BOLD, Typeface.NORMAL);
		leftText.setText(left);
		leftText.setGravity(Gravity.CENTER_VERTICAL);
		 
		TextView topText = new TextView(context);
		topText.setTextSize(18);
		topText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		topText.setText(top);
		topText.setGravity(Gravity.LEFT);
		
		TextView bottomText = new TextView(context);
		bottomText.setTextSize(14);
		bottomText.setText(bottom);
		bottomText.setGravity(Gravity.LEFT);
		
		LinearLayout panel = new LinearLayout(context);
		panel.setOrientation(LinearLayout.VERTICAL);
		panel.addView(topText); 
		panel.addView(bottomText);  
		
		addView(leftText, ICON_PARAMS);
		addView(panel, TEXT_PARAMS);
		setGravity(Gravity.CENTER_VERTICAL);
	}
	
	public ListItemView(final Context context, final String top, final String bottom) {
		super(context);
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(0, 6, 0, 6);
		setBackgroundResource(R.drawable.list_selector_background);
		ICON_PARAMS.setMargins(6, 0, 6, 0);
		 
		TextView topText = new TextView(context);
		topText.setTextSize(20);
		topText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		topText.setText(top);
		topText.setGravity(Gravity.LEFT);
		
		TextView bottomText = new TextView(context);
		bottomText.setTextSize(14);
		bottomText.setText(bottom);
		bottomText.setGravity(Gravity.LEFT);
		
		LinearLayout panel = new LinearLayout(context);
		panel.setOrientation(LinearLayout.VERTICAL);
		panel.addView(topText); 
		panel.addView(bottomText);  
		
		addView(panel, TEXT_PARAMS);
		setGravity(Gravity.CENTER_VERTICAL);
	}
	
	public ListItemView(final Context context, final String text, final int drawableID) {
		super(context);
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(0, 6, 0, 6);
		setBackgroundResource(R.drawable.list_selector_background);
		ICON_PARAMS.setMargins(2, 0, 2, 0);
		
		ImageView icon = new ImageView(context);
		icon.setImageDrawable(context.getResources().getDrawable(drawableID));
		
		TextView textName = new TextView(context);
		textName.setTextSize(24);
		textName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		textName.setText(text);

		addView(icon, ICON_PARAMS);
		addView(textName, TEXT_PARAMS);      
		setGravity(Gravity.CENTER_VERTICAL);
	}

}
