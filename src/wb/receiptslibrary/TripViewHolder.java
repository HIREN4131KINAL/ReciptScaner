package wb.receiptslibrary;

import java.io.File;

import com.actionbarsherlock.view.MenuItem;

import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.DirectDialogOnClickListener;
import wb.android.dialog.DirectLongLivedOnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Toast;

public class TripViewHolder extends DateViewHolder implements BooleanTaskCompleteDelegate, DatabaseHelper.TripRowListener {
	
	@SuppressWarnings("unused") private static final boolean D = SmartReceiptsActivity.D;
	@SuppressWarnings("unused") private static final String TAG = "TripViewHolder";
	
	private static final CharSequence[] RESERVED_CHARS = {"|","\\","?","*","<","\"",":",">","+","[","]","/","'","\n","\r","\t","\0","\f"};
	
	private TripAdapter tripAdapter;
	private HomeHolder parent;
	private AutoCompleteAdapter autoCompleteAdapter;
	private boolean mIsFirstPass;  //Tracks that this is the first time we're using this

	public TripViewHolder(SmartReceiptsActivity activity) {
		super(activity);
		mIsFirstPass = true;
	}
	
	@Override
	public void onCreate() {
		activity.getSupportActionBar().setTitle(activity.getFlex().getString(R.string.app_name));
		this.getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		tripAdapter = new TripAdapter(this, new TripRow[0]); //Quick Hack to provide an empty set
		getParent().setAdapter(tripAdapter);
		activity.getDB().registerTripRowListener(this);
		activity.getDB().getTripsParallel();
		
        if (activity.getIntent().getAction().equalsIgnoreCase(Intent.ACTION_VIEW)) {
        	final Uri uri = activity.getIntent().getData();
        	final CheckBox overwrite = new CheckBox(activity); overwrite.setText(" Overwrite Existing Data?");
        	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
        	builder.setTitle("Import")
        	   .setView(overwrite)
			   .setCancelable(true)
			   .setPositiveButton("Import", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						(new ImportTask(activity, TripViewHolder.this, "Importing your files...", IMPORT_TASK_ID, overwrite.isChecked())).execute(uri);
					}
			    })
			   .setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
			    })
			   .show();
        }
	}
	
	public void onPause() {
		if (autoCompleteAdapter != null) {
			autoCompleteAdapter.onPause();
		}
	}
	
	public void onResume() {
		if (autoCompleteAdapter != null) {
			autoCompleteAdapter.onResume();
		}
	}
	
	public void onDestroy() {
		activity.getDB().unregisterTripRowListener(this);
	}
	
	@Override
	public void onFocus() {
		getParent().setMainLayout();
		activity.getSupportActionBar().setTitle(activity.getFlex().getString(R.string.app_name));
		activity.getDB().getTripsParallel();
		getParent().setAdapter(tripAdapter);
		this.getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return getParent().onParentOptionsItemSelected(item);
	}
	
	public final void tripMenu(final TripRow trip) {
		if (!activity.getStorageManager().isExternal()) {
    		Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    		return;
    	}

    	final boolean newTrip = (trip == null);
    	
		final View scrollView = activity.getFlex().getView(R.layout.dialog_tripmenu);
		final AutoCompleteTextView nameBox = (AutoCompleteTextView) activity.getFlex().getSubView(scrollView, R.id.DIALOG_TRIPMENU_NAME);
		final DateEditText startBox = (DateEditText) activity.getFlex().getSubView(scrollView, R.id.DIALOG_TRIPMENU_START);
		final DateEditText endBox = (DateEditText) activity.getFlex().getSubView(scrollView, R.id.DIALOG_TRIPMENU_END);
		
		//Show default dictionary with auto-complete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
		nameBox.setKeyListener(input);
		
		//Fill Out Fields
		if (newTrip) {
			if (getActivity().getPreferences().enableAutoCompleteSuggestions()) {
				if (autoCompleteAdapter == null) autoCompleteAdapter = AutoCompleteAdapter.getInstance(activity, activity.getDB(), DatabaseHelper.TAG_TRIPS);
				nameBox.setAdapter(autoCompleteAdapter);
			}
			startBox.setFocusableInTouchMode(false); startBox.setOnClickListener(getDurationDateEditTextListener(endBox));
		}
		else {
			if (trip.dir != null) nameBox.setText(trip.dir.getName());
			if (trip.from != null) { startBox.setText(DateFormat.getDateFormat(activity).format(trip.from)); startBox.date = trip.from; }
			if (trip.to != null) { endBox.setText(DateFormat.getDateFormat(activity).format(trip.to)); endBox.date = trip.to; }
			startBox.setFocusableInTouchMode(false); startBox.setOnClickListener(getDateEditTextListener());
		}
		endBox.setFocusableInTouchMode(false); endBox.setOnClickListener(getDateEditTextListener());
		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end
		
		//Show the DialogController
		final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		builder.setTitle((newTrip)?activity.getFlex().getString(R.string.DIALOG_TRIPMENU_TITLE_NEW):activity.getFlex().getString(R.string.DIALOG_TRIPMENU_TITLE_EDIT))
			 .setCancelable(true)
			 .setView(scrollView)
			 .setLongLivedPositiveButton((newTrip)?activity.getFlex().getString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_CREATE):activity.getFlex().getString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_UPDATE), new DirectLongLivedOnClickListener<SmartReceiptsActivity>(activity) {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					 String name = nameBox.getText().toString().trim();
					 final String startDate = startBox.getText().toString();
					 final String endDate = endBox.getText().toString();
					 //Error Checking
					 if (name.length() == 0 || startDate.length() == 0 || endDate.length() == 0) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.DIALOG_TRIPMENU_TOAST_MISSING_FIELD), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (startBox.date == null || endBox.date == null) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (startBox.date.getTime() > endBox.date.getTime()) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.DURATION_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (name.startsWith(" ")) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.SPACE_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 for (int i=0; i < RESERVED_CHARS.length; i++) {
						 if (name.contains(RESERVED_CHARS[i])) {
							 Toast.makeText(activity, activity.getFlex().getString(R.string.ILLEGAL_CHAR_ERROR), Toast.LENGTH_SHORT).show();
							 return;
						 }
					 }
					 if (newTrip) { //Insert
						 File dir = activity.getStorageManager().mkdir(name);
						 if (dir != null) {
							 activity.getDB().insertTripParallel(dir, startBox.date, endBox.date);
						 }
						 else {
							 Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
						 }
						 dialog.cancel();
					 }
					 else { //Update
						 final File dir = activity.getStorageManager().rename(trip.dir, name);
						 if (dir == trip.dir) {
							 Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
							 return;
						 }
						 activity.getDB().updateTripParallel(trip, dir, (startBox.date != null) ? startBox.date : trip.from, (endBox.date != null) ? endBox.date : trip.from);
						 dialog.cancel();
					 }
				}
			 })
			 .setNegativeButton(activity.getFlex().getString(R.string.DIALOG_TRIPMENU_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 String name = nameBox.getText().toString().trim();
					 if (name != null && name.equalsIgnoreCase("_import_")) {
						File smr = activity.getStorageManager().getFile("SmartReceipts.smr");
						if (smr != null && smr.exists()) {
							final Uri uri = Uri.fromFile(smr);
				        	final CheckBox overwrite = new CheckBox(activity); overwrite.setText(" Overwrite Existing Data?");
				        	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
				        	builder.setTitle("Import")
				        	   .setView(overwrite)
							   .setCancelable(true)
							   .setPositiveButton("Import", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										(new ImportTask(activity, TripViewHolder.this, "Importing your files...", IMPORT_TASK_ID, overwrite.isChecked())).execute(uri);
									}
							    })
							   .setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
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
		final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		final String[] editTripItems = activity.getFlex().getStringArray(R.array.EDIT_TRIP_ITEMS);
		builder.setTitle(trip.dir.getName())
			   .setCancelable(true)
			   .setNegativeButton(activity.getFlex().getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .setItems(editTripItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
				    public void onClick(DialogInterface dialog, int item) {
				    	final String selection = editTripItems[item].toString();
				    	if (selection == editTripItems[0]) { //Email Trip
				    		getParent().emailTrip(trip);
				    	}
				    	else if (selection == editTripItems[1]) //Edit Trip
				    		TripViewHolder.this.tripMenu(trip); 
				    	else if (selection == editTripItems[2]) //Delete Trip
				    		TripViewHolder.this.deleteTrip(trip);
				    	dialog.cancel();
				    }
				})
				.show();
    	return true;
    }
	
	public final void deleteTrip(final TripRow trip) {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		builder.setTitle(activity.getFlex().getString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON_TITLE_START) + trip.dir.getName() + activity.getFlex().getString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON_TITLE_END))
			   .setCancelable(true)
			   .setPositiveButton(activity.getFlex().getString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
		           public void onClick(DialogInterface dialog, int id) {
		                activity.getDB().deleteTripParallel(trip);
		           }
		       })
		       .setNegativeButton(activity.getFlex().getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
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
    			Toast.makeText(activity, "Successfully imported all files", Toast.LENGTH_LONG).show();
    		}
    		else {
    			Toast.makeText(activity, activity.getFlex().getString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
    		}
    		activity.getDB().getTripsParallel();
    	}
    }
	
	public void setParent(HomeHolder parent) {
    	this.parent = parent;
    }
    
    public HomeHolder getParent() {
    	return this.parent;
    }

	@Override
	public void onTripRowsQuerySuccess(TripRow[] trips) {
		tripAdapter.notifyDataSetChanged(trips);
		if (mIsFirstPass) { //Pre-Cache the receipts for the top two trips
			if (trips.length > 0) {
				activity.getDB().getReceiptsParallel(trips[0]);
			}
			if (trips.length > 1) {
				activity.getDB().getReceiptsParallel(trips[1]);
			}
			mIsFirstPass = false;
		}
	}

	@Override
	public void onTripRowInsertSuccess(TripRow trip) {
		activity.getDB().getTripsParallel();
		getParent().viewTrip(trip);
	}

	@Override
	public void onTripRowInsertFailure(SQLException ex, File directory) {
		if (ex != null)
			Toast.makeText(activity, "Error: An expense report with that name already exists", Toast.LENGTH_SHORT).show();
		else {
			Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
			activity.getStorageManager().delete(directory);
		}
		
	}

	@Override
	public void onTripRowUpdateSuccess(TripRow trip) {
		activity.getDB().getTripsParallel();
		getParent().viewTrip(trip);
	}

	@Override
	public void onTripRowUpdateFailure(TripRow oldTrip, File directory) {
		Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
		activity.getStorageManager().rename(directory, oldTrip.dir.getName());
	}

	@Override
	public void onTripDeleteSuccess(TripRow oldTrip) {
		if (!activity.getStorageManager().deleteRecursively(oldTrip.dir))
    		Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
		activity.getDB().getTripsParallel();
	}

	@Override
	public void onTripDeleteFailure() {
		Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
	}

}