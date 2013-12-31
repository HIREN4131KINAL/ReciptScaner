package wb.receiptslibrary.fragments;

import java.io.File;

import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;
import wb.receiptslibrary.R;
import wb.receiptslibrary.activities.ReceiptsActivity;
import wb.receiptslibrary.activities.Sendable;
import wb.receiptslibrary.activities.SmartReceiptsActivity;
import wb.receiptslibrary.date.DateEditText;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.workers.EmailAssistant;
import wb.receiptslibrary.workers.ImportTask;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// TODO: Extend ListFragment
public class TripFragment extends WBFragment implements BooleanTaskCompleteDelegate, DatabaseHelper.TripRowListener {

	public static final String TAG = "TripViewHolder";
	
	private static final CharSequence[] RESERVED_CHARS = {"|","\\","?","*","<","\"",":",">","+","[","]","/","'","\n","\r","\t","\0","\f"};
	
	private TripAdapter mAdapter;
	private AutoCompleteAdapter mAutoCompleteAdapter;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private boolean mIsFirstPass;  //Tracks that this is the first time we're using this
	private boolean mIsDualPane;
	
	public static TripFragment newInstance() {
		return new TripFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mIsFirstPass = true;
        mIsDualPane = getResources().getBoolean(R.bool.isTablet);
		mAdapter = new TripAdapter(this, new TripRow[0]); //Quick Hack to provide an empty set
		getPersistenceManager().getDatabase().registerTripRowListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getWorkerManager().getLogger().logInformation("/TripView");
		View rootView = inflater.inflate(getLayoutId(), container, false);
		mListView = (ListView) rootView.findViewById(R.id.listview);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
		mListView.setAdapter(mAdapter);
		getWorkerManager().getAdManager().handleAd(rootView);
		return rootView;
	}
	
