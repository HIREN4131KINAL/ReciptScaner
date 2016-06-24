package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.math.BigDecimal;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.utils.FileUtils;
import wb.android.storage.StorageManager;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link ReceiptsTable}
 */
public final class ReceiptDatabaseAdapter implements SelectionBackedDatabaseAdapter<Receipt, PrimaryKey<Receipt, Integer>, Trip> {

    private final Table<Trip, String> mTripsTable;
    private final Table<PaymentMethod, Integer> mPaymentMethodTable;
    private final StorageManager mStorageManager;

    public ReceiptDatabaseAdapter(@NonNull Table<Trip, String> tripsTable, @NonNull Table<PaymentMethod, Integer> paymentMethodTable, @NonNull PersistenceManager persistenceManager) {
        this(tripsTable, paymentMethodTable, Preconditions.checkNotNull(persistenceManager).getStorageManager());
    }

    public ReceiptDatabaseAdapter(@NonNull Table<Trip, String> tripsTable, @NonNull Table<PaymentMethod, Integer> paymentMethodTable, @NonNull StorageManager storageManager) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mPaymentMethodTable = Preconditions.checkNotNull(paymentMethodTable);
        mStorageManager = Preconditions.checkNotNull(storageManager);
    }

    @NonNull
    @Override
    public Receipt read(@NonNull Cursor cursor) {
        final int parentIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
        final Trip trip = mTripsTable.findByPrimaryKey(cursor.getString(parentIndex)).toBlocking().first();
        return readForSelection(cursor, trip);
    }


    @NonNull
    @Override
    public Receipt readForSelection(@NonNull Cursor cursor, @NonNull Trip trip) {

        final int idIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_ID);
        final int pathIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
        final int nameIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_NAME);
        final int categoryIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY);
        final int priceIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
        final int taxIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_TAX);
        final int exchangeRateIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXCHANGE_RATE);
        final int dateIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_DATE);
        final int timeZoneIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE);
        final int commentIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
        final int expenseableIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXPENSEABLE);
        final int currencyIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
        final int fullpageIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
        final int paymentMethodIdIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID);
        final int extra_edittext_1_Index = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
        final int extra_edittext_2_Index = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
        final int extra_edittext_3_Index = cursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);

        final int id = cursor.getInt(idIndex);
        final String path = cursor.getString(pathIndex);
        final String name = cursor.getString(nameIndex);

        // TODO: Join category with categories table
        final String category = cursor.getString(categoryIndex);
        final double priceDouble = cursor.getDouble(priceIndex);
        final double taxDouble = cursor.getDouble(taxIndex);
        final double exchangeRateDouble = cursor.getDouble(exchangeRateIndex);
        final String priceString = cursor.getString(priceIndex);
        final String taxString = cursor.getString(taxIndex);
        final String exchangeRateString = cursor.getString(exchangeRateIndex);
        final long date = cursor.getLong(dateIndex);
        final String timezone = (timeZoneIndex > 0) ? cursor.getString(timeZoneIndex) : null;
        final String comment = cursor.getString(commentIndex);
        final boolean expensable = cursor.getInt(expenseableIndex) > 0;
        final String currency = cursor.getString(currencyIndex);
        final boolean fullpage = !(cursor.getInt(fullpageIndex) > 0);
        final int paymentMethodId = cursor.getInt(paymentMethodIdIndex); // Not using a join, since we need
        final String extra_edittext_1 = cursor.getString(extra_edittext_1_Index);
        final String extra_edittext_2 = cursor.getString(extra_edittext_2_Index);
        final String extra_edittext_3 = cursor.getString(extra_edittext_3_Index);
        File img = null;
        if (!TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
            img = mStorageManager.getFile(trip.getDirectory(), path);
        }
        final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(id);
        builder.setTrip(trip)
                .setName(name)
                .setCategory(category)
                .setImage(img)
                .setDate(date)
                .setTimeZone(timezone)
                .setComment(comment)
                .setIsExpenseable(expensable)
                .setCurrency(currency)
                .setIsFullPage(fullpage)
                .setIndex(cursor.getPosition() + 1)
                .setPaymentMethod(mPaymentMethodTable.findByPrimaryKey(paymentMethodId).toBlocking().first())
                .setExtraEditText1(extra_edittext_1)
                .setExtraEditText2(extra_edittext_2)
                .setExtraEditText3(extra_edittext_3);


        /**
         * Please note that a very frustrating bug exists here. Android cursors only return the first 6
         * characters of a price string if that string contains a '.' character. It returns all of them
         * if not. This means we'll break for prices over 5 digits unless we are using a comma separator, 
         * which we'd do in the EU. Stupid check below to un-break this. Stupid Android.
         *
         * TODO: Longer term, everything should be saved with a decimal point
         * https://code.google.com/p/android/issues/detail?id=22219
         */
        if (!TextUtils.isEmpty(priceString) && priceString.contains(",")) {
            builder.setPrice(priceString);
        } else {
            builder.setPrice(priceDouble);
        }
        if (!TextUtils.isEmpty(taxString) && taxString.contains(",")) {
            builder.setTax(taxString);
        } else {
            builder.setTax(taxDouble);
        }
        final ExchangeRateBuilderFactory exchangeRateBuilder = new ExchangeRateBuilderFactory().setBaseCurrency(currency);
        if (!TextUtils.isEmpty(exchangeRateString) && exchangeRateString.contains(",")) {
            exchangeRateBuilder.setRate(trip.getTripCurrency(), exchangeRateString);
        } else {
            exchangeRateBuilder.setRate(trip.getTripCurrency(), exchangeRateDouble);
        }
        builder.setExchangeRate(exchangeRateBuilder.build());

        return builder.build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull Receipt receipt) {
        final ContentValues values = new ContentValues();

        // Add core data
        values.put(ReceiptsTable.COLUMN_PARENT, receipt.getTrip().getName());
        values.put(ReceiptsTable.COLUMN_NAME, receipt.getName().trim());
        values.put(ReceiptsTable.COLUMN_CATEGORY, receipt.getCategory().getName());
        values.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime());
        values.put(ReceiptsTable.COLUMN_TIMEZONE, receipt.getTimeZone().getID());
        values.put(ReceiptsTable.COLUMN_COMMENT, receipt.getComment());
        values.put(ReceiptsTable.COLUMN_ISO4217, receipt.getPrice().getCurrencyCode());
        values.put(ReceiptsTable.COLUMN_EXPENSEABLE, receipt.isExpensable());
        values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !receipt.isFullPage());

        // Add file
        File file = renameFile(receipt.getFile());
        if (file != null) {
            values.put(ReceiptsTable.COLUMN_PATH, file.getName());
        }

        // Add payment method if one exists
        if (receipt.getPaymentMethod() != null) {
            values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, receipt.getPaymentMethod().getId());
        }

        // Note: We replace the commas here with decimals to avoid database bugs around parsing decimal values
        // TODO: Ensure this logic works for prices like "1,234.56"
        // TODO: Check if we can use doubleValue going forward like distance does here
        values.put(ReceiptsTable.COLUMN_PRICE, receipt.getPrice().getDecimalFormattedPrice().replace(",", "."));
        values.put(ReceiptsTable.COLUMN_TAX, receipt.getTax().getDecimalFormattedPrice().replace(",", "."));
        values.put(ReceiptsTable.COLUMN_EXCHANGE_RATE, receipt.getPrice().getExchangeRate().getDecimalFormattedExchangeRate(receipt.getTrip().getDefaultCurrencyCode()).replace(",", "."));

        // Add extras
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, receipt.getExtraEditText1());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, receipt.getExtraEditText2());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, receipt.getExtraEditText3());

        return values;
    }

    @NonNull
    @Override
    public Receipt build(@NonNull Receipt receipt, @NonNull PrimaryKey<Receipt, Integer> primaryKey) {
        return new ReceiptBuilderFactory(primaryKey.getPrimaryKeyValue(receipt), receipt).build();
    }

    @Nullable
    private File renameFile(@Nullable File file) {
        /**
         *         final int rcptNum = this.getReceiptsSerial(receipt.getTrip()).size() + 1; // Use this to order things more properly

         * final StringBuilder stringBuilder = new StringBuilder(rcptNum + "_");
         * stringBuilder.append(FileUtils.omitIllegalCharactersFromFileName(receipt.getName().trim()));
        File file = receipt.getFile();
        if (file != null) {
            stringBuilder.append('.').append(StorageManager.getExtension(receipt.getFile()));
            final String newName = stringBuilder.toString();
            File renamedFile = mPersistenceManager.getStorageManager().getFile(receipt.getTrip().getDirectory(), newName);
            if (!renamedFile.exists()) { // If this file doesn't exist, let's rename our current one
                Log.e(TAG, "Changing image name from: " + receipt.getFile().getName() + " to: " + newName);
                file = mPersistenceManager.getStorageManager().rename(file, newName); // Returns oldFile on failure
            }
            values.put(ReceiptsTable.COLUMN_PATH, file.getName());
        }
         */
        if (file == null) {
            return null;
        }
        throw new UnsupportedOperationException("Todo: Determine where the file renaming should take place...");
    }

}
