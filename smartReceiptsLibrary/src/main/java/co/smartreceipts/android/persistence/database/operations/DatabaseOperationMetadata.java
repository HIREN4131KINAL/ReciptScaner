package co.smartreceipts.android.persistence.database.operations;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class DatabaseOperationMetadata {

    private final OperationFamilyType mOperationFamilyType;

    public DatabaseOperationMetadata() {
        this(OperationFamilyType.Default);
    }

    public DatabaseOperationMetadata(@NonNull OperationFamilyType operationFamilyType) {
        mOperationFamilyType = Preconditions.checkNotNull(operationFamilyType);
    }

    @NonNull
    public OperationFamilyType getOperationFamilyType() {
        return mOperationFamilyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseOperationMetadata)) return false;

        DatabaseOperationMetadata that = (DatabaseOperationMetadata) o;

        return mOperationFamilyType == that.mOperationFamilyType;

    }

    @Override
    public int hashCode() {
        return mOperationFamilyType.hashCode();
    }
}
