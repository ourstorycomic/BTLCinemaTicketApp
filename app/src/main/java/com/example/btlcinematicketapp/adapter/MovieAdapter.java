package com.example.btlcinematicketapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.VH> {
    public interface OnMovieClick { void onClick(Movie m); }

    private final List<Movie> items = new ArrayList<>();
    private final OnMovieClick listener;

    public MovieAdapter(OnMovieClick listener) { this.listener = listener; }

    public void setItems(List<Movie> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Movie m = items.get(position);
        h.poster.setImageResource(UiUtil.posterRes(h.itemView.getContext(), m.posterName));
        h.title.setText(m.title);
        h.sub.setText(m.genre + " • " + m.duration + " phút");
        h.age.setText(m.ageRating);
        h.rating.setText("★ " + m.rating);
        h.itemView.setOnClickListener(v -> listener.onClick(m));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, sub, age, rating;

        VH(View v) {
            super(v);
            poster = v.findViewById(R.id.imgPoster);
            title = v.findViewById(R.id.tvTitle);
            sub = v.findViewById(R.id.tvSub);
            age = v.findViewById(R.id.tvAge);
            rating = v.findViewById(R.id.tvRating);
        }
    }
}
