package wb.receiptslibrary.activities;

import android.net.Uri;

public interface Sendable {
	
	public boolean wasCalledFromSendAction();
    public Uri actionSendUri();

}
