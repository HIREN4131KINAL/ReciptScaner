package co.smartreceipts.android.sync.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;

public class RemoteBackupsListAdapter extends RecyclerView.Adapter<RemoteBackupsListAdapter.ViewHolder> {

    private final List<RemoteBackupMetadata> mBackupMetadataList;

    public RemoteBackupsListAdapter() {
        this(Collections.<RemoteBackupMetadata>emptyList());
    }

    public RemoteBackupsListAdapter(@NonNull List<RemoteBackupMetadata> backupMetadataList) {
        mBackupMetadataList = new ArrayList<>(backupMetadataList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remote_backups_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RemoteBackupMetadata metadata = mBackupMetadataList.get(position);
        holder.backupDeviceNameTextView.setText(metadata.getSyncDeviceName());
        holder.backupDateTextView.setText(metadata.getLastModifiedDate().toString());
    }

    @Override
    public int getItemCount() {
        return mBackupMetadataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView backupDeviceNameTextView;
        final TextView backupDateTextView;

        ViewHolder(@NonNull View view) {
            super(view);
            backupDeviceNameTextView = (TextView) view.findViewById(R.id.remote_backup_device_name);
            backupDateTextView = (TextView) view.findViewById(R.id.remote_backup_date);
        }
    }
}
