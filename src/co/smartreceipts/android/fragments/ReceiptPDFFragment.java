package co.smartreceipts.android.fragments;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ReceiptPDFFragment extends WBFragment {
	
	public static final String TAG = "ReceiptPDFFragment";
	
	private MuPDFReaderView mReaderView;
	private MuPDFCore mCore;
	private MuPDFPageAdapter mAdapter;

	public static ReceiptPDFFragment newInstance() {
		return new ReceiptPDFFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		final Uri uri = getSherlockActivity().getIntent().getData();
		if (uri != null) {
			//TODO: Off main thread - follow steps in example
			try {
				mCore = new MuPDFCore(getSherlockActivity(), uri.getPath());
				mAdapter = new MuPDFPageAdapter(getSherlockActivity(), mCore);
			} catch (Exception e) {
				if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
				Toast.makeText(getSherlockActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
			}
		}
		else {
			Toast.makeText(getSherlockActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mReaderView = new MuPDFReaderView(getSherlockActivity()); //Can't inflate b/c this takes activity... rewrite this class?
		mReaderView.setAdapter(mAdapter);
		return mReaderView;
	}
}
