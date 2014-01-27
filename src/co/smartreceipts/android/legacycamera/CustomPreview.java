package co.smartreceipts.android.legacycamera;

import wb.android.camera.CameraPreview;
import co.smartreceipts.android.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class CustomPreview extends CameraPreview {
	
	private boolean _isPictureTaken;
	private Paint _bPaint;
	private Bitmap _greenCheck, _redX;
	private int _width, _height;
	private MyCameraActivity _cameraActivity;
	private int _checkLeft, _checkTop, _xLeft, _xTop;
	
	public CustomPreview(MyCameraActivity cameraActivity, int widthPixels, int heightPixels) {
		super(cameraActivity);
		_cameraActivity = cameraActivity;
		_isPictureTaken = false;
		_bPaint = new Paint(Paint.ANTI_ALIAS_FLAG); 
		_width = widthPixels; _height = heightPixels;
		_greenCheck = BitmapFactory.decodeResource(getResources(), R.drawable.greencheck);
		_redX = BitmapFactory.decodeResource(getResources(), R.drawable.redx);
		_checkLeft = _greenCheck.getWidth()/2;
		_checkTop = _height-2*_greenCheck.getHeight();
		_xLeft = _width - 3*_redX.getWidth()/2;
		_xTop = _height-2*_redX.getHeight();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (_isPictureTaken) {
			canvas.drawBitmap(_greenCheck, _checkLeft, _checkTop, _bPaint);
			canvas.drawBitmap(_redX, _xLeft, _xTop, _bPaint);
		}
	}
	
	public final boolean onTouchEvent(MotionEvent event) {
		if (_isPictureTaken) {
			final int checkRadiusSquared = _greenCheck.getHeight()*8;
			final int xRadiusSquared = _redX.getHeight()*8;
			final int checkDx = _checkLeft + _greenCheck.getWidth()/2 - ((int)event.getX());
			final int checkDy = _checkTop + _greenCheck.getHeight()/2 - ((int)event.getY());
			final int xDx = _xLeft + _redX.getWidth()/2 - ((int)event.getX());
			final int xDy = _xTop + _redX.getHeight()/2 - ((int)event.getY());
			if (checkRadiusSquared > checkDx*checkDx - checkDy*checkDy) {
				_isPictureTaken = false;
				invalidate();
				_cameraActivity.checkCallback();
				return true;
			}
			else if (xRadiusSquared > xDx*xDx - xDy*xDy) {
				_isPictureTaken = false;
				invalidate();
				_cameraActivity.xCallback();
				return true;
			}
		}
		return false;
	}
	
	public void setPictureTakenCallback(boolean isPictureTaken) {
		_isPictureTaken = isPictureTaken;
		this.invalidate();
	}

}
