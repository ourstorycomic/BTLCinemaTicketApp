package com.example.btlcinematicketapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.FoodItem;
import com.example.btlcinematicketapp.util.TimeUtil;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {
    public interface OnChange { void onChange(); }

    private final List<FoodItem> foods;
    private final int[] qty;
    private final OnChange listener;

    public FoodAdapter(List<FoodItem> foods, OnChange listener) {
        this.foods = foods;
        this.qty = new int[foods.size()];
        this.listener = listener;
    }

    public int total() {
        int t = 0;
        for (int i = 0; i < qty.length; i++) t += qty[i] * foods.get(i).price;
        return t;
    }

    public int count() {
        int n = 0;
        for (int q : qty) n += q;
        return n;
    }

    public long[] selectedIds() {
        int n = 0;
        for (int q : qty) if (q > 0) n++;
        long[] ids = new long[n];
        int k = 0;
        for (int i = 0; i < qty.length; i++) if (qty[i] > 0) ids[k++] = foods.get(i).id;
        return ids;
    }

    public int[] selectedQty() {
        int n = 0;
        for (int q : qty) if (q > 0) n++;
        int[] out = new int[n];
        int k = 0;
        for (int q : qty) if (q > 0) out[k++] = q;
        return out;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FoodItem f = foods.get(position);
        h.emoji.setText(f.emoji);
        h.name.setText(f.name);
        h.desc.setText(f.category + " • " + f.description);
        h.price.setText(TimeUtil.money(f.price));
        h.qty.setText(String.valueOf(qty[position]));

        h.itemView.setOnClickListener(v -> showFoodDetailDialog(v.getContext(), f));

        h.plus.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION || qty[p] >= 10) return;
            qty[p]++;
            h.qty.setText(String.valueOf(qty[p]));
            listener.onChange();
        });
        h.minus.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p == RecyclerView.NO_POSITION || qty[p] <= 0) return;
            qty[p]--;
            h.qty.setText(String.valueOf(qty[p]));
            listener.onChange();
        });
    }

    private void showFoodDetailDialog(Context context, FoodItem food) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_food_detail, null);

        TextView tvEmoji = view.findViewById(R.id.tvDetailEmoji);
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView btnClose = view.findViewById(R.id.btnDetailClose);

        tvEmoji.setText(food.emoji != null ? food.emoji : "🍿");
        tvName.setText(food.name);
        tvCategory.setText(food.category != null ? food.category : "Món ăn / Combo");
        tvDesc.setText(food.description != null ? food.description : "Khẩu phần thơm ngon phục vụ khi xem phim.");
        tvPrice.setText(TimeUtil.money(food.price));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() { return foods.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView emoji, name, desc, price, qty, plus, minus;

        VH(View v) {
            super(v);
            emoji = v.findViewById(R.id.tvEmoji);
            name = v.findViewById(R.id.tvName);
            desc = v.findViewById(R.id.tvDesc);
            price = v.findViewById(R.id.tvPrice);
            qty = v.findViewById(R.id.tvQty);
            plus = v.findViewById(R.id.btnPlus);
            minus = v.findViewById(R.id.btnMinus);
        }
    }
}
