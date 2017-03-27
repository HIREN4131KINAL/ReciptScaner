package co.smartreceipts.android.ocr.info.tooltip;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.widget.Tooltip;
import dagger.android.support.AndroidSupportInjection;

public class OcrInformationalTooltipFragment extends Fragment {

    @Inject
    AnalyticsManager analyticsManager;

    private OcrInformationalTooltipInteractor mInteractor;
    private OcrInformationalTooltipPresenter mPresenter;


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInteractor = new OcrInformationalTooltipInteractor(getActivity(),
                new NavigationHandler(getActivity()), analyticsManager);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new Tooltip(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new OcrInformationalTooltipPresenter(mInteractor, (Tooltip) view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onPause() {
        mPresenter.onPause();
        super.onPause();
    }
}
