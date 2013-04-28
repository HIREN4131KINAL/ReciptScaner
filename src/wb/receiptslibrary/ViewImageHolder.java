package wb.receiptslibrary;

public class ViewImageHolder {
	
	
	/*
	else if (item.getItemId() == RETAKE_PHOTO_ID) {
		String dirPath;
		try {
			if (_currentTrip.dir.exists())
				dirPath = _currentTrip.dir.getCanonicalPath();
			else
				dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
		} catch (IOException e) {
			Toast.makeText(SmartReceiptsActivity.activity, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
			return false;
		}
		if (_useNativeCamera) {
			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			_imageUri = Uri.fromFile(new File(dirPath, _highlightedReceipt.img.getName()));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
            startActivityForResult(intent, NATIVE_RETAKE_PHOTO_CAMERA_REQUEST);				
		}
		else {
    		final Intent intent = new Intent(activity, MyCameraActivity.class);
			String[] strings  = new String[] {dirPath, _highlightedReceipt.img.getName()};
			intent.putExtra(STRING_DATA, strings);
			activity.startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
		}
	}*/

}
