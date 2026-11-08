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

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.ViewHolder> {
    private final List<Cinema> cinemas;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cinema cinema);
    }

    public CinemaAdapter(List<Cinema> cinemas, OnItemClickListener listener) {
        this.cinemas = cinemas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.tvCinemaName.setText(cinema.name);
        holder.tvCinemaLocation.setText(cinema.location);
        holder.tvCinemaAddress.setText(cinema.address);

        if (holder.ivLogo != null) {
            holder.ivLogo.setImageResource(UiUtil.getCinemaLogoRes(holder.itemView.getContext(), cinema.brand));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(cinema));
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvCinemaName, tvCinemaLocation, tvCinemaAddress;

        ViewHolder(View view) {
            super(view);
            ivLogo = view.findViewById(R.id.ivLogo);
            tvCinemaName = view.findViewById(R.id.tvCinemaName);
            tvCinemaLocation = view.findViewById(R.id.tvCinemaLocation);
            tvCinemaAddress = view.findViewById(R.id.tvCinemaAddress);
        }
    }
}
