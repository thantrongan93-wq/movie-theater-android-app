package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lab10.R;
import com.example.lab10.utils.CurrencyUtils;

import java.util.List;

public class FoodAdapter<T> extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface FoodItemBinder<T> {
        Long getId(T item);
        String getName(T item);
        Double getPrice(T item);
        String getImageUrl(T item);
        String getDescription(T item);
        int getQuantity(T item);
        void setQuantity(T item, int quantity);
    }

    public interface OnQuantityChangedListener {
        void onQuantityChanged();
    }

    private List<T> items;
    private FoodItemBinder<T> binder;
    private OnQuantityChangedListener listener;

    public FoodAdapter(List<T> items, FoodItemBinder<T> binder,
                       OnQuantityChangedListener listener) {
        this.items = items;
        this.binder = binder;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        T item = items.get(position);

        holder.tvName.setText(binder.getName(item));
        holder.tvPrice.setText(CurrencyUtils.formatPrice(binder.getPrice(item)));

        String desc = binder.getDescription(item);
        if (desc != null && !desc.isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(desc);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        Glide.with(holder.ivFood.getContext())
                .load(binder.getImageUrl(item))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivFood);

        holder.tvQuantity.setText(String.valueOf(binder.getQuantity(item)));

        holder.btnMinus.setOnClickListener(v -> {
            int current = binder.getQuantity(item);
            if (current > 0) {
                binder.setQuantity(item, current - 1);
                holder.tvQuantity.setText(String.valueOf(current - 1));
                if (listener != null) listener.onQuantityChanged();
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            int current = binder.getQuantity(item);
            binder.setQuantity(item, current + 1);
            holder.tvQuantity.setText(String.valueOf(current + 1));
            if (listener != null) listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<T> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName, tvPrice, tvDescription, tvQuantity;
        Button btnPlus, btnMinus;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood        = itemView.findViewById(R.id.iv_food);
            tvName        = itemView.findViewById(R.id.tv_food_name);
            tvPrice       = itemView.findViewById(R.id.tv_food_price);
            tvDescription = itemView.findViewById(R.id.tv_food_description);
            tvQuantity    = itemView.findViewById(R.id.tv_quantity);
            btnPlus       = itemView.findViewById(R.id.btn_plus);
            btnMinus      = itemView.findViewById(R.id.btn_minus);
        }
    }
}