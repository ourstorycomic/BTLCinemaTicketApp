package com.example.btlcinematicketapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private final List<String> dates;
    private final List<String> daysOfWeek;
    private int selectedPosition = 0;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String dateStr);
    }

    public DateAdapter(List<String> dates, List<String> daysOfWeek, OnItemClickListener listener) {
        this.dates = dates;
        this.daysOfWeek = daysOfWeek;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvDate.setText(dates.get(position));
        holder.tvDayOfWeek.setText(daysOfWeek.get(position));

        GradientDrawable bg = (GradientDrawable) holder.llDateContainer.getBackground();
        if (position == selectedPosition) {
            bg.setStroke(3, Color.parseColor("#D81B60"));
            holder.tvDate.setTextColor(Color.parseColor("#D81B60"));
            holder.tvDayOfWeek.setBackgroundColor(Color.parseColor("#D81B60"));
            holder.tvDayOfWeek.setTextColor(Color.WHITE);
        } else {
            bg.setStroke(1, Color.parseColor("#DDDDDD"));
            holder.tvDate.setTextColor(Color.parseColor("#333333"));
            holder.tvDayOfWeek.setBackgroundColor(Color.parseColor("#F0F0F0"));
            holder.tvDayOfWeek.setTextColor(Color.parseColor("#555555"));
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            int oldPos = selectedPosition;
            selectedPosition = currentPos;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(dates.get(currentPos));
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llDateContainer;
        TextView tvDate, tvDayOfWeek;

        ViewHolder(View view) {
            super(view);
            llDateContainer = view.findViewById(R.id.llDateContainer);
            tvDate = view.findViewById(R.id.tvDate);
            tvDayOfWeek = view.findViewById(R.id.tvDayOfWeek);
        }
    }
}