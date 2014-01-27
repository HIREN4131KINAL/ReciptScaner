package co.smartreceipts.android.activities;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.ui.UpNavigationSlidingPaneLayout;
import wb.android.util.AppRating;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.ReceiptsFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.Preferences;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SmartReceiptsActivity extends WBActivity implements Navigable, Attachable {
    
	//logging variables
    static final String TAG = "SmartReceiptsActivity";
    
    //Camera Request Extras
    public static final String STRING_DATA = "strData";
    public static final int DIR = 0;
    public static final int NAME = 1;
    
    //Action Send Extras
    public static final String ACTION_SEND_URI = "actionSendUri";
    
    //Receiver Settings
    protected static final String FILTER_ACTION = "co.smartreceipts.android";
    
    //AppRating
    private static final int LAUNCHES_UNTIL_PROMPT = 25;
    
    //Instace Vars
	private UpNavigationSlidingPaneLayout mSlidingPaneLayout;
    private boolean mIsDualPane;
    private Attachment mAttachment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
    	setContentView(R.layout.activity_main);
    	
    	// savedInstanceState is non-null when there is fragment state saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape). In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it. Since the SlidingPaneLayout is controlled at the activity
    	// layer, however, as opposed to the fragment layer, it will need to be recreated regardless of what the current fragment
    	// state is
    	// TODO: Test with not reinflating fragments
    	mIsDualPane = getResources().getBoolean(R.bool.isTablet);
        if (!mIsDualPane) {
	        mSlidingPaneLayout = (UpNavigationSlidingPaneLayout) findViewById(R.id.slidingpanelayout);
	        mSlidingPaneLayout.setPanelSlideListener(this);
	        mSlidingPaneLayout.openPane();
        }
    	
		if (savedInstanceState == null) {
			displayTripsLayout();
	        AppRating.onLaunch(this, LAUNCHES_UNTIL_PROMPT, "Smart Receipts", getPackageName());
		}

    }
    
    private void displayTripsLayout() {
    	if (BuildConfig.DEBUG) Log.d(TAG, "displayTripsLayout");
    	getSupportFragmentManager().beginTransaction().replace(R.id.content_list, TripFragment.newInstance(), TripFragment.TAG).commit();
		if (getIntent().hasExtra(TripRow.PARCEL_KEY)) { //We already have a feed we're looking to use
			final TripRow trip = (TripRow) getIntent().getParcelableExtra(TripRow.PARCEL_KEY);
			viewReceipts(trip);
 		}	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (BuildConfig.DEBUG) Log.d(TAG, "onStart");
    	if (!getSmartReceiptsApplication().getPersistenceManager().getStorageManager().isExternal())
    		Toast.makeText(SmartReceiptsActivity.this, getSmartReceiptsApplication().getFlex().getString(this, R.string.SD_WARNING), Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
    	// Present dialog for viewing an attachment
    	final Attachment attachment = new Attachment(getIntent(), getContentResolver());
    	setAttachment(attachment);
    	if (attachment.isValid() && attachment.isDirectlyAttachable()) { 
        	final Preferences preferences = getSmartReceiptsApplication().getPersistenceManager().getPreferences();
        	final int stringId = attachment.isPDF() ? R.string.pdf : R.string.image;
    		if (preferences.showActionSendHelpDialog()) {
	        	BetterDialogBuilder builder = new BetterDialogBuilder(this);
	        	builder.setTitle(getString(R.string.dialog_attachment_title, getString(stringId)))
	        		   .setMessage(getString(R.string.dialog_attachment_text, getString(stringId)))
	        		   .setPositiveButton(R.string.dialog_attachment_positive, new DialogInterface.OnClickListener() {
	    					@Override
	    					public void onClick(DialogInterface dialog, int which) {
	    						dialog.cancel();
	    					}
	        		   })
	        		   .setNegativeButton(R.string.dialog_attachment_negative, new DialogInterface.OnClickListener() {
	    					@Override
	    					public void onClick(DialogInterface dialog, int which) {
	    						preferences.setShowActionSendHelpDialog(false);
	    						preferences.commit();
	    						dialog.cancel();
	    					}
	        		   })
	        		   .show();
    		}
    		else {
    			Toast.makeText(this, getString(R.string.dialog_attachment_text, getString(stringId)), Toast.LENGTH_LONG).show();
    		}
    	}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mIsDualPane) {
			getSupportMenuInflater().inflate(R.menu.menu_trip, menu);
		}
		else {
			if (mSlidingPaneLayout.isOpen()) {
				getSupportMenuInflater().inflate(R.menu.menu_trip, menu);
			}
			else {
				getSupportMenuInflater().inflate(R.menu.menu_main, menu);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (!mIsDualPane) { // Home should never be enabled for tablets anyway but just in case
				mSlidingPaneLayout.openPane();
			}
			return true;
		}
		else if (item.getItemId() == R.id.menu_main_settings) {
	    	getSmartReceiptsApplication().getSettings().showSettingsMenu(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_about) {
            getSmartReceiptsApplication().getSettings().showAbout(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_categories) {
	    	getSmartReceiptsApplication().getSettings().showCategoriesMenu(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_csv) {
	    	getSmartReceiptsApplication().getSettings().showCustomCSVMenu();
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_pdf) {
	    	getSmartReceiptsApplication().getSettings().showCustomPDFMenu();
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
    public String getTag() {
    	return TAG;
    }

	@Override
	public void onPanelSlide(View panel, float slideOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPanelOpened(View panel) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onPanelOpened");
		if (!mIsDualPane) {
			enableUpNavigation(false);
			getSupportActionBar().setTitle(getSmartReceiptsApplication().getFlex().getString(this, R.string.app_name));
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
			if (fragment != null) {
				getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			}
			supportInvalidateOptionsMenu();
		}
	}

	@Override
	public void onPanelClosed(View panel) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onPanelClosed");
		if (!mIsDualPane) {
			enableUpNavigation(true);
			supportInvalidateOptionsMenu();
		}
	}

	@Override
	public void viewReceipts(TripRow trip) {
		getSupportFragmentManager().beginTransaction().replace(R.id.content_details, ReceiptsFragment.newInstance(trip), ReceiptsFragment.TAG).commitAllowingStateLoss();
		if (!mIsDualPane) {
			enableUpNavigation(true);
			mSlidingPaneLayout.closePane();
		}
	}

	@Override
	public void viewTrips() {
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
		if (fragment != null) {
			getSupportFragmentManager().beginTransaction().remove(fragment).commit();
		}
		if (!mIsDualPane) {
			mSlidingPaneLayout.openPane();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (!mIsDualPane) {
			if (!mSlidingPaneLayout.isOpen()) {
				mSlidingPaneLayout.openPane();
			}
			else {
				super.onBackPressed();
			}
		}
		else {
			super.onBackPressed();
		}
	}
	
	/**
	 * Returns the attachment that is generated via the main activity
	 * @return
	 */
	public Attachment getAttachment() {
		return mAttachment;
	}
	
	/**
	 * Stores the main attachment details for later
	 */
	public void setAttachment(Attachment attachment) {
		mAttachment = attachment;
	}
	   
}