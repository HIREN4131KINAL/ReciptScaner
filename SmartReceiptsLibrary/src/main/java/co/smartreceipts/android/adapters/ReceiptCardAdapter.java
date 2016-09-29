package co.smartreceipts.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;

public class ReceiptCardAdapter extends CardAdapter<Receipt> {

    private final BackupProvidersManager mBackupProvidersManager;
    private final Drawable mCloudDisabledDrawable;
    private final Drawable mNotSyncedDrawable;
    private final Drawable mSyncedDrawable;

	public ReceiptCardAdapter(Context context, Preferences preferences, BackupProvidersManager backupProvidersManager) {
		super(context, preferences);
        mBackupProvidersManager = backupProvidersManager;
        mCloudDisabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_off_24dp, context.getTheme());
        mNotSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_queue_24dp, context.getTheme());
        mSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_done_24dp, context.getTheme());
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
            if (data.getSyncState().isSynced(SyncProvider.GoogleDrive)) {
                Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mSyncedDrawable).into(image);
            } else {
                Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mNotSyncedDrawable).into(image);
            }
        } else {
            Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mCloudDisabledDrawable).into(image);
        }
    }
}
