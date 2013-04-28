package com.wb.navigation;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public abstract class WBActivity extends SherlockActivity {
	
	static final boolean D = true;
	private static final String TAG = "WBActivity";
	
	private ViewController controller;
	
	protected abstract ViewController buildController();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "onCreate");
		this.controller = buildController();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "onStart");
		if (this.controller != null) controller.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (D) Log.d(TAG, "onResume");
		if (this.controller != null) controller.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (D) Log.d(TAG, "onPause");
		if (this.controller != null) controller.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (D) Log.d(TAG, "onStop");
		if (this.controller != null) controller.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (D) Log.d(TAG, "onDestroy");
		if (this.controller != null) controller.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if (D) Log.d(TAG, "onBackPressed");
		if (this.controller != null) {
			if (controller.onBackPressed())
				super.onBackPressed();
		}
		else {
			super.onBackPressed();
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.controller == null) return false; 
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				return controller.onDownMotionEvent(x, y);
			case MotionEvent.ACTION_MOVE:
				return controller.onMoveMotionEvent(x, y);
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				return controller.onUpMotionEvent(x, y);
			default:
				return false;
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		if (this.controller != null)
			return controller.onCreateOptionsMenu(menu);
		else
			return false;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (this.controller != null)
			return controller.onOptionsItemSelected(item);
		else
			return false;
	}
	
	@Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (this.controller != null)
			controller.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (this.controller != null)
			controller.onSaveInstanceState(outState);
	}

}