package co.smartreceipts.android.trips;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TripCardAdapter;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.fragments.ReceiptsFragment;
import co.smartreceipts.android.fragments.WBListFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.LastTripController;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.rating.FeedbackDialogFragment;
import co.smartreceipts.android.rating.RatingDialogFragment;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.widget.backups.RemoteBackupsDataCache;
import co.smartreceipts.android.utils.FileUtils;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Tooltip;
import co.smartreceipts.android.workers.EmailAssistant;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;

public class TripFragment extends WBListFragment implements TableEventsListener<Trip>, AdapterView.OnItemLongClickListener {

    private static final String ARG_NAVIGATE_TO_VIEW_LAST_TRIP = "arg_nav_to_last_trip";
    private static final String OUT_NAV_TO_LAST_TRIP = "out_nav_to_last_trip";

    private TripTableController mTripTableController;
    private TripFragmentPresenter mPresenter;
    private Analytics mAnalytics;

    private NavigationHandler mNavigationHandler;

    private TripCardAdapter mAdapter;
    private ProgressBar mProgressDialog;
    private TextView mNoDataAlert;

    private Tooltip mTooltip;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        mNavigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new FragmentProvider());
        mTripTableController = getSmartReceiptsApplication().getTableControllerManager().getTripTableController();
        mAdapter = new TripCardAdapter(getActivity(), getPersistenceManager().getPreferenceManager(), getSmartReceiptsApplication().getBackupProvidersManager());
        if (savedInstanceState == null) {
            mNavigateToLastTrip = getArguments().getBoolean(ARG_NAVIGATE_TO_VIEW_LAST_TRIP);
        } else {
            mNavigateToLastTrip = savedInstanceState.getBoolean(OUT_NAV_TO_LAST_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        final View rootView = inflater.inflate(R.layout.trip_fragment_layout, container, false);
        mProgressDialog = (ProgressBar) rootView.findViewById(R.id.progress);
        mNoDataAlert = (TextView) rootView.findViewById(R.id.no_data);
        mTooltip = (Tooltip) rootView.findViewById(R.id.trip_tooltip);
        rootView.findViewById(R.id.trip_action_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripMenu(null);
            }
        });
        mPresenter = new TripFragmentPresenter(this);
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

        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        mAnalytics = smartReceiptsApplication.getAnalyticsManager();

        mPresenter.checkRating();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        mTripTableController.subscribe(this);
        mTripTableController.get();
        getActivity().setTitle(getFlexString(R.string.sr_app_name));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");

        mTripTableController.unsubscribe(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putBoolean(OUT_NAV_TO_LAST_TRIP, mNavigateToLastTrip);
    }

    public final void tripMenu(final Trip trip) {
        final PersistenceManager persistenceManager = getPersistenceManager();
        if (!persistenceManager.getStorageManager().isExternal()) {
            Toast.makeText(getActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
            return;
        }

        if (trip == null) {
            mNavigationHandler.navigateToCreateTripFragment();
        } else {
            mNavigationHandler.navigateToEditTripFragment(trip);
        }
    }

    public final boolean editTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        final String[] editTripItems = getFlex().getStringArray(getActivity(), R.array.EDIT_TRIP_ITEMS);
        builder.setTitle(trip.getName())
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setItems(editTripItems, new DialogInterface.OnClickListener() {
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
        builder.setTitle(getString(R.string.delete_item, trip.getName()))
                .setMessage(getString(R.string.delete_sync_information))
                .setCancelable(true)
                .setPositiveButton(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mTripTableController.delete(trip, new DatabaseOperationMetadata());
                    }
                })
                .setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
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
    public void onInsertSuccess(@NonNull Trip trip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            viewReceipts(trip);
        }
    }

    @Override
    public void onInsertFailure(@NonNull Trip trip, @Nullable Throwable ex, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            if (ex != null) {
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Trip oldTip, @NonNull Trip newTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            viewReceipts(newTrip);
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Trip oldTrip, @Nullable Throwable ex, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            if (ex != null) {
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Trip oldTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
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
    public void onDeleteFailure(@NonNull Trip oldTrip, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
        }
    }

    private void viewReceipts(Trip trip) {
        mNavigationHandler.navigateToReportInfoFragment(trip);
    }

    public void showRatingTooltip() {
        mTooltip.setQuestion(R.string.rating_tooltip_text, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAnalytics.record(Events.Ratings.UserDeclinedRatingPrompt);
                mNavigationHandler.showDialog(new FeedbackDialogFragment());
                mTooltip.hideWithAnimation();
                mPresenter.dontShowRatingPrompt();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAnalytics.record(Events.Ratings.UserAcceptedRatingPrompt);
                mNavigationHandler.showDialog(new RatingDialogFragment());
                mTooltip.hideWithAnimation();
                mPresenter.dontShowRatingPrompt();
            }
        });

        mTooltip.showWithAnimation();
        mAnalytics.record(Events.Ratings.RatingPromptShown);
    }


}