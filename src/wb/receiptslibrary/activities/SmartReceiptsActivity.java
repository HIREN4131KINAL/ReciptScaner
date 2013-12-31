package wb.receiptslibrary.activities;

import java.io.File;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.util.AppRating;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.R;
import wb.receiptslibrary.fragments.ReceiptsFragment;
import wb.receiptslibrary.fragments.TripFragment;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.Preferences;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SmartReceiptsActivity extends WBActivity implements Sendable  {
    
	//logging variables
    static final String TAG = "SmartReceiptsActivity";
    
    //Camera Request Extras
    public static final String STRING_DATA = "strData";
    public static final int DIR = 0;
    public static final int NAME = 1;
    
    //Action Send Extras
    public static final String ACTION_SEND_URI = "actionSendUri";
    
    //Receiver Settings
    protected static final String FILTER_ACTION = "wb.receiptslibrary";
    
    //AppRating
    private static final int LAUNCHES_UNTIL_PROMPT = 20;
    
    //Instace Vars
    private Uri mActionSendUri;
    private boolean mIsDualPane;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mIsDualPane = getResources().getBoolean(R.bool.isTablet);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
 			if (BuildConfig.DEBUG) Log.d(TAG, "Creating new layout");
 			displayTripsLayout();
        }
        if (wasCalledFromSendAction()) {
        	final Preferences preferences = getSmartReceiptsApplication().getPersistenceManager().getPreferences();
    		if (preferences.showActionSendHelpDialog()) {
	        	BetterDialogBuilder builder = new BetterDialogBuilder(this);
	        	builder.setTitle("Add Picture to Receipt")
	        		   .setMessage("Tap on an existing receipt to add this image to it.")
	        		   .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
	    					@Override
	    					public void onClick(DialogInterface dialog, int which) {
	    						dialog.cancel();
	    					}
	        		   })
	        		   .setNegativeButton("Don't Show Again", new DialogInterface.OnClickListener() {
	    					@Override
	    					public void onClick(DialogInterface dialog, int which) {
	    						preferences.setShowActionSendHelpDialog(false);
	    						preferences.commit();
	    						dialog.cancel();
	    					}
	        		   })
	        		   .show();
    		}
        }
        else {
        	AppRating.onLaunch(this, LAUNCHES_UNTIL_PROMPT, "Smart Receipts", getPackageName());
        }
    }
    
    private void displayTripsLayout() {
		if (getIntent().hasExtra(TripRow.PARCEL_KEY)) { //We already have a feed we're looking to use
			final TripRow trip = (TripRow) getIntent().getParcelableExtra(TripRow.PARCEL_KEY);
			if (mIsDualPane) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, TripFragment.newInstance(), TripFragment.TAG).commit();
				getSupportFragmentManager().beginTransaction().replace(R.id.content_details, ReceiptsFragment.newInstance(trip), ReceiptsFragment.TAG).commit();
			}
			else {
				final Intent intent = new Intent(this, ReceiptsActivity.class);
				intent.putExtra(TripRow.PARCEL_KEY, trip);
				intent.putExtra(ACTION_SEND_URI, mActionSendUri);
				startActivity(intent);
			}
 		}
		else {
			getSupportFragmentManager().beginTransaction().replace(R.id.content_list, TripFragment.newInstance(), TripFragment.TAG).commit();
		}	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (!getSmartReceiptsApplication().getPersistenceManager().getStorageManager().isExternal())
    		Toast.makeText(SmartReceiptsActivity.this, getSmartReceiptsApplication().getFlex().getString(R.string.SD_WARNING), Toast.LENGTH_LONG).show();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_main_settings) {
	    	getSmartReceiptsApplication().getSettings().showSettingsMenu();
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_about) {
            getSmartReceiptsApplication().getSettings().showAbout();
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_categories) {
	    	getSmartReceiptsApplication().getSettings().showCategoriesMenu();
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_csv) {
	    	getSmartReceiptsApplication().getSettings().showCustomCSVMenu();
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_export) {
	    	getSmartReceiptsApplication().getSettings().showExport();
            return true;
	    }/*
		else if (item.getItemId() ==  R.id.menu_main_settings) {
			final Intent intent = new Intent(this, SettingsActivity.class);;
			startActivity(intent);
			return true;
		}*/
		else {
			return super.onOptionsItemSelected(item);
		}
	}
    
    @Override
    public boolean wasCalledFromSendAction() {
    	//Duplicated code with ReceiptsActivity. Fix by creating superclass
    	
    	if (this.getIntent() != null && this.getIntent().getAction() != null && this.getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
        	if (this.getIntent().getExtras() != null) {
        		if (mActionSendUri == null) {
        			Cursor cursor = null;
        			try {
		    	        final Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
		    	        cursor = getContentResolver().query(uri, null, null, null, null); 
		    	        if (cursor == null) { // local file path (i.e. Dropbox)
		    	        	mActionSendUri = Uri.parse(uri.toString());
		    	        } 
		    	        else { 
		    	            cursor.moveToFirst(); 
		    	            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
		    	            mActionSendUri = Uri.fromFile(new File(cursor.getString(idx)));
		    	        }
        			}
        			finally {
        				if (cursor != null) cursor.close();
        			}
        		}
    	        return true;
        	}
        	else {
        		Toast.makeText(SmartReceiptsActivity.this, getSmartReceiptsApplication().getFlex().getString(R.string.IMG_SEND_ERROR), Toast.LENGTH_LONG).show();
	        	return false;
        	}
        }
    	else {
    		return false;
    	}
    }
    
    @Override
    public Uri actionSendUri() {
    	return mActionSendUri;
    }
    
    @Override
    public String getTag() {
    	return TAG;
    }
	   
}