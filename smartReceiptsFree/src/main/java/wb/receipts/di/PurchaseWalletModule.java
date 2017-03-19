package wb.receipts.di;

import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Module;
import dagger.Provides;

@Module
public class PurchaseWalletModule {
    @Provides
    public static PurchaseWallet provideSubscriptionCache (DefaultPurchaseWallet defaultPurchaseWallet) {
        return defaultPurchaseWallet;
    }
}
