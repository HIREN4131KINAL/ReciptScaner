package co.smartreceipts.android.ocr.info.tooltip;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.Tooltip;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.*;

public class OcrInformationalTooltipPresenter implements OnClickListener {

    private final OcrInformationalTooltipInteractor mInteractor;

    private final Tooltip mTooltip;
    private CompositeSubscription mCompositeSubscription;

    public OcrInformationalTooltipPresenter(@NonNull OcrInformationalTooltipInteractor interactor, @NonNull Tooltip tooltip) {
        mInteractor = Preconditions.checkNotNull(interactor);

        mTooltip = Preconditions.checkNotNull(tooltip);
        mTooltip.setInfo(R.string.ocr_informational_tooltip_text, this, this);
        mTooltip.setVisibility(GONE);
    }

    public void onResume() {
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(mInteractor.getShowQuestionTooltipStream()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    Logger.info(OcrInformationalTooltipPresenter.this, "Showing OCR Tooltip");
                    mTooltip.setVisibility(VISIBLE);
                }
            }));
    }

    public void onPause() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = null;
    }

    @Override
    public void onClick(View view) {
        if (view == mTooltip) {
            mInteractor.showOcrInformation();
        } else if (view.getId() == R.id.tooltip_close_icon) {
            mInteractor.dismissTooltip();
        }
        mTooltip.setVisibility(GONE);
    }
}
