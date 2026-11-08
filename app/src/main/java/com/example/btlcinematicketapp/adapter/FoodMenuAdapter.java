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

public class FoodMenuAdapter extends RecyclerView.Adapter<FoodMenuAdapter.ViewHolder> {
    private final List<FoodItem> foods;

    public FoodMenuAdapter(List<FoodItem> foods) {
        this.foods = foods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem food = foods.get(position);
        holder.tvEmoji.setText(food.emoji);
        holder.tvFoodName.setText(food.name);
        holder.tvFoodDesc.setText(food.description);
        holder.tvFoodPrice.setText(TimeUtil.money(food.price));

        holder.itemView.setOnClickListener(v -> showFoodDetailDialog(v.getContext(), food));
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
    public int getItemCount() {
        return foods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvFoodName, tvFoodDesc, tvFoodPrice;

        ViewHolder(View view) {
            super(view);
            tvEmoji = view.findViewById(R.id.tvEmoji);
            tvFoodName = view.findViewById(R.id.tvFoodName);
            tvFoodDesc = view.findViewById(R.id.tvFoodDesc);
            tvFoodPrice = view.findViewById(R.id.tvFoodPrice);
        }
    }
}
