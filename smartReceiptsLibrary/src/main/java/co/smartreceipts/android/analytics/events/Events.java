package co.smartreceipts.android.analytics.events;

public final class Events {

    private enum Category implements Event.Category {
        Purchases, Navigation, Reports, Receipts, Distance, Generate
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
        public static final Event SettingsOverflow = new DefaultEvent(Category.Purchases, "SettingsOverflow");
        public static final Event BackupOverflow = new DefaultEvent(Category.Purchases, "BackupOverflow");
        public static final Event SmartReceiptsPlusOverflow = new DefaultEvent(Category.Purchases, "SmartReceiptsPlusOverflow");
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
        public static final Event FullPdfReport = new DefaultEvent(Category.Generate, "FullPdfReport");
        public static final Event ImagesPdfReport = new DefaultEvent(Category.Generate, "ImagesPdfReport");
        public static final Event CsvReport = new DefaultEvent(Category.Generate, "CsvReport");
        public static final Event StampedZipReport = new DefaultEvent(Category.Generate, "StampedZipReport");
    }

}
