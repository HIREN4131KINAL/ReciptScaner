package com.wb.navigation;

import java.util.Stack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ViewController {

	private static final boolean D = WBActivity.D;
	private static final String TAG = "ViewController";
	
	private WBActivity activity;
	private ViewHolder<? extends WBActivity> currentView;
	private Stack<ViewHolder<? extends WBActivity>> views, tempViews;
	
	public ViewController(WBActivity activity) {
		this.activity = activity;
		views = new Stack<ViewHolder<? extends WBActivity>>();
		tempViews = new Stack<ViewHolder<? extends WBActivity>>();
	}
	
	public void pushView(ViewHolder<? extends WBActivity> view) {
		currentView = view;
		views.push(view);
		tempViews.clear();
		currentView.onCreate();
		activity.invalidateOptionsMenu();
	}
	
	public void pushViewButDontRender(ViewHolder<? extends WBActivity> view) {
		currentView = view;
		views.push(view);
		tempViews.clear();
	}
	
	public void pushTemporaryView(ViewHolder<? extends WBActivity> view) {
		currentView = view;
		tempViews.push(view);
		currentView.onCreate();
		activity.invalidateOptionsMenu();
	}
	
	public ViewHolder<? extends WBActivity> popView() {
		if (D) Log.d(TAG, "Popping View"); 
		if (!tempViews.isEmpty()) {
			ViewHolder<? extends WBActivity> pop = tempViews.pop();
			if (!tempViews.isEmpty()) {
				currentView = tempViews.peek();
				currentView.onFocus();
				activity.invalidateOptionsMenu();
			}
			else {
				currentView = null;
			}
			return pop;
		}
		else if (!views.isEmpty()) {
			ViewHolder<? extends WBActivity> pop = views.pop();
			if (!views.isEmpty()) {
				currentView = views.peek();
				currentView.onFocus();
				activity.invalidateOptionsMenu();
			}
			else {
				currentView = null;
			}
			return pop;
		}
		else {
			currentView = null;
			return null;
		}
	}
	
	public void onStart() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onStart();
		}
		final int tSize = tempViews.size();
		for (int i=0; i<tSize; i++) {
			tempViews.get(i).onStart();
		}
	}
	
	
	public void onResume() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onResume();
		}
		final int tSize = tempViews.size();
		for (int i=0; i<tSize; i++) {
			tempViews.get(i).onResume();
		}
	}
	
	
	public void onPause() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onPause();
		}
		final int tSize = tempViews.size();
		for (int i=0; i<tSize; i++) {
			tempViews.get(i).onPause();
		}
	}
	
	
	public void onStop() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onStop();
		}
		final int tSize = tempViews.size();
		for (int i=0; i<tSize; i++) {
			tempViews.get(i).onStop();
		}
	}
	
	
	public void onDestroy() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onDestroy();
		}
		final int tSize = tempViews.size();
		for (int i=0; i<tSize; i++) {
			tempViews.get(i).onDestroy();
		}
	}	
	
	public boolean onBackPressed() {
		if (currentView != null) currentView.onBackPressed();
		if (currentView instanceof ViewHolderGroup) {
			ViewHolderGroup<? extends WBActivity> holderGroup = (ViewHolderGroup<? extends WBActivity>) currentView;
			if (holderGroup.getViewCount() < 1)
				popView();
		}
		else {
			popView();
		}
		return (currentView == null);
	}
	
	
	public boolean onDownMotionEvent(float x, float y) {
		if (currentView != null)
			return currentView.onDownMotionEvent(x, y);
		else
			return false;
	}
	
	public boolean onMoveMotionEvent(float x, float y) {
		if (currentView != null)
			return currentView.onMoveMotionEvent(x, y);
		else
			return false;
	}
	
	public boolean onUpMotionEvent(float x, float y) {
		if (currentView != null)
			return currentView.onUpMotionEvent(x, y);
		else
			return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		if (currentView != null)
			return currentView.onCreateOptionsMenu(menu);
		else
			return false;
	}
	
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (currentView != null)
			return currentView.onOptionsItemSelected(item);
		else
			return false;
    }
    
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	if (currentView != null)
    		currentView.onActivityResult(requestCode, resultCode, data);
    }
    
    public void onSaveInstanceState(Bundle outState) {
    	final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onSaveInstanceState(outState);
		}
    }
    
}