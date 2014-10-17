package co.smartreceipts.android.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;

import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

public class ReceiptPDFFragment extends WBFragment implements FilePicker.FilePickerSupport {

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
		final Uri uri = getActivity().getIntent().getData();
		if (uri != null) {
			//TODO: Off main thread - follow steps in example (might be okay actually, looks to be async behind the scenes)
			try {
				mCore = new MuPDFCore(getActivity(), uri.getPath());
				mAdapter = new MuPDFPageAdapter(getActivity(), this, mCore);
			} catch (Exception e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, e.toString());
				}
				Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
			}
		}
		else {
			Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			mReaderView = new MuPDFReaderView(getActivity()); //Can't inflate b/c this takes activity... rewrite this class?
			mReaderView.setAdapter(mAdapter);
			return mReaderView;
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, e.toString());
			}
			Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
			return new View(getActivity());
		}
	}

	@Override
	public void performPickFor(FilePicker picker) {
		// Stub method (useful only if we're making a PDF chooser activity)
	}
}
