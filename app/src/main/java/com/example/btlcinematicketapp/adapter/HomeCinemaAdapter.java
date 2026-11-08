package com.example.btlcinematicketapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.Cinema;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.List;

public class HomeCinemaAdapter extends RecyclerView.Adapter<HomeCinemaAdapter.ViewHolder> {
    private final List<Cinema> cinemas;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cinema cinema);
    }

    public HomeCinemaAdapter(List<Cinema> cinemas, OnItemClickListener listener) {
        this.cinemas = cinemas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_cinema, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.tvName.setText(cinema.name);
        holder.tvLocation.setText(cinema.location);
        holder.tvAddress.setText(cinema.address);

        if (holder.ivLogo != null) {
            holder.ivLogo.setImageResource(UiUtil.getCinemaLogoRes(holder.itemView.getContext(), cinema.brand));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(cinema));
        if (holder.btnViewShowtimes != null) {
            holder.btnViewShowtimes.setOnClickListener(v -> listener.onItemClick(cinema));
        }
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName, tvLocation, tvAddress, btnViewShowtimes;

        ViewHolder(View view) {
            super(view);
            ivLogo = view.findViewById(R.id.ivLogo);
            tvName = view.findViewById(R.id.tvName);
            tvLocation = view.findViewById(R.id.tvLocation);
            tvAddress = view.findViewById(R.id.tvAddress);
            btnViewShowtimes = view.findViewById(R.id.btnViewShowtimes);
        }
    }
}
