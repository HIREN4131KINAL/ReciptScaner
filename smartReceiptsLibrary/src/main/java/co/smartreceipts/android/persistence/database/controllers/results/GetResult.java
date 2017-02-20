package co.smartreceipts.android.persistence.database.controllers.results;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public class GetResult<ModelType> {

    private final List<ModelType> model;
    private final Throwable throwable;

    public GetResult(@NonNull List<ModelType> model) {
        this(model, null);
    }

    public GetResult(@NonNull Throwable throwable) {
        this(null, throwable);
    }

    public GetResult(@Nullable List<ModelType> model, @Nullable Throwable throwable) {
        this.model = model;
        this.throwable = throwable;
    }

    @Nullable
    public List<ModelType> get() {
        return model;
    }

    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

}
