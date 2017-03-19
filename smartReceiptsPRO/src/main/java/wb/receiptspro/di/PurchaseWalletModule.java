package wb.receiptspro.di;

import co.smartreceipts.android.purchases.wallet.ProPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Module;
import dagger.Provides;

@Module
public class PurchaseWalletModule {
    @Provides
    public static PurchaseWallet providePurchaseWallet (ProPurchaseWallet proPurchaseWallet) {
        return proPurchaseWallet;
    }
}
