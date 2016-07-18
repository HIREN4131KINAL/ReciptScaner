package co.smartreceipts.android.persistence.database.defaults;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.impl.ImmutableCategoryImpl;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import wb.android.flex.Flex;

public class TableDefaultCustomizerImpl implements TableDefaultsCustomizer {

    private final Context mContext;
    private final ReceiptColumnDefinitions mReceiptColumnDefinitions;
    private final Executor mExecutor;

    public TableDefaultCustomizerImpl(@NonNull Context context, @NonNull ReceiptColumnDefinitions receiptColumnDefinitions) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
        mExecutor = Executors.newCachedThreadPool();
        
    }

    @Override
    public void insertCSVDefaults(@NonNull final CSVTable csvTable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Column<Receipt>> columns = mReceiptColumnDefinitions.getCsvDefaults();
                final int size = columns.size();
                for (int i = 0; i < size; i++) {
                    csvTable.insert(columns.get(i)).toBlocking().first();
                }
            }
        });
    }

    @Override
    public void insertPDFDefaults(@NonNull final PDFTable pdfTable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Column<Receipt>> columns = mReceiptColumnDefinitions.getPdfDefaults();
                final int size = columns.size();
                for (int i = 0; i < size; i++) {
                    pdfTable.insert(columns.get(i)).toBlocking().first();
                }
            }
        });
    }

    @Override
    public void insertCategoryDefaults(@NonNull final CategoriesTable categoriesTable) {
        final Resources resources = mContext.getResources();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_null), resources.getString(R.string.category_null_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_airfare), resources.getString(R.string.category_airfare_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_breakfast), resources.getString(R.string.category_breakfast_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_dinner), resources.getString(R.string.category_dinner_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_entertainment), resources.getString(R.string.category_entertainment_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_gasoline), resources.getString(R.string.category_gasoline_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_gift), resources.getString(R.string.category_gift_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_hotel), resources.getString(R.string.category_hotel_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_laundry), resources.getString(R.string.category_laundry_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_lunch), resources.getString(R.string.category_lunch_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_other), resources.getString(R.string.category_other_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_parking_tolls), resources.getString(R.string.category_parking_tolls_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_postage_shipping), resources.getString(R.string.category_postage_shipping_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_car_rental), resources.getString(R.string.category_car_rental_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_taxi_bus), resources.getString(R.string.category_taxi_bus_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_telephone_fax), resources.getString(R.string.category_telephone_fax_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_tip), resources.getString(R.string.category_tip_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_train), resources.getString(R.string.category_train_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_books_periodicals), resources.getString(R.string.category_books_periodicals_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_cell_phone), resources.getString(R.string.category_cell_phone_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_dues_subscriptions), resources.getString(R.string.category_dues_subscriptions_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_meals_justified), resources.getString(R.string.category_meals_justified_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_stationery_stations), resources.getString(R.string.category_stationery_stations_code))).toBlocking().first();
                categoriesTable.insert(new ImmutableCategoryImpl(resources.getString(R.string.category_training_fees), resources.getString(R.string.category_training_fees_code))).toBlocking().first();
            }
        });
    }

    @Override
    public void insertPaymentMethodDefaults(@NonNull final PaymentMethodsTable paymentMethodsTable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                paymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(mContext.getString(R.string.payment_method_default_unspecified)).build()).toBlocking().first();
                paymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(mContext.getString(R.string.payment_method_default_corporate_card)).build()).toBlocking().first();
                paymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(mContext.getString(R.string.payment_method_default_personal_card)).build()).toBlocking().first();
                paymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(mContext.getString(R.string.payment_method_default_cash)).build()).toBlocking().first();
                paymentMethodsTable.insert(new PaymentMethodBuilderFactory().setMethod(mContext.getString(R.string.payment_method_default_check)).build()).toBlocking().first();
            }
        });
    }
}
