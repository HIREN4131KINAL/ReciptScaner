package co.smartreceipts.android.trips.editor;

import java.io.File;
import java.sql.Date;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableControllerManager;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.utils.FileUtils;

public class TripCreateEditFragmentPresenter {

    @Inject
    TripCreateEditFragment fragment;
    @Inject
    Analytics analytics;
    @Inject
    TableControllerManager tableControllerManager;

    @Inject
    public TripCreateEditFragmentPresenter() {
    }

    public boolean checkTrip(String name, String startDateText, Date startDate,
                             String endDateText, Date endDate) {
        // Error Checking
        if (name.length() == 0 || startDateText.length() == 0 || endDateText.length() == 0) {
            fragment.showError(TripEditorErrors.MISSING_FIELD);
            return false;
        }
        if (startDate == null || endDate == null) {
            fragment.showError(TripEditorErrors.CALENDAR_ERROR);
            return false;
        }
        if (startDate.getTime() > endDate.getTime()) {
            fragment.showError(TripEditorErrors.DURATION_ERROR);
            return false;
        }
        if (name.startsWith(" ")) {
            fragment.showError(TripEditorErrors.SPACE_ERROR);
            return false;
        }
        if (FileUtils.filenameContainsIllegalCharacter(name)) {
            fragment.showError(TripEditorErrors.ILLEGAL_CHAR_ERROR);
            return false;
        }

        return true;
    }

    public Trip saveTrip(File file, Date startDate, Date endDate, String defaultCurrency,
                         String comment, String costCenter) {
        TripTableController tripTableController = tableControllerManager.getTripTableController();


        if (fragment.getTrip() == null) { // Insert
            analytics.record(Events.Reports.PersistNewReport);
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
            analytics.record(Events.Reports.PersistUpdateReport);
            final Trip updateTrip = new TripBuilderFactory(fragment.getTrip())
                    .setDirectory(file)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    // TODO: Update trip timezones iff date was changed
                    .setComment(comment)
                    .setCostCenter(costCenter)
                    .setDefaultCurrency(defaultCurrency)
                    .build();
            tripTableController.update(fragment.getTrip(), updateTrip, new DatabaseOperationMetadata());
            return updateTrip;
        }
    }
}
