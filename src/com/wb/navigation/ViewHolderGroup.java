package com.wb.navigation;

import java.util.Stack;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ViewHolderGroup<T extends WBActivity> extends ViewHolder<T> {
	
	private static final boolean D = WBActivity.D;
	private static final String TAG = "ViewHolderGroup";
	
	private ViewHolder<T> currentView;
	private final Stack<ViewHolder<T>> views;

	public ViewHolderGroup(T activity) {
		super(activity);
		views = new Stack<ViewHolder<T>>();
	}
	
	public ViewHolderGroup(T activity, int contentID) {
		super(activity, contentID);
		views = new Stack<ViewHolder<T>>();
	}
	
	public void pushChild(ViewHolder<T> view) {
		currentView = view;
		currentView.setParent(this);
		views.push(view);
		currentView.onCreate();
	}
	
	public void pushChildButDontRender(ViewHolder<T> view) {
		currentView = view;
		currentView.setParent(this);
		views.push(view);
	}
	
	public ViewHolder<T> popChild() { 
		if (!views.isEmpty()) {
			ViewHolder<T> pop = views.pop();
			if (!views.isEmpty()) {
				currentView = views.peek();
				currentView.onFocus();
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
	
	@Override
	public void onFocus() {
		if (currentView != null) currentView.onFocus();
	}
	
	public void onStart() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onStart();
		}
	}
	
	public void onResume() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onResume();
		}
	}
	
	
	public void onPause() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onPause();
		}
	}
	
	
	public void onStop() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onStop();
		}
	}
	
	
	public void onDestroy() {
		final int size = views.size();
		for (int i=0; i<size; i++) {
			views.get(i).onDestroy();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (currentView != null) currentView.onBackPressed();
		popChild();
	}
	
	@Override
	public boolean onDownMotionEvent(float x, float y) {
		if (currentView != null)
			return currentView.onDownMotionEvent(x, y);
		else
			return false;
	}
	
	@Override
	public boolean onMoveMotionEvent(float x, float y) {
		if (currentView != null)
			return currentView.onMoveMotionEvent(x, y);
		else
			return false;
	}

	@Override
	public boolean onUpMotionEvent(float x, float y) {
		if (currentView != null)
			return currentView.onUpMotionEvent(x, y);
		else
			return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (currentView != null)
			return currentView.onCreateOptionsMenu(menu);
		else
			return false;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (currentView != null)
			return currentView.onOptionsItemSelected(item);
		else
			return false;
    }

	@Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	if (currentView != null)
    		currentView.onActivityResult(requestCode, resultCode, data);
    }
	
	int getViewCount() {
		if (views == null || currentView == null)
			return -1;
		else
			return views.size();
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
    	
    }

}