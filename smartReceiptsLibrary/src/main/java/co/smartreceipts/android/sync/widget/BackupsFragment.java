package co.smartreceipts.android.sync.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.ExportBackupDialogFragment;
import co.smartreceipts.android.fragments.ImportBackupDialogFragment;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class BackupsFragment extends WBFragment implements BackupProviderChangeListener {

    private static final int IMPORT_SMR_REQUEST_CODE = 50;
    private static final String SMR_EXTENSION = "smr";

    private BackupProvidersManager mBackupProvidersManager;
    private RemoteBackupsResultsCache mRemoteBackupsResultsCache;
    private CompositeSubscription mCompositeSubscription;

    private Toolbar mToolbar;
    private Button mExportButton;
    private Button mImportButton;
    private Button mBackupConfigButton;
    private TextView mWarningTextView;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackupProvidersManager = getSmartReceiptsApplication().getBackupProvidersManager();
        mRemoteBackupsResultsCache = new RemoteBackupsResultsCache(getFragmentManager(), mBackupProvidersManager);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.backups, container, false);
        mExportButton = (Button) view.findViewById(R.id.manual_backup_export);
        mImportButton = (Button) view.findViewById(R.id.manual_backup_import);
        mWarningTextView = (TextView) view.findViewById(R.id.auto_backup_warning);
        mBackupConfigButton = (Button) view.findViewById(R.id.automatic_backup_config_button);
        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ExportBackupDialogFragment exportBackupDialogFragment = new ExportBackupDialogFragment();
                exportBackupDialogFragment.show(getFragmentManager(), ExportBackupDialogFragment.TAG);
            }
        });
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.import_string)), IMPORT_SMR_REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mBackupConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SelectAutomaticBackupProviderDialogFragment selectAutomaticBackupProviderDialogFragment = new SelectAutomaticBackupProviderDialogFragment();
                selectAutomaticBackupProviderDialogFragment.show(getFragmentManager(), SelectAutomaticBackupProviderDialogFragment.TAG);
            }
        });
        return view;
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
        mCompositeSubscription = new CompositeSubscription();
        mBackupProvidersManager.registerChangeListener(this);
        updateViewsForProvider(mBackupProvidersManager.getSyncProvider());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMPORT_SMR_REQUEST_CODE) {
                if (data != null) {
                    final ImportBackupDialogFragment importBackupDialogFragment = ImportBackupDialogFragment.newInstance(data.getData());
                    importBackupDialogFragment.show(getFragmentManager(), ImportBackupDialogFragment.TAG);
                }
            }
        }
    }

    @Override
    public void onPause() {
        mBackupProvidersManager.unregisterChangeListener(this);
        mCompositeSubscription.unsubscribe();
        super.onPause();
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

        mCompositeSubscription.add(mRemoteBackupsResultsCache.getBackups(syncProvider)
                .subscribe(new Action1<List<RemoteBackupMetadata>>() {
                    @Override
                    public void call(List<RemoteBackupMetadata> remoteBackupMetadatas) {
                        final RemoteBackupsListAdapter remoteBackupsListAdapter = new RemoteBackupsListAdapter(remoteBackupMetadatas);
                        mRecyclerView.setAdapter(remoteBackupsListAdapter);
                    }
                }));
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        updateViewsForProvider(newProvider);
    }
}
