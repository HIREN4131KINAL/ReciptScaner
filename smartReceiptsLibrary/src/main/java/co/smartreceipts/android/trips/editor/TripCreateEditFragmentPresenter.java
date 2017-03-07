package co.smartreceipts.android.trips.editor;

import java.io.File;
import java.sql.Date;

import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.utils.FileUtils;

public class TripCreateEditFragmentPresenter {

    private TripCreateEditFragment mFragment;

    public TripCreateEditFragmentPresenter(TripCreateEditFragment fragment) {
        mFragment = fragment;
    }

    public boolean checkTrip(String name, String startDateText, Date startDate,
                             String endDateText, Date endDate) {
        // Error Checking
        if (name.length() == 0 || startDateText.length() == 0 || endDateText.length() == 0) {
            mFragment.showError(TripEditorErrors.MISSING_FIELD);
            return false;
        }
        if (startDate == null || endDate == null) {
            mFragment.showError(TripEditorErrors.CALENDAR_ERROR);
            return false;
        }
        if (startDate.getTime() > endDate.getTime()) {
            mFragment.showError(TripEditorErrors.DURATION_ERROR);
            return false;
        }
        if (name.startsWith(" ")) {
            mFragment.showError(TripEditorErrors.SPACE_ERROR);
            return false;
        }
        if (FileUtils.filenameContainsIllegalCharacter(name)) {
            mFragment.showError(TripEditorErrors.ILLEGAL_CHAR_ERROR);
            return false;
        }

        return true;
    }

    public Trip saveTrip(File file, Date startDate, Date endDate, String defaultCurrency,
                         String comment, String costCenter) {
        TripTableController tripTableController = mFragment.getSmartReceiptsApplication()
                .getTableControllerManager().getTripTableController();


        if (mFragment.getTrip() == null) { // Insert
            mFragment.getSmartReceiptsApplication().getAnalyticsManager().record(Events.Reports.PersistNewReport);
            final Trip insertTrip = new TripBuilderFactory()
                    .setDirectory(file)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setComment(comment)
                    .setCostCenter(costCenter)
                    .setDefaultCurrency(defaultCurrency)
                    .build();
            tripTableController.insert(insertTrip, new DatabaseOperationMetadata());
            return insertTrip;
        } else { // Update
            mFragment.getSmartReceiptsApplication().getAnalyticsManager().record(Events.Reports.PersistUpdateReport);
            final Trip updateTrip = new TripBuilderFactory(mFragment.getTrip())
                    .setDirectory(file)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    // TODO: Update trip timezones iff date was changed
                    .setComment(comment)
                    .setCostCenter(costCenter)
                    .setDefaultCurrency(defaultCurrency)
                    .build();
            tripTableController.update(mFragment.getTrip(), updateTrip, new DatabaseOperationMetadata());
            return updateTrip;
        }
    }
}
