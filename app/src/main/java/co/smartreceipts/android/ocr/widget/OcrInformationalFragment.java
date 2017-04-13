package co.smartreceipts.android.ocr.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;

public class OcrInformationalFragment extends Fragment {

    @Inject
    Analytics analytics;

    private OcrInformationalInteractor mInteractor;
    private OcrInformationalPresenter mPresenter;

    public static OcrInformationalFragment newInstance() {
        return new OcrInformationalFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        setHasOptionsMenu(true);

        mInteractor = new OcrInformationalInteractor(analytics, getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ocr_questionnaire, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new OcrInformationalPresenter(view, mInteractor);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ocr_questionnaire, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return mInteractor.dismissQuestionnaire();
        } else if (item.getItemId() == R.id.action_save) {
            return mInteractor.submitQuestionnaire();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            actionBar.setTitle(R.string.ocr_questionnaire);
            actionBar.setSubtitle("");
        }
        mPresenter.onResume();
    }

    @Override
    public void onPause() {
        mPresenter.onPause();
        Logger.debug(this, "onPause");
        super.onPause();
    }
}
