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

public class FeaturedMovieAdapter extends RecyclerView.Adapter<FeaturedMovieAdapter.ViewHolder> {
    private final List<Movie> movies;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public FeaturedMovieAdapter(List<Movie> movies, OnItemClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_featured, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        
        // Remove 'T' from "T16" to display "16+" instead
        String rating = movie.ageRating.replace("T", "");
        if (rating.equals("P")) rating = "ALL";
        holder.tvAgeRating.setText(rating + "+");
        
        holder.tvRating.setText(movie.rating + "/10");
        holder.tvTitle.setText(movie.title);
        holder.tvGenre.setText(movie.genre);
        
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
        TextView tvAgeRating, tvRank, tvRating, tvTitle, tvGenre;

        ViewHolder(View view) {
            super(view);
            ivPoster = view.findViewById(R.id.ivPoster);
            tvAgeRating = view.findViewById(R.id.tvAgeRating);
            tvRank = view.findViewById(R.id.tvRank);
            tvRating = view.findViewById(R.id.tvRating);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvGenre = view.findViewById(R.id.tvGenre);
        }
    }
}