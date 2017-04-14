package co.smartreceipts.android.purchases.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class InAppPurchaseTest {

    @Test
    public void getters() {
        assertEquals(InAppPurchase.SmartReceiptsPlus.getSku(), "pro_sku_3");
        assertEquals(InAppPurchase.SmartReceiptsPlus.getType(), Subscription.class);
        assertEquals(InAppPurchase.SmartReceiptsPlus.getProductType(), "subs");
        assertEquals(InAppPurchase.SmartReceiptsPlus.getPurchaseFamily(), PurchaseFamily.SmartReceiptsPlus);

        assertEquals(InAppPurchase.OcrScans50.getSku(), "ocr_purchase_1");
        assertEquals(InAppPurchase.OcrScans50.getType(), ConsumablePurchase.class);
        assertEquals(InAppPurchase.OcrScans50.getProductType(), "inapp");
        assertEquals(InAppPurchase.OcrScans50.getPurchaseFamily(), PurchaseFamily.Ocr);

        assertEquals(InAppPurchase.OcrScans10.getSku(), "ocr_purchase_10");
        assertEquals(InAppPurchase.OcrScans10.getType(), ConsumablePurchase.class);
        assertEquals(InAppPurchase.OcrScans10.getProductType(), "inapp");
        assertEquals(InAppPurchase.OcrScans10.getPurchaseFamily(), PurchaseFamily.Ocr);
    }

    @Test
    public void from() {
        assertEquals(InAppPurchase.SmartReceiptsPlus, InAppPurchase.from("pro_sku_3"));
        assertEquals(InAppPurchase.OcrScans50, InAppPurchase.from("ocr_purchase_1"));
        assertEquals(InAppPurchase.OcrScans10, InAppPurchase.from("ocr_purchase_10"));
    }

    @Test
    public void getConsumablePurchaseSkus() {
        final List<String> purchases = Arrays.asList("ocr_purchase_10", "ocr_purchase_1");
        assertEquals(InAppPurchase.getConsumablePurchaseSkus(), purchases);
    }

    @Test
    public void getSubscriptionSkus() {
        final List<String> purchases = Collections.singletonList("pro_sku_3");
        assertEquals(InAppPurchase.getSubscriptionSkus(), purchases);
    }

}