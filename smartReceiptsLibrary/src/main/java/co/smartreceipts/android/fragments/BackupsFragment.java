package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;

public class BackupsFragment extends WBFragment {

    private BackupProvidersManager mBackupProvidersManager;

    private Toolbar mToolbar;
    private Button mExportButton;
    private Button mImportButton;
    private Button mBackupConfigButton;
    private TextView mWarningTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackupProvidersManager = getSmartReceiptsApplication().getBackupProvidersManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.backups, container, false);
        mExportButton = (Button) view.findViewById(R.id.manual_backup_export);
        mImportButton = (Button) view.findViewById(R.id.manual_backup_import);
        mWarningTextView = (TextView) view.findViewById(R.id.auto_backup_warning);
        mBackupConfigButton = (Button) view.findViewById(R.id.automatic_backup_config_button);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateViewsForProvider(mBackupProvidersManager.getSyncProvider());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.backups);
        }
    }

    private void updateViewsForProvider(@NonNull SyncProvider syncProvider) {
        if (syncProvider == SyncProvider.None) {
            mWarningTextView.setVisibility(View.VISIBLE);
            mBackupConfigButton.setText(R.string.auto_backup_configure);
        } else if (syncProvider == SyncProvider.GoogleDrive) {
            mWarningTextView.setVisibility(View.GONE);
            mBackupConfigButton.setText(R.string.auto_backup_source_google_drive);
        } else {
            throw new IllegalArgumentException("Unsupported sync provider type was specified");
        }
    }
}
