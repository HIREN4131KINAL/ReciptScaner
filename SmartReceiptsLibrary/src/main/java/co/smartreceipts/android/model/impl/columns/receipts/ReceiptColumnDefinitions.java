package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.comparators.ColumnNameComparator;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.SettingUserIdColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import wb.android.flex.Flex;

/**
 * Provides specific definitions for all {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.Column}
 * objects
 */
public final class ReceiptColumnDefinitions implements ColumnDefinitions<Receipt> {

    private static final String TAG = ReceiptColumnDefinitions.class.getSimpleName();

    private enum ActualDefinition {
        BLANK(R.string.column_item_blank),
        CATEGORY_CODE(R.string.column_item_category_code),
        CATEGORY_NAME(R.string.column_item_category_name),
        USER_ID(R.string.column_item_user_id),
        REPORT_NAME(R.string.column_item_report_name),
        REPORT_START_DATE(R.string.column_item_report_start_date),
        REPORT_END_DATE(R.string.column_item_report_end_date),
        REPORT_COMMENT(R.string.column_item_report_comment),
        REPORT_COST_CENTER(R.string.column_item_report_cost_center),
        IMAGE_FILE_NAME(R.string.column_item_image_file_name),
        IMAGE_PATH(R.string.column_item_image_path),
        COMMENT(R.string.RECEIPTMENU_FIELD_COMMENT),
        CURRENCY(R.string.RECEIPTMENU_FIELD_CURRENCY),
        DATE(R.string.RECEIPTMENU_FIELD_DATE),
        NAME(R.string.RECEIPTMENU_FIELD_NAME),
        PRICE(R.string.RECEIPTMENU_FIELD_PRICE),
        PRICE_EXCHANGED(R.string.column_item_converted_price_exchange_rate),
        TAX(R.string.RECEIPTMENU_FIELD_TAX),
        TAX_EXCHANGED(R.string.column_item_converted_tax_exchange_rate),
        PRICE_PLUS_TAX_EXCHANGED(R.string.column_item_converted_price_plus_tax_exchange_rate),
        EXCHANGE_RATE(R.string.column_item_exchange_rate),
        PICTURED(R.string.column_item_pictured),
        EXPENSABLE(R.string.column_item_expensable),
        INDEX(R.string.column_item_index),
        ID(R.string.column_item_id),
        PAYMENT_METHOD(R.string.column_item_payment_method),
        EXTRA_EDITTEXT_1(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1),
        EXTRA_EDITTEXT_2(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2),
        EXTRA_EDITTEXT_3(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);

        private final int mStringResId;

        ActualDefinition(@StringRes int stringResId) {
            mStringResId = stringResId;
        }

        @StringRes
        public final int getStringResId() {
            return mStringResId;
        }

    }


    private final Context mContext;
    private final DatabaseHelper mDB;
    private final Preferences mPreferences;
    private final Flex mFlex;
    private final ActualDefinition[] mActualDefinitions;

    public ReceiptColumnDefinitions(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex) {
        this(context, persistenceManager.getDatabase(), persistenceManager.getPreferences(), flex);
    }

    public ReceiptColumnDefinitions(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull Preferences preferences, Flex flex) {
        mContext = context;
        mDB = db;
        mPreferences = preferences;
        mFlex = flex;
        mActualDefinitions = ActualDefinition.values();
    }


