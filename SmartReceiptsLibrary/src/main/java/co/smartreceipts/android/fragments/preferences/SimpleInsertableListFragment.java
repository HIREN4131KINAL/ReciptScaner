package co.smartreceipts.android.fragments.preferences;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;

public abstract class SimpleInsertableListFragment<T> extends WBFragment implements View.OnClickListener {
	
	private ListView mListView;
	private Adapter<T> mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new Adapter<T>(this, getActivity(), getData());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.simple_list, container, false);
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_settings_categories, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings_add) {
			addItem();
			return true;
		}
		else if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * @return the {@link Adapter} that is being used by this fragment
	 */
	protected final Adapter<T> getAdapter() {
		return mAdapter;
	}
	
	/**
	 * @return - the data set used to populate this list fragment
	 */
	protected abstract List<T> getData();
	
	/**
	 * Shows the proper message in order to assist the user with inserting an item
	 */
	protected abstract void addItem();
	
	/**
	 * Gets a view for a particular item. Subclasses should implement the traditional view-holder pattern
	 *  
	 * @param item - the data item to use
	 * @param convertView - the recycled view or {@code null} if it does not yet exist
	 * @param parent - the parent view with which to add this to
	 * @return the properly constructed view object
	 */
	public abstract View getView(LayoutInflater inflater, T item, View convertView, ViewGroup parent);
	
	
	public static final class Adapter<T> extends BaseAdapter {
		
		private final SimpleInsertableListFragment<T> mParentFragment;
		private final List<T> mData;
		protected final LayoutInflater mInflater;
		
		public Adapter(final SimpleInsertableListFragment<T> parentFragment, final Context context, final List<T> data) {
			mParentFragment = parentFragment;
			mInflater = LayoutInflater.from(context);
			mData = data;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public T getItem(int i) {
			return mData.get(i);
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup parent) {
			final T item = getItem(i);
			return mParentFragment.getView(mInflater, item, convertView, parent);
		}
		
	}

}
