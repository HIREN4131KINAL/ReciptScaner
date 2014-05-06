package wb.android.ui;

import android.content.Context;
import android.util.AttributeSet;

public class SquareRecyclingImageView extends RecyclingImageView {
	
	public SquareRecyclingImageView(Context context) {
        super(context);
    }

    public SquareRecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }

}
