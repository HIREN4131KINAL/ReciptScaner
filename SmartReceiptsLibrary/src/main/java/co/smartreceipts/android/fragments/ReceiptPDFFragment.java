package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joanzapata.pdfview.PDFView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.model.Receipt;

public class ReceiptPDFFragment extends WBFragment {

    public static final String TAG = "ReceiptPDFFragment";

    private Receipt mReceipt;

    private Toolbar mToolbar;
    private PDFView mPDFView;

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
        mNavigationHandler = new NavigationHandler(getActivity(), new DefaultFragmentProvider());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.receipt_pdf_view, container, false);
        mPDFView = (PDFView) rootView.findViewById(R.id.pdf_frame);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mReceipt != null && mReceipt.getPDF() != null) {
            mPDFView.fromFile(mReceipt.getPDF()).load();
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_pdf_open_error), Toast.LENGTH_SHORT).show();
        }
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

}