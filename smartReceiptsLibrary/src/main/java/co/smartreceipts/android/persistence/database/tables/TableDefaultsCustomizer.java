package co.smartreceipts.android.persistence.database.tables;

import android.support.annotation.NonNull;

public interface TableDefaultsCustomizer {

    void insertCSVDefaults(@NonNull CSVTable table);

    void insertPDFDefaults(@NonNull PDFTable table);

    void insertCategoryDefaults(@NonNull CategoriesTable table);
}
