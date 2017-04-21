package co.smartreceipts.android.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;

import co.smartreceipts.android.R;

public class Tooltip extends RelativeLayout {

    private Button mButtonNo, mButtonYes;
    private TextView mMessageText;
    private ImageView mCloseIcon, mErrorIcon;

    public Tooltip(Context context) {
        super(context);
        init();
    }

    public Tooltip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Tooltip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tooltip, this);
        mMessageText = (TextView) findViewById(R.id.tooltip_message);
        mButtonNo = (Button) findViewById(R.id.tooltip_no);
        mButtonYes = (Button) findViewById(R.id.tooltip_yes);
        mCloseIcon = (ImageView) findViewById(R.id.tooltip_close_icon);
        mErrorIcon = (ImageView) findViewById(R.id.tooltip_error_icon);

        setVisibility(VISIBLE);
    }

    public void setError(@StringRes int messageStringId, @Nullable OnClickListener closeClickListener) {
        setViewStateError();
        mMessageText.setText(getContext().getText(messageStringId));
        showCloseIcon(closeClickListener);
    }

    public void setErrorWithoutClose(@StringRes int messageStringId, @Nullable OnClickListener tooltipClickListener) {
        setViewStateError();
        mCloseIcon.setVisibility(GONE);

        mMessageText.setText(getContext().getText(messageStringId));
        setTooltipClickListener(tooltipClickListener);
    }

    public void setInfo(@StringRes int infoStringId, @Nullable OnClickListener tooltipClickListener, @Nullable OnClickListener closeClickListener) {
        setInfoMessage(infoStringId);
        setTooltipClickListener(tooltipClickListener);
        showCloseIcon(closeClickListener);

        mErrorIcon.setVisibility(GONE);
        mButtonNo.setVisibility(GONE);
        mButtonYes.setVisibility(GONE);
    }

    public void setQuestion(@StringRes int questionStringId, @Nullable OnClickListener noClickListener, @Nullable OnClickListener yesClickListener) {
        setInfoMessage(questionStringId);

        mButtonNo.setVisibility(VISIBLE);
        mButtonYes.setVisibility(VISIBLE);

        mCloseIcon.setVisibility(GONE);
        mErrorIcon.setVisibility(GONE);

        mButtonNo.setOnClickListener(noClickListener);
        mButtonYes.setOnClickListener(yesClickListener);
    }

    public void setInfoMessage(@StringRes int messageStringId) {
        setInfoBackground();
        mMessageText.setText(messageStringId);
        mMessageText.setVisibility(VISIBLE);
    }

    public void setInfoMessage(@Nullable CharSequence text) {
        setInfoBackground();
        mMessageText.setText(text);
        mMessageText.setVisibility(VISIBLE);
    }
    
    public void setTooltipClickListener(@Nullable OnClickListener tooltipClickListener) {
        setOnClickListener(tooltipClickListener);
    }
    
    public void showCloseIcon(@Nullable OnClickListener closeClickListener) {
        mCloseIcon.setVisibility(VISIBLE);
        mCloseIcon.setOnClickListener(closeClickListener);
    }

    private void setErrorBackground() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.smart_receipts_colorError));
    }

    private void setInfoBackground() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.smart_receipts_colorAccent));
    }

    private void setViewStateError() {
        setErrorBackground();

        mMessageText.setVisibility(VISIBLE);
        mCloseIcon.setVisibility(VISIBLE);
        mErrorIcon.setVisibility(VISIBLE);

        mButtonNo.setVisibility(GONE);
        mButtonYes.setVisibility(GONE);
    }

    public void hideWithAnimation() {
        TransitionManager.beginDelayedTransition((ViewGroup) getParent(), new Slide(Gravity.TOP));
        setVisibility(GONE);
    }

    public void showWithAnimation() {
        TransitionManager.beginDelayedTransition((ViewGroup) getParent(), new Slide(Gravity.TOP));
        setVisibility(VISIBLE);
    }
}
