package co.smartreceipts.android.analytics.events;

public final class Events {

    private enum Category implements Event.Category {
        Purchases, Navigation, Reports, Receipts, Distance, Generate, Ratings, Informational, Sync, Ocr
    }

    public static final class Purchases {
        public static final Event PurchaseSuccess = new DefaultEvent(Category.Purchases, "PurchaseSuccess");
        public static final Event PurchaseFailed = new DefaultEvent(Category.Purchases, "PurchaseFailed");
        public static final Event ShowPurchaseIntent = new DefaultEvent(Category.Purchases, "ShowPurchaseIntent");
        public static final Event AdUpsellShown = new DefaultEvent(Category.Purchases, "AdUpsellShown");
        public static final Event AdUpsellShownOnFailure = new DefaultEvent(Category.Purchases, "AdUpsellShownOnFailure");
        public static final Event AdUpsellTapped = new DefaultEvent(Category.Purchases, "AdUpsellTapped");
    }

    public static final class Navigation {
        public static final Event SettingsOverflow = new DefaultEvent(Category.Navigation, "SettingsOverflow");
        public static final Event BackupOverflow = new DefaultEvent(Category.Navigation, "BackupOverflow");
        public static final Event SmartReceiptsPlusOverflow = new DefaultEvent(Category.Navigation, "SmartReceiptsPlusOverflow");
    }

    public static final class Reports {
        public static final Event PersistNewReport = new DefaultEvent(Category.Reports, "PersistNewReport");
        public static final Event PersistUpdateReport = new DefaultEvent(Category.Reports, "PersistUpdateReport");
    }

    public static final class Receipts {
        public static final Event AddPictureReceipt = new DefaultEvent(Category.Receipts, "AddPictureReceipt");
        public static final Event AddTextReceipt = new DefaultEvent(Category.Receipts, "AddTextReceipt");
        public static final Event ImportPictureReceipt = new DefaultEvent(Category.Receipts, "ImportPictureReceipt");
        public static final Event ReceiptMenuEdit = new DefaultEvent(Category.Receipts, "ReceiptMenuEdit");
        public static final Event ReceiptMenuRetakePhoto = new DefaultEvent(Category.Receipts, "ReceiptMenuRetakePhoto");
        public static final Event ReceiptMenuViewPdf = new DefaultEvent(Category.Receipts, "ReceiptMenuViewPdf");
        public static final Event ReceiptMenuViewImage = new DefaultEvent(Category.Receipts, "ReceiptMenuViewImage");
        public static final Event ReceiptMenuDelete = new DefaultEvent(Category.Receipts, "ReceiptMenuDelete");
        public static final Event ReceiptMenuMoveCopy = new DefaultEvent(Category.Receipts, "ReceiptMenuMoveCopy");
        public static final Event ReceiptMenuSwapUp = new DefaultEvent(Category.Receipts, "ReceiptMenuSwapUp");
        public static final Event ReceiptMenuSwapDown = new DefaultEvent(Category.Receipts, "ReceiptMenuSwapDown");

        public static final Event ReceiptImageViewRotateCcw = new DefaultEvent(Category.Receipts, "ReceiptImageViewRotateCcw");
        public static final Event ReceiptImageViewRotateCw = new DefaultEvent(Category.Receipts, "ReceiptImageViewRotateCw");
        public static final Event ReceiptImageViewRetakePhoto = new DefaultEvent(Category.Receipts, "ReceiptImageViewRetakePhoto");

        public static final Event PersistNewReceipt = new DefaultEvent(Category.Receipts, "PersistNewReceipt");
        public static final Event PersistUpdateReceipt = new DefaultEvent(Category.Receipts, "PersistUpdateReceipt");
        public static final Event RequestExchangeRate = new DefaultEvent(Category.Receipts, "RequestExchangeRate");
        public static final Event RequestExchangeRateSuccess = new DefaultEvent(Category.Receipts, "RequestExchangeRateSuccess");
        public static final Event RequestExchangeRateFailed = new DefaultEvent(Category.Receipts, "RequestExchangeRateFailed");
        public static final Event RequestExchangeRateFailedWithNull = new DefaultEvent(Category.Receipts, "RequestExchangeRateFailedWithNull");
    }

