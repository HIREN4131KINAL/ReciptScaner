package wb.android.autocomplete;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

public class AutoCompleteAdapter extends CursorAdapter implements Filterable, FilterQueryProvider {

	private final QueryListener mQueryListener;
	private final CharSequence mTag; 
	private final MyClickListener mClickListener;
	private final ItemSelectedListener mItemSelectedListener;
	private final int mCol;
	private String mLastString;
	
	public interface ItemSelectedListener {
		public void onItemSelected(CharSequence text, CharSequence tag);
	}
	
	public interface QueryListener {
		public Cursor getAutoCompleteCursor(CharSequence text, CharSequence tag);
	}
	
	@SuppressWarnings("deprecation")
	private AutoCompleteAdapter(Activity activity, Cursor cursor, QueryListener mQueryListener, CharSequence mTag, ItemSelectedListener itemSelectedListener, int col) {
		super(activity, cursor);
		this.mQueryListener = mQueryListener;
		this.mTag = mTag;
		this.mLastString = "";
		setFilterQueryProvider(this);
		this.mClickListener = new MyClickListener();
		mItemSelectedListener = itemSelectedListener;
		mCol = col;
	}
	
	public static AutoCompleteAdapter getInstance(Activity activity, CharSequence mTag, QueryListener queryListener, ItemSelectedListener itemSelectedListener) {
		return getInstance(activity, mTag, queryListener, itemSelectedListener, 0);
	}
	
	public static AutoCompleteAdapter getInstance(Activity activity, CharSequence mTag, QueryListener queryListener, ItemSelectedListener itemSelectedListener, int col) {
		Cursor cursor = queryListener.getAutoCompleteCursor("", mTag);
		return new AutoCompleteAdapter(activity, cursor, queryListener, mTag, itemSelectedListener, col);
	}
	
	public void reset() {
		this.mLastString = "";
		onPause();
	}
	
	public final void onPause() {
		Cursor cursor = getCursor();
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String text = cursor.getString(mCol);
		if (text.trim().equalsIgnoreCase(mLastString.trim())) {
			view.getRootView().setVisibility(View.INVISIBLE);
			//TODO: Push on item selected...
		}
		else {
			((TextView) view).setText(text);
			view.getRootView().setVisibility(View.VISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		if (mItemSelectedListener != null && parent instanceof ListView) { // Should always be the case (but to prevent against updates, etc)
			((ListView) parent).setOnItemClickListener(mClickListener);
		}
        final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        String item = cursor.getString(mCol);
        view.setText(item);
        return view;
	}
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(mCol);
	}
	
	private class MyClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mItemSelectedListener.onItemSelected(((TextView)view).getText(), mTag);
		}
		
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		mLastString = (constraint == null) ? "" : constraint.toString();
        return mQueryListener.getAutoCompleteCursor(mLastString, mTag);
	}

}