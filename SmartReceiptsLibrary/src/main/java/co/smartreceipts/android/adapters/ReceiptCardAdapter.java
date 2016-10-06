package co.smartreceipts.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.widget.AutomaticBackupsInfoDialogFragment;

public class ReceiptCardAdapter extends CardAdapter<Receipt> {

    private final FragmentActivity mFragmentActivity;

	public ReceiptCardAdapter(FragmentActivity fragmentActivity, Preferences preferences, BackupProvidersManager backupProvidersManager) {
		super(fragmentActivity, preferences, backupProvidersManager);
        mFragmentActivity = fragmentActivity;
	}
	
	@Override
	protected String getPrice(Receipt data) {
		return data.getPrice().getCurrencyFormattedPrice();
	}
	
	@Override
	protected void setPriceTextView(TextView textView, Receipt data) {
		textView.setText(getPrice(data));
	}
	
	@Override
	protected void setNameTextView(TextView textView, Receipt data) {
		textView.setText(data.getName());
	}
	
	@Override
	protected void setDateTextView(TextView textView, Receipt data) {
		if (getPreferences().isShowDate()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getFormattedDate(getContext(), getPreferences().getDateSeparator()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setCategory(TextView textView, Receipt data) {
		if (getPreferences().isShowCategory()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getCategory().getName());
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setMarker(TextView textView, Receipt data) {
		if (getPreferences().isShowPhotoPDFMarker()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getMarkerAsString(getContext()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}

    @Override
    protected void setSyncStateImage(ImageView image, Receipt data) {
        if (mBackupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            image.setClickable(false);
            if (data.getSyncState().isSynced(SyncProvider.GoogleDrive)) {
                Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mSyncedDrawable).into(image);
            } else {
                Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mNotSyncedDrawable).into(image);
            }
            image.setOnClickListener(null);
        } else {
            image.setClickable(true);
            Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mCloudDisabledDrawable).into(image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new NavigationHandler(mFragmentActivity).showDialog(new AutomaticBackupsInfoDialogFragment());
                }
            });
        }
    }
}
