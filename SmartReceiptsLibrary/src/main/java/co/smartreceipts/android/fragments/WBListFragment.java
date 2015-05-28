package co.smartreceipts.android.fragments;

import wb.android.flex.Flex;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.date.DateManager;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.workers.WorkerManager;

public class WBListFragment extends ListFragment {

	private SmartReceiptsApplication mApplication;
	private DateManager mDateManager;

	private boolean mDisableScreenLoggingOnStart = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = getSmartReceiptsApplication();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!mDisableScreenLoggingOnStart) {
			getWorkerManager().getLogger().logScreen(this);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mApplication = null;
	}

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParentFragment() != null) {
            getParentFragment().startActivityForResult(intent, requestCode);
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    protected Flex getFlex() {
		return getSmartReceiptsApplication().getFlex();
	}

	protected String getFlexString(int id) {
		if (isAdded()) {
			return getSmartReceiptsApplication().getFlex().getString(getActivity(), id);
		}
		else {
			return "";
		}
	}

	protected DateManager getDateManager() {
		if (mDateManager == null) {
			mDateManager = new DateManager(getActivity(), getSmartReceiptsApplication().getPersistenceManager().getPreferences());
		}
		return mDateManager;
	}

	protected PersistenceManager getPersistenceManager() {
		return getSmartReceiptsApplication().getPersistenceManager();
	}

	protected WorkerManager getWorkerManager() {
		return getSmartReceiptsApplication().getWorkerManager();
	}

	public ActionBar getSupportActionBar() {
		return ((AppCompatActivity)getActivity()).getSupportActionBar();
	}

	public SmartReceiptsApplication getSmartReceiptsApplication() {
		if (mApplication == null) {
			if (getActivity() != null) {
				final Application application = getActivity().getApplication();
				if (application instanceof SmartReceiptsApplication) {
					mApplication = (SmartReceiptsApplication) application;
				}
				else {
					throw new RuntimeException("The Application must be an instance a SmartReceiptsApplication");
				}
			}
			else {
				// We're in a weird state and probably will crash soon... But just in case
				mApplication = SmartReceiptsApplication.getInstance();
			}
		}
		return mApplication;
	}

	public void disableScreenLoggingOnStart() {
		mDisableScreenLoggingOnStart = true;
	}

}