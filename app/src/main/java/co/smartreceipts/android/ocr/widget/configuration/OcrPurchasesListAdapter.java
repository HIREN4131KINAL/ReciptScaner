package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.smartreceipts.android.R;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import rx.Observable;
import rx.subjects.PublishSubject;

public class OcrPurchasesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final View headerView;
    private final PublishSubject<AvailablePurchase> availablePurchaseClickSubject = PublishSubject.create();
    private List<AvailablePurchase> availablePurchases = Collections.emptyList();

    public OcrPurchasesListAdapter(@NonNull View headerView) {
        this.headerView = Preconditions.checkNotNull(headerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(headerView);
        } else {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ocr_purchase_list_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final AvailablePurchase availablePurchase = availablePurchases.get(position - 1);
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.purchaseName.setText(availablePurchase.getTitle());
            itemHolder.purchaseDescription.setText(availablePurchase.getDescription());
            itemHolder.purchasePrice.setText(availablePurchase.getPrice());
            itemHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    availablePurchaseClickSubject.onNext(availablePurchase);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return availablePurchases.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    public void setAvailablePurchases(@NonNull List<AvailablePurchase> availablePurchases) {
        this.availablePurchases = new ArrayList<>(Preconditions.checkNotNull(availablePurchases));
        notifyDataSetChanged();
    }

    @NonNull
    public Observable<AvailablePurchase> getAvailablePurchaseClicks() {
        return availablePurchaseClickSubject.asObservable();
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        final View headerView;

        HeaderViewHolder(@NonNull View view) {
            super(view);
            headerView = view;
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        View parentView;
        @BindView(R.id.purchase_name) TextView purchaseName;
        @BindView(R.id.purchase_description) TextView purchaseDescription;
        @BindView(R.id.purchase_price) TextView purchasePrice;

        ItemViewHolder(@NonNull View view) {
            super(view);
            parentView = view;
            ButterKnife.bind(this, view);
        }
    }
}
