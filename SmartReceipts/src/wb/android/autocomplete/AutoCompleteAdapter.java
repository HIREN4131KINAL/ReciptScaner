package wb.android.autocomplete;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class AutoCompleteAdapter extends CursorAdapter implements Filterable {

	private final AutoCompleteQueriable queriable;
	private final CharSequence tag;
	private final Cursor cursor;
	private final Activity activity;
	
	private AutoCompleteAdapter(Activity activity, Cursor cursor, AutoCompleteQueriable queriable, CharSequence tag) {
		super(activity, cursor);
		this.activity = activity;
		this.cursor = cursor;
		this.queriable = queriable;
		this.tag = tag;
		activity.startManagingCursor(cursor);
	}
	
	public static AutoCompleteAdapter getInstance(Activity activity, AutoCompleteQueriable queriable, CharSequence tag) {
		Cursor cursor = queriable.getAutoCompleteCursor("", tag);
		return new AutoCompleteAdapter(activity, cursor, queriable, tag);
	}
	
	public final void onPause() {
		activity.stopManagingCursor(cursor);
		cursor.close();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String text = cursor.getString(0);
		((TextView) view).setText(text);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        String item = cursor.getString(0);
        view.setText(item);
        return view;
	}
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(0);
	}
	
	@Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null)
            return getFilterQueryProvider().runQuery(constraint);
        String input = (constraint == null) ? "" : constraint.toString();
        return queriable.getAutoCompleteCursor(input, tag);
    }

}