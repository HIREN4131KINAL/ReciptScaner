package co.smartreceipts.android.ocr.info.tooltip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;

public class OcrInformationalTooltipFragment extends Fragment {

    private OcrInformationalTooltipInteractor mInteractor;
    private OcrInformationalTooltipPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SmartReceiptsApplication application = (SmartReceiptsApplication) getActivity().getApplication();
        mInteractor = new OcrInformationalTooltipInteractor(getActivity(), getFragmentManager(), application.getAnalyticsManager());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.information_tooltip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new OcrInformationalTooltipPresenter(mInteractor, view);
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
