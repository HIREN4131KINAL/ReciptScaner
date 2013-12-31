package wb.receiptslibrary.activities;

import wb.receiptslibrary.SmartReceiptsApplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DispatcherActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dispatch(getLastActivity());
	}
	
	protected void dispatch(String activity) {
		Intent intent;
		if (activity.equals(SmartReceiptsActivity.TAG)) {
			intent = new Intent(this, SmartReceiptsActivity.class);
		}
		else if (activity.equals(ReceiptsActivity.TAG)) {
			intent = new Intent(this, ReceiptsActivity.class);
		}
		else if (activity.equals(ReceiptImageActivity.TAG)) {
			intent = new Intent(this, ReceiptImageActivity.class);
		}
		else { //Fallback case
			intent = new Intent(this, SmartReceiptsActivity.class);
		}
		intent.setAction(getIntent().getAction());
		startActivity(intent);
	}
	
	protected String getLastActivity() {
		return ((SmartReceiptsApplication) getApplication()).getPersistenceManager().getPreferences().getLastActivityTag();
	}
}
