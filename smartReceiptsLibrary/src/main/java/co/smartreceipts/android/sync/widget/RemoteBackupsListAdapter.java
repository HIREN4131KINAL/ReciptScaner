package co.smartreceipts.android.sync.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;

public class RemoteBackupsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final View mHeaderView;
    private final BackupProvidersManager mBackupProvidersManager;
    private final Preferences mPreferences;
    private final List<RemoteBackupMetadata> mBackupMetadataList;

    public RemoteBackupsListAdapter(@NonNull View headerView, @NonNull BackupProvidersManager backupProvidersManager, @NonNull Preferences preferences) {
        this(headerView, backupProvidersManager, preferences, Collections.<RemoteBackupMetadata>emptyList());
    }

    public RemoteBackupsListAdapter(@NonNull View headerView, @NonNull BackupProvidersManager backupProvidersManager,
                                    @NonNull Preferences preferences, @NonNull List<RemoteBackupMetadata> backupMetadataList) {
        mHeaderView = Preconditions.checkNotNull(headerView);
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        mPreferences = Preconditions.checkNotNull(preferences);
        mBackupMetadataList = new ArrayList<>(backupMetadataList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        } else {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remote_backups_list_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;
            final Context context = itemHolder.backupDeviceNameTextView.getContext();
            final RemoteBackupMetadata metadata = mBackupMetadataList.get(position - 1);
            if (metadata.getSyncDeviceId().equals(mBackupProvidersManager.getDeviceSyncId())) {
                itemHolder.backupDeviceNameTextView.setText(context.getString(R.string.existing_remote_backup_current_device, metadata.getSyncDeviceName()));
            } else {
                itemHolder.backupDeviceNameTextView.setText(metadata.getSyncDeviceName());
            }
            itemHolder.backupProviderTextView.setText(R.string.auto_backup_source_google_drive);
            itemHolder.backupDateTextView.setText(ModelUtils.getFormattedDate(metadata.getLastModifiedDate(), TimeZone.getDefault(), context, mPreferences.getDateSeparator()));
            final View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, itemHolder.backupOverflowView);
                    popupMenu.getMenuInflater().inflate(R.menu.remote_backups_list_item_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.remote_backups_list_item_menu_restore) {
                                // TODO
                                return true;
                            } else if (item.getItemId() == R.id.remote_backups_list_item_menu_delete) {
                                // TODO
                                return true;
                            } else {
                                throw new IllegalArgumentException("Unsupported menu type was selected");
                            }
                        }
                    });
                    popupMenu.show();
                }
            };
            itemHolder.parentView.setOnClickListener(onClickListener);
            itemHolder.backupOverflowView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mBackupMetadataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        final View headerView;

        HeaderViewHolder(@NonNull View view) {
            super(view);
            headerView = view;
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        final View parentView;
        final TextView backupDeviceNameTextView;
        final TextView backupProviderTextView;
        final TextView backupDateTextView;
        final ImageView backupOverflowView;

        ItemViewHolder(@NonNull View view) {
            super(view);
            parentView = view;
            backupDeviceNameTextView = (TextView) view.findViewById(R.id.remote_backup_device_name);
            backupProviderTextView = (TextView) view.findViewById(R.id.remote_backup_provider);
            backupDateTextView = (TextView) view.findViewById(R.id.remote_backup_date);
            backupOverflowView = (ImageView) view.findViewById(R.id.remote_backup_metadata_overflow);
        }
    }
}
