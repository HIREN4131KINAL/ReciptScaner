package co.smartreceipts.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.Attachable;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TripCardAdapter;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.LastTripController;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.utils.FileUtils;
import co.smartreceipts.android.workers.EmailAssistant;
import co.smartreceipts.android.workers.ImportTask;
import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;

public class TripFragment extends WBListFragment implements BooleanTaskCompleteDelegate, TableEventsListener<Trip>, AdapterView.OnItemLongClickListener {

    public static final String TAG = "TripFragment";

    private static final String ARG_NAVIGATE_TO_VIEW_LAST_TRIP = "arg_nav_to_last_trip";
    private static final String OUT_NAV_TO_LAST_TRIP = "out_nav_to_last_trip";

    private NavigationHandler mNavigationHandler;
    private TripCardAdapter mAdapter;
    private AutoCompleteAdapter mNameAutoCompleteAdapter, mCostCenterAutoCompleteAdapter;
    private Attachable mAttachable;
    private ProgressBar mProgressDialog;
    private TextView mNoDataAlert;
    private TripTableController mTripTableController;
    private boolean mNavigateToLastTrip;

    public static TripFragment newInstance() {
        return newInstance(false);
    }

    public static TripFragment newInstance(boolean navigateToViewLastTrip) {
        final TripFragment tripFragment = new TripFragment();
        final Bundle args = new Bundle();
        args.putBoolean(ARG_NAVIGATE_TO_VIEW_LAST_TRIP, navigateToViewLastTrip);
        tripFragment.setArguments(args);
        return tripFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        if (activity instanceof Attachable) {
            mAttachable = (Attachable) activity;
        } else {
            throw new IllegalArgumentException("The TripFragment's Activity must extend the Navigable and Attachable interfaces");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mNavigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new DefaultFragmentProvider());
        mTripTableController = getSmartReceiptsApplication().getTableControllerManager().getTripTableController();
        mAdapter = new TripCardAdapter(getActivity(), getPersistenceManager().getPreferences());
        if (savedInstanceState == null) {
            mNavigateToLastTrip = getArguments().getBoolean(ARG_NAVIGATE_TO_VIEW_LAST_TRIP);
        } else {
            mNavigateToLastTrip = savedInstanceState.getBoolean(OUT_NAV_TO_LAST_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View rootView = inflater.inflate(getLayoutId(), container, false);
        mProgressDialog = (ProgressBar) rootView.findViewById(R.id.progress);
        mNoDataAlert = (TextView) rootView.findViewById(R.id.no_data);
        rootView.findViewById(R.id.trip_action_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripMenu(null);
            }
        });
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(mAdapter); // Set this here to ensure this has been laid out already
        getListView().setOnItemLongClickListener(this);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    public int getLayoutId() {
        return R.layout.trip_fragment_layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mTripTableController.subscribe(this);
        mTripTableController.get();
        getActivity().setTitle(getFlexString(R.string.sr_app_name));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setSubtitle(null);
        }
        // Handles SMR imports
        final Attachment attachment = mAttachable.getAttachment();
        if (attachment != null && attachment.isValid() && attachment.isSMR() && attachment.isActionView()) {
            performImport(attachment.getUri());
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        if (mNameAutoCompleteAdapter != null) {
            mNameAutoCompleteAdapter.onPause();
        }
        if (mCostCenterAutoCompleteAdapter != null) {
            mCostCenterAutoCompleteAdapter.onPause();
        }
        mTripTableController.unsubscribe(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(OUT_NAV_TO_LAST_TRIP, mNavigateToLastTrip);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG, "Result Code: " + resultCode);
        Log.d(TAG, "Request Code: " + requestCode);

        if (resultCode == Activity.RESULT_OK) { // -1
            if (requestCode == ImportTask.TASK_ID) {
                if (data != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            getContext().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                        }
                    } catch (SecurityException e) {
                        Log.e(TAG, "Swalling security exception", e);
                    }
                    performImport(data.getData());
                }
            }
        } else {
            Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public final void tripMenu(final Trip trip) {
        final PersistenceManager persistenceManager = getPersistenceManager();
        if (!persistenceManager.getStorageManager().isExternal()) {
            Toast.makeText(getActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
            return;
        }

        final boolean newTrip = (trip == null);

        final View scrollView = getFlex().getView(getActivity(), R.layout.dialog_tripmenu);
        final AutoCompleteTextView nameBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), scrollView, R.id.dialog_tripmenu_name);
        final DateEditText startBox = (DateEditText) getFlex().getSubView(getActivity(), scrollView, R.id.dialog_tripmenu_start);
        final DateEditText endBox = (DateEditText) getFlex().getSubView(getActivity(), scrollView, R.id.dialog_tripmenu_end);
        final Spinner currencySpinner = (Spinner) getFlex().getSubView(getActivity(), scrollView, R.id.dialog_tripmenu_currency);
        final EditText commentBox = (EditText) getFlex().getSubView(getActivity(), scrollView, R.id.dialog_tripmenu_comment);
        final AutoCompleteTextView costCenterBox = (AutoCompleteTextView) scrollView.findViewById(R.id.dialog_tripmenu_cost_center);
        costCenterBox.setVisibility(getPersistenceManager().getPreferences().getIncludeCostCenter() ? View.VISIBLE : View.GONE);

        final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCurrenciesList());
        currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currenices);

        // Show default dictionary with auto-complete
        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
        nameBox.setKeyListener(input);

        // Fill Out Fields
        if (newTrip) {
            if (persistenceManager.getPreferences().enableAutoCompleteSuggestions()) {
                final DatabaseHelper db = getPersistenceManager().getDatabase();
                if (mNameAutoCompleteAdapter == null) {
                    mNameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_TRIPS_NAME, db);
                } else {
                    mNameAutoCompleteAdapter.reset();
                }
                if (mCostCenterAutoCompleteAdapter == null) {
                    mCostCenterAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_TRIPS_COST_CENTER, db);
                } else {
                    mCostCenterAutoCompleteAdapter.reset();
                }
                nameBox.setAdapter(mNameAutoCompleteAdapter);
                costCenterBox.setAdapter(mCostCenterAutoCompleteAdapter);
            }
            startBox.setFocusableInTouchMode(false);
            startBox.setOnClickListener(getDateManager().getDurationDateEditTextListener(endBox));
            int idx = currenices.getPosition(getPersistenceManager().getPreferences().getDefaultCurreny());
            if (idx > 0) {
                currencySpinner.setSelection(idx);
            }
        } else {
            if (trip.getDirectory() != null) {
                nameBox.setText(trip.getName());
            }
            if (trip.getStartDate() != null) {
                startBox.setText(trip.getFormattedStartDate(getActivity(), getPersistenceManager().getPreferences().getDateSeparator()));
                startBox.date = trip.getStartDate();
            }
            if (trip.getEndDate() != null) {
                endBox.setText(trip.getFormattedEndDate(getActivity(), getPersistenceManager().getPreferences().getDateSeparator()));
                endBox.date = trip.getEndDate();
            }
            if (!TextUtils.isEmpty(trip.getComment())) {
                commentBox.setText(trip.getComment());
            }
            int idx = currenices.getPosition(trip.getDefaultCurrencyCode());
            if (idx > 0) {
                currencySpinner.setSelection(idx);
            }
            startBox.setFocusableInTouchMode(false);
            startBox.setOnClickListener(getDateManager().getDateEditTextListener());
            if (!TextUtils.isEmpty(trip.getCostCenter())) {
                costCenterBox.setText(trip.getCostCenter());
            }

            currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    final String newCurrencyCode = currenices.getItem(position).toString();
                    if (!trip.getDefaultCurrencyCode().equals(newCurrencyCode)) {
                        Toast.makeText(view.getContext(), R.string.toast_warning_reset_exchange_rate, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Intentional no-op
                }
            });
        }
        endBox.setFocusableInTouchMode(false);
        endBox.setOnClickListener(getDateManager().getDateEditTextListener());
        nameBox.setSelection(nameBox.getText().length()); // Put the cursor at the end

        // Show the DialogController
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        builder.setTitle((newTrip) ? getFlexString(R.string.DIALOG_TRIPMENU_TITLE_NEW) : getFlexString(R.string.DIALOG_TRIPMENU_TITLE_EDIT)).setCancelable(true).setView(scrollView).setLongLivedPositiveButton((newTrip) ? getFlexString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_CREATE) : getFlexString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_UPDATE), new LongLivedOnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = nameBox.getText().toString().trim();
                final String startDate = startBox.getText().toString();
                final String endDate = endBox.getText().toString();
                final String defaultCurrencyCode = currencySpinner.getSelectedItem().toString();
                final String comment = commentBox.getText().toString();
                final String costCenter = costCenterBox.getText().toString();
                // Error Checking
                if (name.length() == 0 || startDate.length() == 0 || endDate.length() == 0) {
                    Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_TRIPMENU_TOAST_MISSING_FIELD), Toast.LENGTH_LONG).show();
                    return;
                }
                if (startBox.date == null || endBox.date == null) {
                    Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_LONG).show();
                    return;
                }
                if (startBox.date.getTime() > endBox.date.getTime()) {
                    Toast.makeText(getActivity(), getFlexString(R.string.DURATION_ERROR), Toast.LENGTH_LONG).show();
                    return;
                }
                if (name.startsWith(" ")) {
                    Toast.makeText(getActivity(), getFlexString(R.string.SPACE_ERROR), Toast.LENGTH_LONG).show();
                    return;
                }
                if (FileUtils.filenameContainsIllegalCharacter(name)) {
                    Toast.makeText(getActivity(), getFlexString(R.string.ILLEGAL_CHAR_ERROR), Toast.LENGTH_LONG).show();
                    return;
                }

                if (newTrip) { // Insert
                    getSmartReceiptsApplication().getAnalyticsManager().record(Events.Reports.PersistNewReport);
                    final Trip insertTrip = new TripBuilderFactory()
                            .setDirectory(persistenceManager.getStorageManager().getFile(name))
                            .setStartDate(startBox.date)
                            .setEndDate(endBox.date)
                            .setComment(comment)
                            .setCostCenter(costCenter)
                            .setDefaultCurrency(defaultCurrencyCode)
                            .build();
                    mTripTableController.insert(insertTrip);
                    dialog.cancel();
                } else { // Update
                    getSmartReceiptsApplication().getAnalyticsManager().record(Events.Reports.PersistUpdateReport);
                    final Trip updateTrip = new TripBuilderFactory(trip)
                            .setDirectory(persistenceManager.getStorageManager().getFile(name))
                            .setStartDate(startBox.date)
                            .setEndDate(endBox.date)
                            // TODO: Update trip timezones iff date was changed
                            .setComment(comment)
                            .setCostCenter(costCenter)
                            .setDefaultCurrency(defaultCurrencyCode)
                            .build();
                    mTripTableController.update(trip, updateTrip);
                    dialog.cancel();
                }
            }
        }).setNegativeButton(getFlexString(R.string.DIALOG_TRIPMENU_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    public final boolean editTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        final String[] editTripItems = getFlex().getStringArray(getActivity(), R.array.EDIT_TRIP_ITEMS);
        builder.setTitle(trip.getName()).setCancelable(true).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).setItems(editTripItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                final String selection = editTripItems[item];
                if (selection == editTripItems[0]) {
                    TripFragment.this.tripMenu(trip);
                } else if (selection == editTripItems[1]) {
                    TripFragment.this.deleteTrip(trip);
                }
                dialog.cancel();
            }
        }).show();
        return true;
    }

    public final void deleteTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        builder.setTitle(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON_TITLE_START) + " " + trip.getName() + getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON_TITLE_END)).setCancelable(true).setPositiveButton(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mTripTableController.delete(trip);
            }
        }).setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        viewReceipts(mAdapter.getItem(position));
        // v.setSelected(true);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
        editTrip(mAdapter.getItem(position));
        return true;
    }

    @Override
    public void onGetSuccess(@NonNull List<Trip> trips) {
        if (isResumed()) {
            mProgressDialog.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
            if (trips.isEmpty()) {
                mNoDataAlert.setVisibility(View.VISIBLE);
            } else {
                mNoDataAlert.setVisibility(View.INVISIBLE);
            }
            mAdapter.notifyDataSetChanged(trips);

            if (!trips.isEmpty() && mNavigateToLastTrip) {
                mNavigateToLastTrip = false;
                // If we have trips, open up whatever one was last used
                final LastTripController lastTripController = new LastTripController(getActivity());
                final Trip lastTrip = lastTripController.getLastTrip(trips);
                if (lastTrip != null) {
                    viewReceipts(lastTrip);
                }
            }
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        if (isResumed()) {
            if (e instanceof SQLiteDatabaseCorruptException) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_sql_corrupt_title).setMessage(R.string.dialog_sql_corrupt_message).setPositiveButton(R.string.dialog_sql_corrupt_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.dialog_sql_corrupt_intent_subject), getString(R.string.dialog_sql_corrupt_intent_text));
                        getActivity().startActivity(Intent.createChooser(intent, getResources().getString(R.string.dialog_sql_corrupt_chooser)));
                        dialog.dismiss();
                            }
                }).show();
            } else {
                Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onInsertSuccess(@NonNull Trip trip) {
        if (isResumed()) {
            viewReceipts(trip);
        }
    }

    @Override
    public void onInsertFailure(@NonNull Trip trip, @Nullable Throwable ex) {
        if (isAdded()) {
            if (ex != null) {
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Trip oldTip, @NonNull Trip newTrip) {
        if (isResumed()) {
            viewReceipts(newTrip);
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Trip oldTrip, @Nullable Throwable ex) {
        if (isAdded()) {
            if (ex != null) {
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Trip oldTrip) {
        if (isAdded()) {
            final Fragment detailsFragment = getFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
            if (detailsFragment != null) {
                getFragmentManager().beginTransaction().remove(detailsFragment).commit();
                final ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(getFlexString(R.string.sr_app_name));
                }
            }
        }
        mTripTableController.get();
    }

    @Override
    public void onDeleteFailure(@NonNull Trip oldTrip, @Nullable Throwable e) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
        }
    }

    private void viewReceipts(Trip trip) {
        mNavigationHandler.navigateToReportInfoFragment(trip);
    }

    private void performImport(final Uri uri) {
        final CheckBox overwrite = new CheckBox(getActivity());
        overwrite.setText(" Overwrite Existing Data?");
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        builder.setTitle(R.string.import_string).setView(overwrite).setCancelable(true).setPositiveButton(R.string.import_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                (new ImportTask(getActivity(), TripFragment.this, getString(R.string.progress_import), IMPORT_TASK_ID, overwrite.isChecked(), getPersistenceManager())).execute(uri);
                mAttachable.setAttachment(null);
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAttachable.setAttachment(null);
            }
        }).show();
    }

    private final int IMPORT_TASK_ID = 1;

    @Override
    public synchronized void onBooleanTaskComplete(int taskID, Boolean success) {
        if (taskID == IMPORT_TASK_ID) {
            if (success) {
                Toast.makeText(getActivity(), R.string.toast_import_complete, Toast.LENGTH_LONG).show();
                getActivity().finish(); // TODO: Fix this hack - finishing the activity to get rid of the old Intent so
                // we don't reshow import dialog
            } else {
                Toast.makeText(getActivity(), getFlexString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
            }
            mTripTableController.get();
        }
    }

}