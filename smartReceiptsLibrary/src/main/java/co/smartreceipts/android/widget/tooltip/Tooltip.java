package co.smartreceipts.android.widget.tooltip;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.smartreceipts.android.R;
import wb.android.util.Utils;

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

    public void showErrorWithoutIcon(String errorMessage) {
        setErrorBackground();

        mMessageText.setVisibility(VISIBLE);
        mCloseIcon.setVisibility(VISIBLE);

        mButtonNo.setVisibility(GONE);
        mButtonYes.setVisibility(GONE);
        mErrorIcon.setVisibility(GONE);

        mMessageText.setText(errorMessage);
    }

    public void showError(String errorMessage) {
        setErrorBackground();

        mMessageText.setVisibility(VISIBLE);
        mCloseIcon.setVisibility(VISIBLE);
        mErrorIcon.setVisibility(VISIBLE);

        mButtonNo.setVisibility(GONE);
        mButtonYes.setVisibility(GONE);

        mMessageText.setText(errorMessage);
    }

    public void setInfo(int stringId, OnClickListener tooltipClickListener, OnClickListener closeClickListener) {
        setInfoBackground();

//        if (Utils.ApiHelper.hasLollipop()) {
//            TransitionManager.beginDelayedTransition(this, new Slide(Gravity.TOP));
//        }

        mMessageText.setVisibility(VISIBLE);
        mCloseIcon.setVisibility(VISIBLE);

        mErrorIcon.setVisibility(GONE);
        mButtonNo.setVisibility(GONE);
        mButtonYes.setVisibility(GONE);

        mMessageText.setText(getContext().getText(stringId));
        setOnClickListener(tooltipClickListener);
        mCloseIcon.setOnClickListener(closeClickListener);

    }

    public void showQuestion(String questionText, OnClickListener noClickListener, OnClickListener yesClickListener) {
        setInfoBackground();

        mMessageText.setVisibility(VISIBLE);
        mButtonNo.setVisibility(VISIBLE);
        mButtonYes.setVisibility(VISIBLE);

        mCloseIcon.setVisibility(GONE);
        mErrorIcon.setVisibility(GONE);

        mMessageText.setText(questionText);
        mButtonNo.setOnClickListener(noClickListener);
        mButtonYes.setOnClickListener(yesClickListener);
    }

    private void setErrorBackground() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.smart_receipts_colorError));
    }

    private void setInfoBackground() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.smart_receipts_colorAccent));
    }




}
