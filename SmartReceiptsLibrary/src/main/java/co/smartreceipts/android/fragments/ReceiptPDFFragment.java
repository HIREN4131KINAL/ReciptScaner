package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.model.Receipt;

public class ReceiptPDFFragment extends WBFragment implements FilePicker.FilePickerSupport {

    public static final String TAG = "ReceiptPDFFragment";

    private Receipt mReceipt;

    private MuPDFReaderView mReaderView;
    private Toolbar mToolbar;

    private MuPDFCore mCore;
    private MuPDFPageAdapter mAdapter;
    private NavigationHandler mNavigationHandler;

    public static ReceiptPDFFragment newInstance(@NonNull Receipt receipt) {
        final ReceiptPDFFragment fragment = new ReceiptPDFFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Receipt.PARCEL_KEY, receipt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        if (mReceipt.hasPDF()) {
            try {
                mCore = new MuPDFCore(getActivity(), mReceipt.getFilePath());
                mAdapter = new MuPDFPageAdapter(getActivity(), this, mCore);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
        }
        mNavigationHandler = new NavigationHandler(getActivity(), new DefaultFragmentProvider());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.receipt_pdf_view, container, false);
        final FrameLayout pdfFrame = (FrameLayout) rootView.findViewById(R.id.pdf_frame);
        if (mCore != null && mAdapter != null) {
            try {
                mReaderView = new MuPDFReaderView(getActivity()); //Can't inflate b/c this takes activity... rewrite this class?
                mReaderView.setAdapter(mAdapter);
                pdfFrame.addView(mReaderView);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mReceipt.getName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationHandler.navigateToReportInfoFragment(mReceipt.getTrip());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void performPickFor(FilePicker picker) {
        // Stub method (useful only if we're making a PDF chooser activity)
    }
}
