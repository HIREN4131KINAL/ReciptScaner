package co.smartreceipts.android.workers.reports.pdf.renderer.formatting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class RenderingFormatting {

    private final Map<Class<? extends Formatting>, Formatting<?>> FormattingMap = new HashMap<>();

    public void addFormatting(@NonNull Formatting<?> Formatting) {
        Preconditions.checkNotNull(Formatting);
        this.FormattingMap.put(Formatting.getClass(), Formatting);
    }

    public boolean hasFormatting(@NonNull Class<? extends Formatting> type) {
        Preconditions.checkNotNull(type);
        return FormattingMap.containsKey(type);
    }

    @Nullable
    public <T> T getFormatting(@NonNull Class<? extends Formatting<T>> type) {
        Preconditions.checkNotNull(type);
        final Formatting<?> Formatting = FormattingMap.get(type);
        if (Formatting != null) {
            return (T) Formatting.value();
        } else {
            return null;
        }
    }

    @NonNull
    public <T> T getFormatting(@NonNull Class<? extends Formatting<T>> type, @NonNull T defaultValue) {
        Preconditions.checkNotNull(type);
        final Formatting<?> Formatting = FormattingMap.get(type);
        if (Formatting != null) {
            return (T) Formatting.value();
        } else {
            return defaultValue;
        }
    }
}
