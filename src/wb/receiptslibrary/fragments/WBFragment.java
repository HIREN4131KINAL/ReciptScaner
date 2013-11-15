package wb.receiptslibrary.fragments;

import wb.android.flex.Flex;
import wb.receiptslibrary.Navigable;
import wb.receiptslibrary.R;
import wb.receiptslibrary.SmartReceiptsActivity;
import wb.receiptslibrary.date.DateManager;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.workers.WorkerManager;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class WBFragment extends SherlockFragment {

	private SmartReceiptsActivity mSmartReceiptsActivity;
	private Navigable mNavigable;
	private DateManager mDateManager;
	
	Flex getFlex() {
		return getSmartReceiptsActivity().getFlex();
	}
	
	String getFlexString(int id) {
		return getSmartReceiptsActivity().getFlex().getString(id);
	}
	
	DateManager getDateManager() {
		if (mDateManager == null) {
			mDateManager = new DateManager(getSmartReceiptsActivity());
		}
		return mDateManager;
	}
	
	PersistenceManager getPersistenceManager() {
		return getSmartReceiptsActivity().getPersistenceManager();
	}
	
	WorkerManager getWorkerManager() {
		return getSmartReceiptsActivity().getWorkerManager();
	}
	
	SmartReceiptsActivity getSmartReceiptsActivity() {
		if (mSmartReceiptsActivity == null) {
			if ((getSherlockActivity() instanceof SmartReceiptsActivity))
				mSmartReceiptsActivity = (SmartReceiptsActivity) getSherlockActivity();
			else
				throw new ClassCastException("This method requires that the Activity is an instance of SmartReceiptsActivity");
		}
		return mSmartReceiptsActivity;
	}
	
	Navigable getNavigator() {
		if (mNavigable == null) {
			SmartReceiptsActivity activity = getSmartReceiptsActivity();
			if ((activity instanceof Navigable))
				mNavigable = (Navigable) activity;
			else
				throw new ClassCastException("This method requires that SmartReceiptsActivity implements Navigable"); 
		}
		return mNavigable;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == R.id.menu_main_about) {
    		getNavigator().viewAbout();
			return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_settings) {
    		getNavigator().viewSettings();
    		return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_categories) {
    		getNavigator().viewCategories();
    		return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_csv) {
    		getSmartReceiptsActivity().showCustomCSVMenu();
    		return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_export) {
    		getNavigator().viewExport();
    		return true;
    	}
    	return false;
	}

}
