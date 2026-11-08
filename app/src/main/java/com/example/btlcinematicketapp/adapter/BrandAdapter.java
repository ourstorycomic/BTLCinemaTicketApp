package com.example.btlcinematicketapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {
    private final List<String> brands;
    private int selectedPosition = 0;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String brand);
    }

    public BrandAdapter(List<String> brands, OnItemClickListener listener) {
        this.brands = brands;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String brand = brands.get(position);
        holder.tvBrandName.setText(brand);

        // Load logo
        String logoName = "logo_" + brand.toLowerCase();
        int resId = holder.itemView.getContext().getResources().getIdentifier(
                logoName, "drawable", holder.itemView.getContext().getPackageName());
        if (resId != 0) {
            holder.ivLogo.setImageResource(resId);
        } else {
            holder.ivLogo.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder
        }
        
        holder.ivLogo.setImageResource(UiUtil.getCinemaLogoRes(holder.itemView.getContext(), brand));
        
        if (position == selectedPosition) {
            holder.ivLogo.setBackgroundResource(R.drawable.bg_date_item_selected);
            holder.tvBrandName.setTextColor(Color.parseColor("#E50914"));
        } else {
            holder.ivLogo.setBackgroundResource(R.drawable.bg_date_item_normal);
            holder.tvBrandName.setTextColor(Color.parseColor("#555555"));
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            int oldPos = selectedPosition;
            selectedPosition = currentPos;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(brand);
        });
    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvBrandName;

        ViewHolder(View view) {
            super(view);
            ivLogo = view.findViewById(R.id.ivLogo);
            tvBrandName = view.findViewById(R.id.tvBrandName);
        }
    }
}
