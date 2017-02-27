package co.smartreceipts.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
        mCloseIcon = (ImageView) findViewById(R.id.close_icon);
        mErrorIcon = (ImageView) findViewById(R.id.error_icon);
    }

    public void showError(String errorMessage) {
        mErrorIcon.setVisibility(VISIBLE);
        mMessageText.setVisibility(VISIBLE);
        mCloseIcon.setVisibility(VISIBLE);

        mMessageText.setText(errorMessage);
    }

    public void showInfo(String infoMessage) {
        mCloseIcon.setVisibility(VISIBLE);
        mMessageText.setVisibility(VISIBLE);

        mMessageText.setText(infoMessage);
    }

    public void showQuestion(String questionText, OnClickListener noClickListener, OnClickListener yesClickListener) {
        mMessageText.setVisibility(VISIBLE);
        mButtonNo.setVisibility(VISIBLE);
        mButtonYes.setVisibility(VISIBLE);

        mMessageText.setText(questionText);
        mButtonNo.setOnClickListener(noClickListener);
        mButtonYes.setOnClickListener(yesClickListener);
    }

}
