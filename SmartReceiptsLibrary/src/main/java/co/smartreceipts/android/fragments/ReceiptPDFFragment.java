package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;

public class ReceiptPDFFragment extends WBFragment implements FilePicker.FilePickerSupport {

    public static final String TAG = "ReceiptPDFFragment";

    private MuPDFReaderView mReaderView;
    private MuPDFCore mCore;
    private MuPDFPageAdapter mAdapter;
    private Receipt mReceipt;

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
        setHasOptionsMenu(true);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.receipt_pdf_view, container, false);
        final FrameLayout pdfFrame = (FrameLayout) rootView.findViewById(R.id.pdf_frame);
        try {
            mReaderView = new MuPDFReaderView(getActivity()); //Can't inflate b/c this takes activity... rewrite this class?
            mReaderView.setAdapter(mAdapter);
            pdfFrame.addView(mReaderView);
            return rootView;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
            return new View(getActivity());
        }
    }

    @Override
    public void performPickFor(FilePicker picker) {
        // Stub method (useful only if we're making a PDF chooser activity)
    }
}
