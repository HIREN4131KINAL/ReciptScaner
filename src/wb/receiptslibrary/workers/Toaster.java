package wb.receiptslibrary.workers;

import wb.receiptslibrary.SmartReceiptsActivity;
import android.widget.Toast;

public class Toaster extends WorkerChild {

	Toaster(WorkerManager manager) {
		super(manager);
	}

	public void toastLong(int stringID) {
		SmartReceiptsActivity activity = mWorkerManager.getSmartReceiptsActivity();
    	Toast.makeText(activity, activity.getFlex().getString(stringID), Toast.LENGTH_LONG).show();
    }
    
    public void toastShort(int stringID) {
    	SmartReceiptsActivity activity = mWorkerManager.getSmartReceiptsActivity();
    	Toast.makeText(activity, activity.getFlex().getString(stringID), Toast.LENGTH_SHORT).show();
    }
    
}
