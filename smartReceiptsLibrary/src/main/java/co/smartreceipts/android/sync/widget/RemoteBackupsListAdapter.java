package co.smartreceipts.android.sync.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;

public class RemoteBackupsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final View mHeaderView;
    private final List<RemoteBackupMetadata> mBackupMetadataList;

    public RemoteBackupsListAdapter(@NonNull View headerView) {
        this(headerView, Collections.<RemoteBackupMetadata>emptyList());
    }

    public RemoteBackupsListAdapter(@NonNull View headerView, @NonNull List<RemoteBackupMetadata> backupMetadataList) {
        mHeaderView = Preconditions.checkNotNull(headerView);
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
            final RemoteBackupMetadata metadata = mBackupMetadataList.get(position - 1);
            itemHolder.backupDeviceNameTextView.setText(metadata.getSyncDeviceName());
            itemHolder.backupDateTextView.setText(metadata.getLastModifiedDate().toString());
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

        final TextView backupDeviceNameTextView;
        final TextView backupDateTextView;

        ItemViewHolder(@NonNull View view) {
            super(view);
            backupDeviceNameTextView = (TextView) view.findViewById(R.id.remote_backup_device_name);
            backupDateTextView = (TextView) view.findViewById(R.id.remote_backup_date);
        }
    }
}
