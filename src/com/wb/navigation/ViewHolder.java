package com.wb.navigation;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ViewHolder<T extends WBActivity> {
	
	private static final boolean D = WBActivity.D;
	private static final String TAG = "ViewHolder";
	
	public static final int NO_CONTENT_ID = -1;
	
	protected final T activity;
	protected final int contentID;
	
	private ViewHolder<? extends WBActivity> parent;
	
	public ViewHolder(T activity) {
		this.activity = activity;
		this.contentID = NO_CONTENT_ID;
	}
	
	public ViewHolder(T activity, int contentID) {
		this.activity = activity;
		this.contentID = contentID;
	}
	
	//This is called when popView returns the context back to this view (Recreates by default)
	public void onFocus() {
		onCreate();
	}
	
	public void onCreate() {
		
	}
	
	public void onStart() {

	}
	
	
	public void onResume() {

	}
	
	public void onPause() {

	}
	
	
	public void onStop() {

	}
	
	
	public void onDestroy() {

	}
	
	
	public void onBackPressed() {

	}
	
	
	public boolean onDownMotionEvent(float x, float y) {
		return false;
	}
	
	public boolean onMoveMotionEvent(float x, float y) {
		return false;
	}
	
	public boolean onUpMotionEvent(float x, float y) {
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
	
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		
	}

	public ViewHolder<? extends WBActivity> getParent() {
		return parent;
	}

	public void setParent(ViewHolder<? extends WBActivity> parent) {
		this.parent = parent;
	}
	
	public final T getActivity() {
		return activity;
	}
	
    public void onSaveInstanceState(Bundle outState) {
    	
    }
	
}