package co.smartreceipts.android.rating;


import android.view.View;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.widget.Tooltip;

public class RatingTooltipPresenter {

    private final Tooltip mTooltip;

    public RatingTooltipPresenter(Tooltip tooltip) {
        mTooltip = Preconditions.checkNotNull(tooltip);

        mTooltip.setQuestion(R.string.rating_tooltip_text, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //// TODO: 01.03.2017 NO flow
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //// TODO: 01.03.2017 YES flow
            }
        });
    }
}
