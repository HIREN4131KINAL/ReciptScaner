package co.smartreceipts.android.workers.reports.pdf.renderer.formatting;

import android.support.annotation.NonNull;

public class Alignment extends AbstractFormatting<Alignment.Type> {

    public enum Type {
        Centered
    }

    public Alignment(@NonNull Type alignment) {
        super(alignment, Alignment.Type.class);
    }
}
