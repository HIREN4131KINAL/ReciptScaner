package co.smartreceipts.android.sync.widget;

import android.annotation.SuppressLint;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.network.SupportedNetworkType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class BackupsFragment extends WBFragment implements BackupProviderChangeListener {

    private static final int IMPORT_SMR_REQUEST_CODE = 50;
    private static final String SMR_EXTENSION = "smr";

    private BackupProvidersManager mBackupProvidersManager;
    private RemoteBackupsDataCache mRemoteBackupsDataCache;
    private CompositeSubscription mCompositeSubscription;
    private NavigationHandler mNavigationHandler;

    private Toolbar mToolbar;
    private View mHeaderView;
    private View mExportButton;
    private View mImportButton;
    private View mBackupConfigButton;
    private ImageView mBackupConfigButtonImage;
    private TextView mBackupConfigButtonText;
    private TextView mWarningTextView;
    private CheckBox mWifiOnlyCheckbox;
    private View mExistingBackupsSection;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBackupProvidersManager = getSmartReceiptsApplication().getBackupProvidersManager();
        mRemoteBackupsDataCache = new RemoteBackupsDataCache(getFragmentManager(), mBackupProvidersManager, getPersistenceManager().getDatabase());
        mNavigationHandler = new NavigationHandler(getActivity());
    }

    @Nullable
    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.backups_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);

        mHeaderView = inflater.inflate(R.layout.backups_header, null);
        mExportButton = mHeaderView.findViewById(R.id.manual_backup_export);
        mImportButton = mHeaderView.findViewById(R.id.manual_backup_import);
        mWarningTextView = (TextView) mHeaderView.findViewById(R.id.auto_backup_warning);
        mBackupConfigButton = mHeaderView.findViewById(R.id.automatic_backup_config_button);
        mBackupConfigButtonImage = (ImageView) mHeaderView.findViewById(R.id.automatic_backup_config_button_image);
        mBackupConfigButtonText = (TextView) mHeaderView.findViewById(R.id.automatic_backup_config_button_text);
        mWifiOnlyCheckbox = (CheckBox) mHeaderView.findViewById(R.id.auto_backup_wifi_only);
        mExistingBackupsSection = mHeaderView.findViewById(R.id.existing_backups_section);

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavigationHandler.showDialog(new ExportBackupDialogFragment());
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
        mWifiOnlyCheckbox.setChecked(getPersistenceManager().getPreferences().getAutoBackupOnWifiOnly());
        mWifiOnlyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                getPersistenceManager().getPreferences().getSharedPreferences().edit().putBoolean(getString(R.string.pref_no_category_auto_backup_wifi_only_key), checked).apply();
                mBackupProvidersManager.setAndInitializeNetworkProviderType(checked ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setAdapter(new RemoteBackupsListAdapter(mHeaderView, getActivity(), mBackupProvidersManager, getPersistenceManager().getPreferences()));
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
                    mNavigationHandler.showDialog(ImportLocalBackupDialogFragment.newInstance(data.getData()));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return mNavigationHandler.navigateBack();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        mBackupProvidersManager.unregisterChangeListener(this);
        mCompositeSubscription.unsubscribe();
        super.onPause();
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        updateViewsForProvider(newProvider);
    }

    public void updateViewsForProvider(@NonNull SyncProvider syncProvider) {
        if (isResumed()) {
            if (syncProvider == SyncProvider.None) {
                mWarningTextView.setText(R.string.auto_backup_warning_none);
                mBackupConfigButtonText.setText(R.string.auto_backup_configure);
                mBackupConfigButtonImage.setImageResource(R.drawable.ic_cloud_off_24dp);
                mWifiOnlyCheckbox.setVisibility(View.GONE);
            } else if (syncProvider == SyncProvider.GoogleDrive) {
                mWarningTextView.setText(R.string.auto_backup_warning_drive);
                mBackupConfigButtonText.setText(R.string.auto_backup_source_google_drive);
                mBackupConfigButtonImage.setImageResource(R.drawable.ic_cloud_done_24dp);
                mWifiOnlyCheckbox.setVisibility(View.VISIBLE);
            } else {
                throw new IllegalArgumentException("Unsupported sync provider type was specified");
            }

            mCompositeSubscription.add(mRemoteBackupsDataCache.getBackups(syncProvider)
                    .subscribe(new Action1<List<RemoteBackupMetadata>>() {
                        @Override
                        public void call(List<RemoteBackupMetadata> remoteBackupMetadatas) {
                            if (remoteBackupMetadatas.isEmpty()) {
                                mExistingBackupsSection.setVisibility(View.GONE);
                            } else {
                                mExistingBackupsSection.setVisibility(View.VISIBLE);
                            }
                            final RemoteBackupsListAdapter remoteBackupsListAdapter = new RemoteBackupsListAdapter(mHeaderView, getActivity(), mBackupProvidersManager, getPersistenceManager().getPreferences(), remoteBackupMetadatas);
                            mRecyclerView.setAdapter(remoteBackupsListAdapter);
                        }
                    }));
        }
    }

}
