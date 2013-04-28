package wb.receiptslibrary;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.wb.navigation.ViewController;

public class ViewHolderFactory {
	
	public interface State {
		public static final String IMAGE_URI = "imageUriState";
		public static final String CURR_TRIP = "currTripState";
		public static final String HIGH_RCPT = "highRcptState";
	}
	
	private ViewController controller;
	private SmartReceiptsActivity activity;
	
	public ViewHolderFactory(ViewController controller, SmartReceiptsActivity activity) {
		this.controller = controller;
		this.activity = activity;
	}
	
	public final void buildHomeHolder(final RelativeLayout mainLayout, final ListView listView) {
		this.controller.pushView(new HomeHolder(activity, mainLayout, listView));
	}
	
	/*
	public final void restoreHomeHolder(final RelativeLayout mainLayout, final ListView listView, Bundle bundle) {
		this.controller.pushView(new HomeHolder(activity, mainLayout, listView, bundle));
	}*/
	
	public final void naviagateBackwards() {
		this.controller.onBackPressed();
	}
	
	public final void buildReceiptImageViewHolder(TripRow currentTrip, ReceiptRow currentReceipt) {
		this.controller.pushView(new ReceiptImageViewHolder(activity, currentTrip, currentReceipt));
	}

}