	public int getLayoutId() {
		return R.layout.listlayout;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mAutoCompleteAdapter != null) {
			mAutoCompleteAdapter.onPause();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPersistenceManager().getDatabase().getTripsParallel();
		getSherlockActivity().getSupportActionBar().setTitle(getFlexString(R.string.app_name));
		// Handle Import Code
		if (getSherlockActivity().getIntent().getAction() != null && getSherlockActivity().getIntent().getAction().equalsIgnoreCase(Intent.ACTION_VIEW)) {
        	final Uri uri = getSherlockActivity().getIntent().getData();
        	final CheckBox overwrite = new CheckBox(getSherlockActivity()); overwrite.setText(" Overwrite Existing Data?");
        	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
        	builder.setTitle("Import")
        	   .setView(overwrite)
			   .setCancelable(true)
			   .setPositiveButton("Import", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						(new ImportTask(getSherlockActivity(), TripFragment.this, "Importing your files...", IMPORT_TASK_ID, overwrite.isChecked(), getPersistenceManager())).execute(uri);
					}
			    })
			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
			    })
			   .show();
        }
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getPersistenceManager().getDatabase().unregisterTripRowListener(this);
	}
	
	public final void tripMenu(final TripRow trip) {
		final PersistenceManager persistenceManager = getPersistenceManager();
		if (!persistenceManager.getStorageManager().isExternal()) {
    		Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    		return;
    	}

    	final boolean newTrip = (trip == null);
    	
		final View scrollView = getFlex().getView(R.layout.dialog_tripmenu);
		final AutoCompleteTextView nameBox = (AutoCompleteTextView) getFlex().getSubView(scrollView, R.id.DIALOG_TRIPMENU_NAME);
		final DateEditText startBox = (DateEditText) getFlex().getSubView(scrollView, R.id.DIALOG_TRIPMENU_START);
		final DateEditText endBox = (DateEditText) getFlex().getSubView(scrollView, R.id.DIALOG_TRIPMENU_END);
		
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
				        	final CheckBox overwrite = new CheckBox(getSherlockActivity()); overwrite.setText(" Overwrite Existing Data?");
				        	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
				        	builder.setTitle("Import")
				        	   .setView(overwrite)
							   .setCancelable(true)
							   .setPositiveButton("Import", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										(new ImportTask(getSherlockActivity(), TripFragment.this, "Importing your files...", IMPORT_TASK_ID, overwrite.isChecked(), getPersistenceManager())).execute(uri);
									}
							    })
							   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										return;
									}
							    })
							   .show();
						}
					 }
					 dialog.cancel();   
				 }
			 })
			 .show();
	}
	
	public final boolean editTrip(final TripRow trip) {
		final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		final String[] editTripItems = getFlex().getStringArray(R.array.EDIT_TRIP_ITEMS);
		builder.setTitle(trip.getName())
			   .setCancelable(true)
			   .setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
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
	
	private int IMPORT_TASK_ID = 1;
    @Override
    public synchronized void onBooleanTaskComplete(int taskID, Boolean success) {
    	if (taskID == IMPORT_TASK_ID) {
    		if (success) {
    			Toast.makeText(getSherlockActivity(), "Successfully imported all files", Toast.LENGTH_LONG).show();
    		}
    		else {
    			Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
    		}
    		getPersistenceManager().getDatabase().getTripsParallel();
    	}
    }

	@Override
	public void onTripRowsQuerySuccess(TripRow[] trips) {
		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mAdapter.notifyDataSetChanged(trips);
		if (mIsFirstPass) { //Pre-Cache the receipts for the top two trips
			if (trips.length > 0) {
				getPersistenceManager().getDatabase().getReceiptsParallel(trips[0]);
			}
			if (trips.length > 1) {
				getPersistenceManager().getDatabase().getReceiptsParallel(trips[1]);
			}
			mIsFirstPass = false;
		}
		if (mIsDualPane) {
			final Fragment detailsFragment = getFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
			if (trips.length > 0 && detailsFragment == null) {
				// If we have trips and no fragment is present
				// Add whatever the last fragment was, so something shows up there
				getFragmentManager().beginTransaction().replace(R.id.content_details, ReceiptsFragment.newInstance(), ReceiptsFragment.TAG).commit();
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
			Toast.makeText(getSherlockActivity(), "Error: An expense report with that name already exists", Toast.LENGTH_SHORT).show();
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
		if (mIsDualPane) {
			getFragmentManager().beginTransaction().replace(R.id.content_details, ReceiptsFragment.newInstance(trip), ReceiptsFragment.TAG).commit();
		}
		else {
			final Intent intent = new Intent(getSherlockActivity(), ReceiptsActivity.class);
			intent.putExtra(TripRow.PARCEL_KEY, trip);
			intent.putExtra(SmartReceiptsActivity.ACTION_SEND_URI, ((Sendable)getSherlockActivity()).actionSendUri());
			startActivity(intent);
		}
	}

	@Override
	public void onTripRowUpdateFailure(TripRow newTrip, TripRow oldTrip, File directory) {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
		getPersistenceManager().getStorageManager().rename(directory, oldTrip.getName());
		if (mIsDualPane) {
			getFragmentManager().beginTransaction().replace(R.id.content_details, ReceiptsFragment.newInstance(newTrip), ReceiptsFragment.TAG).commit();
		}
	}

	@Override
	public void onTripDeleteSuccess(TripRow oldTrip) {
		if (oldTrip != null) {
			if (!getPersistenceManager().getStorageManager().deleteRecursively(oldTrip.getDirectory())) {
	    		Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
			}
		}
		if (mIsDualPane) {
			final Fragment detailsFragment = getFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
			if (detailsFragment != null) {
				getFragmentManager().beginTransaction().remove(detailsFragment).commit();
				getSherlockActivity().getSupportActionBar().setTitle(getFlexString(R.string.app_name));
			}
		}
		getPersistenceManager().getDatabase().getTripsParallel();
	}

	@Override
	public void onTripDeleteFailure() {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
	}
	
}