    @Nullable
    @Override
    public Column<Receipt> getColumn(int id, @NonNull String definitionName) {
        for (int i = 0; i < mActualDefinitions.length; i++) {
            final ActualDefinition definition = mActualDefinitions[i];
            if (definitionName.equals(getColumnNameFromStringResId(definition.getStringResId()))) {
                return getColumnFromClass(id, definition, definitionName);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Column<Receipt>> getAllColumns() {
        final ArrayList<AbstractColumnImpl<Receipt>> columns = new ArrayList<>(mActualDefinitions.length);
        for (int i = 0; i < mActualDefinitions.length; i++) {
            final ActualDefinition definition = mActualDefinitions[i];
            final AbstractColumnImpl<Receipt> column = getColumnFromClass(Column.UNKNOWN_ID, definition, getColumnNameFromStringResId(definition.getStringResId()));
            if (column != null) {
                columns.add(column);
            }
        }
        Collections.sort(columns, new ColumnNameComparator<AbstractColumnImpl<Receipt>>());
        return new ArrayList<Column<Receipt>>(columns);
    }

    @NonNull
    @Override
    public Column<Receipt> getDefaultInsertColumn() {
        return new BlankColumn<>(Column.UNKNOWN_ID, getColumnNameFromStringResId(ActualDefinition.BLANK.getStringResId()));
    }

    public List<Column<Receipt>> getCsvDefaults() {
        // TODO: Re-design how these are added
        final ArrayList<Column<Receipt>> columns = new ArrayList<Column<Receipt>>();
        columns.add(getColumn(ActualDefinition.CATEGORY_CODE));
        columns.add(getColumn(ActualDefinition.NAME));
        columns.add(getColumn(ActualDefinition.PRICE));
        columns.add(getColumn(ActualDefinition.CURRENCY));
        columns.add(getColumn(ActualDefinition.DATE));
        return columns;

    }

    public List<Column<Receipt>> getPdfDefaults() {
        // TODO: Re-design how these are added
        final ArrayList<Column<Receipt>> columns = new ArrayList<Column<Receipt>>();
        columns.add(getColumn(ActualDefinition.NAME));
        columns.add(getColumn(ActualDefinition.PRICE));
        columns.add(getColumn(ActualDefinition.DATE));
        columns.add(getColumn(ActualDefinition.CATEGORY_NAME));
        columns.add(getColumn(ActualDefinition.EXPENSABLE));
        columns.add(getColumn(ActualDefinition.PICTURED));
        return columns;
    }

    private Column<Receipt> getColumn(@NonNull ActualDefinition actualDefinition) {
        return getColumnFromClass(Column.UNKNOWN_ID, actualDefinition, getColumnNameFromStringResId(actualDefinition.getStringResId()));
    }

    @Nullable
    private AbstractColumnImpl<Receipt> getColumnFromClass(int id, @NonNull ActualDefinition definition, @NonNull String definitionName) {
        if (TextUtils.isEmpty(definitionName)) {
            // Exit early if we have no name (i.e. it's an undefined extra)
            return null;
        }
        switch (definition) {
            case BLANK:
                return new BlankColumn<>(id, definitionName);
            case CATEGORY_CODE:
                return new ReceiptCategoryCodeColumn(id, definitionName);
            case CATEGORY_NAME:
                return new ReceiptCategoryNameColumn(id, definitionName);
            case USER_ID:
                return new SettingUserIdColumn<>(id, definitionName, mPreferences);
            case REPORT_NAME:
                return new ReportNameColumn(id, definitionName);
            case REPORT_START_DATE:
                return new ReportStartDateColumn(id, definitionName, mContext, mPreferences);
            case REPORT_END_DATE:
                return new ReportEndDateColumn(id, definitionName, mContext, mPreferences);
            case REPORT_COMMENT:
                return new ReportCommentColumn(id, definitionName);
            case REPORT_COST_CENTER:
                return new ReportCostCenterColumn(id, definitionName);
            case IMAGE_FILE_NAME:
                return new ReceiptFileNameColumn(id, definitionName);
            case IMAGE_PATH:
                return new ReceiptFilePathColumn(id, definitionName);
            case COMMENT:
                return new ReceiptCommentColumn(id, definitionName);
            case CURRENCY:
                return new ReceiptCurrencyCodeColumn(id, definitionName);
            case DATE:
                return new ReceiptDateColumn(id, definitionName, mContext, mPreferences);
            case NAME:
                return new ReceiptNameColumn(id, definitionName);
            case PRICE:
                return new ReceiptPriceColumn(id, definitionName);
            case PRICE_EXCHANGED:
                return new ReceiptExchangedPriceColumn(id, definitionName, mContext);
            case TAX:
                return new ReceiptTaxColumn(id, definitionName);
            case TAX_EXCHANGED:
                return new ReceiptExchangedTaxColumn(id, definitionName, mContext);
            case PRICE_PLUS_TAX_EXCHANGED:
                return new ReceiptNetExchangedPricePlusTaxColumn(id, definitionName, mContext, mPreferences);
            case EXCHANGE_RATE:
                return new ReceiptExchangeRateColumn(id, definitionName);
            case PICTURED:
                return new ReceiptIsPicturedColumn(id, definitionName, mContext);
            case EXPENSABLE:
                return new ReceiptIsExpensableColumn(id, definitionName, mContext);
            case INDEX:
                return new ReceiptIndexColumn(id, definitionName);
            case ID:
                return new ReceiptIdColumn(id, definitionName);
            case PAYMENT_METHOD:
                return new ReceiptPaymentMethodColumn(id, definitionName);
            case EXTRA_EDITTEXT_1:
                return new ReceiptExtra1Column(id, definitionName);
            case EXTRA_EDITTEXT_2:
                return new ReceiptExtra2Column(id, definitionName);
            case EXTRA_EDITTEXT_3:
                return new ReceiptExtra3Column(id, definitionName);
            default:
                throw new IllegalArgumentException("Unknown definition type: " + definition);
        }
    }

    private String getColumnNameFromStringResId(int stringResId) {
        if (mFlex != null) {
            return mFlex.getString(mContext, stringResId);
        } else {
            return mContext.getString(stringResId);
        }
    }

}
