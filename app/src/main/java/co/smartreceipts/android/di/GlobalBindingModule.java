package co.smartreceipts.android.di;

import android.app.Activity;
import android.support.v4.app.Fragment;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.di.subcomponents.BackupsFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.CSVColumnsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.CategoriesListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DeleteRemoteBackupDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DeleteRemoteBackupProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DistanceDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DistanceFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ExportBackupWorkerProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.FeedbackDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.GenerateReportFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ImportLocalBackupWorkerProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.InformAboutPdfImageAttachmentDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.LoginFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.OcrInformationalFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.OcrInformationalTooltipFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.PDFColumnsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.PaymentMethodsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.RatingDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptCreateEditFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptImageFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptMoveCopyDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReportInfoFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.SelectAutomaticBackupProviderDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.SettingsActivitySubcomponent;
import co.smartreceipts.android.di.subcomponents.SmartReceiptsActivitySubcomponent;
import co.smartreceipts.android.di.subcomponents.SyncErrorFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.TripCreateEditFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.TripFragmentSubcomponent;
import co.smartreceipts.android.fragments.DistanceDialogFragment;
import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import co.smartreceipts.android.fragments.InformAboutPdfImageAttachmentDialogFragment;
import co.smartreceipts.android.fragments.ReceiptCreateEditFragment;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReceiptMoveCopyDialogFragment;
import co.smartreceipts.android.fragments.ReceiptsListFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import co.smartreceipts.android.identity.widget.LoginFragment;
import co.smartreceipts.android.ocr.info.OcrInformationalFragment;
import co.smartreceipts.android.ocr.info.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.rating.FeedbackDialogFragment;
import co.smartreceipts.android.rating.RatingDialogFragment;
import co.smartreceipts.android.settings.widget.CSVColumnsListFragment;
import co.smartreceipts.android.settings.widget.CategoriesListFragment;
import co.smartreceipts.android.settings.widget.PDFColumnsListFragment;
import co.smartreceipts.android.settings.widget.PaymentMethodsListFragment;
import co.smartreceipts.android.settings.widget.SettingsActivity;
import co.smartreceipts.android.sync.widget.backups.BackupsFragment;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.DownloadRemoteBackupImagesProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ExportBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.errors.SyncErrorFragment;
import co.smartreceipts.android.trips.TripFragment;
import co.smartreceipts.android.trips.editor.TripCreateEditFragment;
import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(
        subcomponents = {
                SmartReceiptsActivitySubcomponent.class,
                SettingsActivitySubcomponent.class,
                TripFragmentSubcomponent.class,
                TripCreateEditFragmentSubcomponent.class,
                ReceiptCreateEditFragmentSubcomponent.class,
                ReceiptImageFragmentSubcomponent.class,
                CSVColumnsListFragmentSubcomponent.class,
                PDFColumnsListFragmentSubcomponent.class,
                GenerateReportFragmentSubcomponent.class,
                ReceiptsListFragmentSubcomponent.class,
                DistanceFragmentSubcomponent.class,
                DistanceDialogFragmentSubcomponent.class,
                InformAboutPdfImageAttachmentDialogFragmentSubcomponent.class,
                BackupsFragmentSubcomponent.class,
                DeleteRemoteBackupProgressDialogFragmentSubcomponent.class,
                DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent.class,
                ExportBackupWorkerProgressDialogFragmentSubcomponent.class,
                ImportLocalBackupWorkerProgressDialogFragmentSubcomponent.class,
                ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent.class,
                ReportInfoFragmentSubcomponent.class,
                SyncErrorFragmentSubcomponent.class,
                FeedbackDialogFragmentSubcomponent.class,
                LoginFragmentSubcomponent.class,
                OcrInformationalFragmentSubcomponent.class,
                OcrInformationalTooltipFragmentSubcomponent.class,
                RatingDialogFragmentSubcomponent.class,
                PaymentMethodsListFragmentSubcomponent.class,
                CategoriesListFragmentSubcomponent.class,
                ReceiptMoveCopyDialogFragmentSubcomponent.class,
                DeleteRemoteBackupDialogFragmentSubcomponent.class,
                SelectAutomaticBackupProviderDialogFragmentSubcomponent.class
        }
)
public abstract class GlobalBindingModule {
    @Binds
    @IntoMap
    @ActivityKey(SmartReceiptsActivity.class)
    public abstract AndroidInjector.Factory<? extends Activity> smartReceiptsActivitySubcomponentBuilder(
            SmartReceiptsActivitySubcomponent.Builder builder);

