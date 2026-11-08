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

import java.util.List;

public class HorizontalMovieAdapter extends RecyclerView.Adapter<HorizontalMovieAdapter.ViewHolder> {
    private final List<Movie> movies;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public HorizontalMovieAdapter(List<Movie> movies, OnItemClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_now_showing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);
        
        String rating = movie.ageRating.replace("T", "");
        if (rating.equals("P")) rating = "ALL";
        holder.tvAgeRating.setText(rating + "+");
        
        holder.tvTitle.setText(movie.title);
        
        int resId = holder.itemView.getContext().getResources().getIdentifier(
                movie.posterName, "drawable", holder.itemView.getContext().getPackageName());
        if (resId != 0) {
            holder.ivPoster.setImageResource(resId);
        } else {
            holder.ivPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvAgeRating, tvTitle;

        ViewHolder(View view) {
            super(view);
            ivPoster = view.findViewById(R.id.ivPoster);
            tvAgeRating = view.findViewById(R.id.tvAgeRating);
            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }
}