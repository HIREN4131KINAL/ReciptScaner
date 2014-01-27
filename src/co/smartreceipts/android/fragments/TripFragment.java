package co.smartreceipts.android.fragments;

import java.io.File;

import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;
import android.app.Activity;
import android.content.DialogInterface;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.Attachable;
import co.smartreceipts.android.activities.Navigable;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.workers.EmailAssistant;
import co.smartreceipts.android.workers.ImportTask;

import com.actionbarsherlock.view.MenuItem;

// TODO: Extend ListFragment
public class TripFragment extends WBListFragment implements BooleanTaskCompleteDelegate, 
														    DatabaseHelper.TripRowListener,
														    AdapterView.OnItemLongClickListener {

	public static final String TAG = "TripFragment";
	
	private static final CharSequence[] RESERVED_CHARS = {"|","\\","?","*","<","\"",":",">","+","[","]","/","'","\n","\r","\t","\0","\f"};
	
	private TripCardAdapter mAdapter;
	private AutoCompleteAdapter mAutoCompleteAdapter;
	private boolean mIsFirstPass;  //Tracks that this is the first time we're using this
	private Navigable mNavigator;
	private Attachable mAttachable;
	private ProgressBar mProgressDialog;
	private TextView mNoDataAlert;
	
	public static TripFragment newInstance() {
		return new TripFragment();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Navigable && activity instanceof Attachable) {
			mNavigator = (Navigable) activity;
			mAttachable = (Attachable) activity;
		}
		else {
			throw new ClassCastException("The TripFragment's Activity must extend the Navigable and Attachable interfaces");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mIsFirstPass = true;
		mAdapter = new TripCardAdapter(getSherlockActivity(), getPersistenceManager().getPreferences());
		getPersistenceManager().getDatabase().registerTripRowListener(this);
		getWorkerManager().getLogger().logInformation("/TripView");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");
		View rootView = inflater.inflate(getLayoutId(), container, false);
		mProgressDialog = (ProgressBar) rootView.findViewById(R.id.progress);
		mNoDataAlert = (TextView) rootView.findViewById(R.id.no_data);
		getWorkerManager().getAdManager().handleAd(rootView);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(mAdapter); //Set this here to ensure this has been laid out already
		getListView().setOnItemLongClickListener(this);
	}
	
	public int getLayoutId() {
		return R.layout.trip_fragment_layout;
	}
	
	@Override
	public void onPause() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
		super.onPause();
		if (mAutoCompleteAdapter != null) {
			mAutoCompleteAdapter.onPause();
		}
	}
	
	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
		super.onResume();
		getPersistenceManager().getDatabase().getTripsParallel();
		getSherlockActivity().getSupportActionBar().setTitle(getFlexString(R.string.app_name));
		// Handles SMR imports
		final Attachment attachment = mAttachable.getAttachment();
		if (attachment.isValid() && attachment.isSMR() && attachment.isActionView()) {
			performImport(attachment.getUri());
		}
	}
	
	@Override
	public void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");
		super.onDestroy();
		getPersistenceManager().getDatabase().unregisterTripRowListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.trip_action_new) {
			tripMenu(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public final void tripMenu(final TripRow trip) {
		final PersistenceManager persistenceManager = getPersistenceManager();
		if (!persistenceManager.getStorageManager().isExternal()) {
    		Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    		return;
    	}

    	final boolean newTrip = (trip == null);
    	
		final View scrollView = getFlex().getView(getSherlockActivity(), R.layout.dialog_tripmenu);
		final AutoCompleteTextView nameBox = (AutoCompleteTextView) getFlex().getSubView(getSherlockActivity(), scrollView, R.id.DIALOG_TRIPMENU_NAME);
		final DateEditText startBox = (DateEditText) getFlex().getSubView(getSherlockActivity(), scrollView, R.id.DIALOG_TRIPMENU_START);
		final DateEditText endBox = (DateEditText) getFlex().getSubView(getSherlockActivity(), scrollView, R.id.DIALOG_TRIPMENU_END);
		
		//Show default dictionary with auto-complete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
		nameBox.setKeyListener(input);
		
		//Fill Out Fields
		if (newTrip) {
			if (persistenceManager.getPreferences().enableAutoCompleteSuggestions()) {
				final DatabaseHelper db = getPersistenceManager().getDatabase();
				if (mAutoCompleteAdapter == null) mAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getSherlockActivity(), DatabaseHelper.TAG_TRIPS, db, null);
				nameBox.setAdapter(mAutoCompleteAdapter);
			}
			startBox.setFocusableInTouchMode(false); startBox.setOnClickListener(getDateManager().getDurationDateEditTextListener(endBox));
		}
		else {
			if (trip.getDirectory() != null) nameBox.setText(trip.getName());
			if (trip.getStartDate() != null) { startBox.setText(trip.getFormattedStartDate(getSherlockActivity(), getPersistenceManager().getPreferences().getDateSeparator())); startBox.date = trip.getStartDate(); }
			if (trip.getEndDate() != null) { endBox.setText(trip.getFormattedEndDate(getSherlockActivity(), getPersistenceManager().getPreferences().getDateSeparator())); endBox.date = trip.getEndDate(); }
			startBox.setFocusableInTouchMode(false); startBox.setOnClickListener(getDateManager().getDateEditTextListener());
		}
		endBox.setFocusableInTouchMode(false); endBox.setOnClickListener(getDateManager().getDateEditTextListener());
		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end
		
		//Show the DialogController
		final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		builder.setTitle((newTrip)?getFlexString(R.string.DIALOG_TRIPMENU_TITLE_NEW):getFlexString(R.string.DIALOG_TRIPMENU_TITLE_EDIT))
			 .setCancelable(true)
			 .setView(scrollView)
			 .setLongLivedPositiveButton((newTrip)?getFlexString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_CREATE):getFlexString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_UPDATE), new LongLivedOnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					 String name = nameBox.getText().toString().trim();
					 final String startDate = startBox.getText().toString();
					 final String endDate = endBox.getText().toString();
					 //Error Checking
					 if (name.length() == 0 || startDate.length() == 0 || endDate.length() == 0) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.DIALOG_TRIPMENU_TOAST_MISSING_FIELD), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (startBox.date == null || endBox.date == null) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (startBox.date.getTime() > endBox.date.getTime()) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.DURATION_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (name.startsWith(" ")) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.SPACE_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 for (int i=0; i < RESERVED_CHARS.length; i++) {
						 if (name.contains(RESERVED_CHARS[i])) {
							 Toast.makeText(getSherlockActivity(), getFlexString(R.string.ILLEGAL_CHAR_ERROR), Toast.LENGTH_SHORT).show();
							 return;
						 }
					 }
					 if (newTrip) { //Insert
						 File dir = persistenceManager.getStorageManager().mkdir(name);
						 if (dir != null) {
							 persistenceManager.getDatabase().insertTripParallel(dir, startBox.date, endBox.date);
						 }
						 else {
							 Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
						 }
						 dialog.cancel();
					 }
					 else { //Update
						 final File dir = persistenceManager.getStorageManager().rename(trip.getDirectory(), name);
						 if (dir == trip.getDirectory()) {
							 Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
							 return;
						 }
						 persistenceManager.getDatabase().updateTripParallel(trip, dir, (startBox.date != null) ? startBox.date : trip.getStartDate(), (endBox.date != null) ? endBox.date : trip.getStartDate());
						 dialog.cancel();
					 }
				}
			 })
			 .setNegativeButton(getFlexString(R.string.DIALOG_TRIPMENU_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 String name = nameBox.getText().toString().trim();
					 if (name != null && name.equalsIgnoreCase("_import_")) {
						File smr = persistenceManager.getStorageManager().getFile("SmartReceipts.smr");
						if (smr != null && smr.exists()) {
							final Uri uri = Uri.fromFile(smr);
				        	performImport(uri);
						}
					 }
					 dialog.cancel();   
				 }
			 })
			 .show();
	}
	
	public final boolean editTrip(final TripRow trip) {
		final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		final String[] editTripItems = getFlex().getStringArray(getSherlockActivity(), R.array.EDIT_TRIP_ITEMS);
		builder.setTitle(trip.getName())
			   .setCancelable(true)
			   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .setItems(editTripItems, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	final String selection = editTripItems[item].toString();
				    	if (selection == editTripItems[0]) { //Email Trip
				    		TripFragment.this.emailTrip(trip);
				    	}
				    	else if (selection == editTripItems[1]) //Edit Trip
				    		TripFragment.this.tripMenu(trip); 
				    	else if (selection == editTripItems[2]) //Delete Trip
				    		TripFragment.this.deleteTrip(trip);
				    	dialog.cancel();
				    }
				})
				.show();
    	return true;
    }
	
	public void emailTrip(TripRow trip) {
    	EmailAssistant.email(getSmartReceiptsApplication(), getSherlockActivity(), trip);
    }
	
	public final void deleteTrip(final TripRow trip) {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		builder.setTitle(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON_TITLE_START) + " " + trip.getName() + getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON_TITLE_END))
			   .setCancelable(true)
			   .setPositiveButton(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               getPersistenceManager().getDatabase().deleteTripParallel(trip);
		           }
		       })
		       .setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	viewReceipts(mAdapter.getItem(position));
    	//v.setSelected(true);
    }
    
    @Override
	public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
    	editTrip(mAdapter.getItem(position));
		return true;
	}

	@Override
	public void onTripRowsQuerySuccess(TripRow[] trips) {
		mProgressDialog.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
		if (trips == null || trips.length == 0) {
			mNoDataAlert.setVisibility(View.VISIBLE);
		}
		else {
			mNoDataAlert.setVisibility(View.INVISIBLE);
		}
		mAdapter.notifyDataSetChanged(trips);
		if (mIsFirstPass) { //Pre-Cache the receipts for the top two trips
			mIsFirstPass = false;
			if (trips.length > 0) {
				getPersistenceManager().getDatabase().getReceiptsParallel(trips[0], true);
			}
			if (trips.length > 1) {
				getPersistenceManager().getDatabase().getReceiptsParallel(trips[1], true);
			}
			if (trips.length > 0) {
				// If we have trips, open up whatever one was last
				viewReceipts(null);
			}
		}
	}

	@Override
	public void onTripRowInsertSuccess(TripRow trip) {
		viewReceipts(trip);
		getPersistenceManager().getDatabase().getTripsParallel();
	}

	@Override
	public void onTripRowInsertFailure(SQLException ex, File directory) {
		if (ex != null)
			Toast.makeText(getSherlockActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_SHORT).show();
		else {
			Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
			getPersistenceManager().getStorageManager().delete(directory);
		}
		
	}

	@Override
	public void onTripRowUpdateSuccess(TripRow trip) {
		getPersistenceManager().getDatabase().getTripsParallel();
		viewReceipts(trip);
	}
	
	public void viewReceipts(TripRow trip) {
		mNavigator.viewReceipts(trip);
	}

	@Override
	public void onTripRowUpdateFailure(TripRow newTrip, TripRow oldTrip, File directory) {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
		getPersistenceManager().getStorageManager().rename(directory, oldTrip.getName());
		viewReceipts(newTrip);
	}

	@Override
	public void onTripDeleteSuccess(TripRow oldTrip) {
		if (oldTrip != null) {
			if (!getPersistenceManager().getStorageManager().deleteRecursively(oldTrip.getDirectory())) {
	    		Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
			}
		}
		final Fragment detailsFragment = getFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
		if (detailsFragment != null) {
			getFragmentManager().beginTransaction().remove(detailsFragment).commit();
			getSherlockActivity().getSupportActionBar().setTitle(getFlexString(R.string.app_name));
		}
		getPersistenceManager().getDatabase().getTripsParallel();
	}

	@Override
	public void onTripDeleteFailure() {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
	}
	
	private void performImport(final Uri uri) {
    	final CheckBox overwrite = new CheckBox(getSherlockActivity()); overwrite.setText(" Overwrite Existing Data?");
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
    	builder.setTitle(R.string.import_string)
    	   .setView(overwrite)
		   .setCancelable(true)
		   .setPositiveButton(R.string.import_string, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					(new ImportTask(getSherlockActivity(), TripFragment.this, getString(R.string.progress_import), IMPORT_TASK_ID, overwrite.isChecked(), getPersistenceManager())).execute(uri);
				}
		    })
		   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
		    })
		   .show();
	}
	
	private int IMPORT_TASK_ID = 1;
    @Override
    public synchronized void onBooleanTaskComplete(int taskID, Boolean success) {
    	if (taskID == IMPORT_TASK_ID) {
    		if (success) {
    			Toast.makeText(getSherlockActivity(), R.string.toast_import_complete, Toast.LENGTH_LONG).show();
    			getSherlockActivity().finish(); //TODO: Fix this hack - finishing the activity to get rid of the old Intent so we don't reshow import dialog
    		}
    		else {
    			Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
    		}
    		getPersistenceManager().getDatabase().getTripsParallel();
    	}
    } 
	
}