    @Binds
    @IntoMap
    @ActivityKey(SettingsActivity.class)
    public abstract AndroidInjector.Factory<? extends Activity> settingsActivitySubcomponentBuilder(
            SettingsActivitySubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(TripFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> tripFragmentSubcomponentBuilder(
            TripFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(TripCreateEditFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> tripCreateEditFragmentSubcomponentBuilder(
            TripCreateEditFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptCreateEditFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptCreateEditFragmentSubcomponentBuilder(
            ReceiptCreateEditFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptImageFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptImageFragmentSubcomponentBuilder(
            ReceiptImageFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptsListFragmentSubcomponentBuilder(
            ReceiptsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(CSVColumnsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> csvColumnListFragmentSubcomponentBuilder(
            CSVColumnsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(PDFColumnsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> pdfColumnListFragmentSubcomponentBuilder(
            PDFColumnsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(GenerateReportFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> generateReportFragmentSubcomponentBuilder(
            GenerateReportFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DistanceFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> distanceFragmentBuilder(
            DistanceFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DistanceDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> distanceDialogFragmentBuilder(
            DistanceDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(InformAboutPdfImageAttachmentDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> informDialogBuilder(
            InformAboutPdfImageAttachmentDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(BackupsFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> backupsFragmentBuilder(
            BackupsFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DeleteRemoteBackupProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> deleteRemoteBackupProgressFragmentBuilder(
            DeleteRemoteBackupProgressDialogFragmentSubcomponent.Builder builder);


    @Binds
    @IntoMap
    @FragmentKey(DownloadRemoteBackupImagesProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> downloadRemoteBackupImagesProgressFragmentBuilder(
            DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ExportBackupWorkerProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> exportBackupWorkerProgressDialogFragmentBuilder(
            ExportBackupWorkerProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ImportLocalBackupWorkerProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> importLocalBackupWorkerProgressDialogFragmentBuilder(
            ImportLocalBackupWorkerProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ImportRemoteBackupWorkerProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> importRemoteBackupWorkerProgressDialogFragmentBuilder(
            ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent.Builder builder);


    @Binds
    @IntoMap
    @FragmentKey(ReportInfoFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> reportInfoFragmentBuilder(
            ReportInfoFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(SyncErrorFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> syncErrorFragmentBuilder(
            SyncErrorFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(FeedbackDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> feedbackDialogFragmentBuilder(
            FeedbackDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(LoginFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> loginFragmentBuilder(
            LoginFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(OcrInformationalFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> ocrInformationalFragmentBuilder(
            OcrInformationalFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(OcrInformationalTooltipFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> ocrInformationalTooltipFragmentBuilder(
            OcrInformationalTooltipFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(RatingDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> ratingDialogFragmentBuilder(
            RatingDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(PaymentMethodsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> paymentMethodsListFragmentBuilder(
            PaymentMethodsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(CategoriesListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> categoriesListFragmentBuilder(
            CategoriesListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptMoveCopyDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptMoveCopyDialogFragmentBuilder(
            ReceiptMoveCopyDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DeleteRemoteBackupDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> deleteRemoteBackupDialogFragmentBuilder(
            DeleteRemoteBackupDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(SelectAutomaticBackupProviderDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> selectAutomaticBackupProviderDialogFragmentBuilder(
            SelectAutomaticBackupProviderDialogFragmentSubcomponent.Builder builder);

}