    public static final class Distance {
        public static final Event PersistNewDistance = new DefaultEvent(Category.Distance, "PersistNewDistance");
        public static final Event PersistUpdateDistance = new DefaultEvent(Category.Distance, "PersistUpdateDistance");
    }

    public static final class Generate {
        public static final Event GenerateReports = new DefaultEvent(Category.Generate, "GenerateReports");
        public static final Event FullPdfReport = new DefaultEvent(Category.Generate, "FullPdfReport");
        public static final Event ImagesPdfReport = new DefaultEvent(Category.Generate, "ImagesPdfReport");
        public static final Event CsvReport = new DefaultEvent(Category.Generate, "CsvReport");
        public static final Event StampedZipReport = new DefaultEvent(Category.Generate, "StampedZipReport");
    }

    public static final class Ratings {
        public static final Event RatingPromptShown = new DefaultEvent(Category.Ratings, "RatingPromptShown");
        public static final Event UserSelectedRate = new DefaultEvent(Category.Ratings, "UserSelectedRate");
        public static final Event UserSelectedNever = new DefaultEvent(Category.Ratings, "UserSelectedNever");
        public static final Event UserSelectedLater = new DefaultEvent(Category.Ratings, "UserSelectedLater");
    }

    public static final class Informational {
        public static final Event ConfigureReport = new DefaultEvent(Category.Informational, "ConfigureReport");
    }

    public static final class Sync {
        public static final Event DisplaySyncError = new DefaultEvent(Category.Sync, "DisplaySyncError");
        public static final Event ClickSyncError = new DefaultEvent(Category.Sync, "ClickSyncError");
    }

    public static final class Ocr {
        public static final Event OcrQuestionnaireTooltipShown = new DefaultEvent(Category.Sync, "OcrQuestionnaireTooltipShown");
        public static final Event OcrQuestionnaireTooltipOpen = new DefaultEvent(Category.Sync, "OcrQuestionnaireTooltipOpen");
        public static final Event OcrQuestionnaireTooltipDismiss = new DefaultEvent(Category.Sync, "OcrQuestionnaireTooltipDismiss");
        public static final Event OcrQuestionnaireSubmit = new DefaultEvent(Category.Sync, "OcrQuestionnaireSubmit");
        public static final Event OcrQuestionnaireDismiss = new DefaultEvent(Category.Sync, "OcrQuestionnaireDismiss");
        public static final Event OcrQuestionnaireEmailUs = new DefaultEvent(Category.Sync, "OcrQuestionnaireEmailUs");
        public static final Event OcrQuestionnaireQuestion1PerReceipt20 = new DefaultEvent(Category.Sync, "OcrQuestionnaireQuestion1PerReceipt20");
        public static final Event OcrQuestionnaireQuestion1PerReceipt15 = new DefaultEvent(Category.Sync, "OcrQuestionnaireQuestion1PerReceipt15");
        public static final Event OcrQuestionnaireQuestion1TooMuch = new DefaultEvent(Category.Sync, "OcrQuestionnaireQuestion1TooMuch");
        public static final Event OcrQuestionnaireQuestion1NotInterested = new DefaultEvent(Category.Sync, "OcrQuestionnaireQuestion1NotInterested");
        public static final Event OcrQuestionnaireQuestion2DelaysOkay = new DefaultEvent(Category.Sync, "OcrQuestionnaireQuestion2DelaysOkay");
        public static final Event OcrQuestionnaireQuestion2NotInterested = new DefaultEvent(Category.Sync, "OcrQuestionnaireQuestion2NotInterested");
    }

}
