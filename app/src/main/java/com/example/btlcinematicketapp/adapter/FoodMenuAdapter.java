package com.example.btlcinematicketapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.FoodItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
        
        String priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(food.price) + " đ";
        holder.tvFoodPrice.setText(priceFormatted);
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