package com.example.btlcinematicketapp.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.MovieDetailActivity;
import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.SeatActivity;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovieShowtimeAdapter extends RecyclerView.Adapter<MovieShowtimeAdapter.ViewHolder> {
    private final List<Movie> movies;
    private final List<List<Showtime>> showtimesList;
    private final DatabaseHelper db;

    public MovieShowtimeAdapter(List<Movie> movies, List<List<Showtime>> showtimesList, DatabaseHelper db) {
        this.movies = movies;
        this.showtimesList = showtimesList;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_with_showtimes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);
        List<Showtime> showtimes = showtimesList.get(position);

        holder.tvMovieTitle.setText(movie.title);
        
        String rating = movie.ageRating.replace("T", "");
        if (rating.equals("P")) rating = "ALL";
        holder.tvAgeRating.setText(rating + "+");

        String info = String.format("%s | %d giờ %d phút", movie.genre, movie.duration / 60, movie.duration % 60);
        holder.tvMovieInfo.setText(info);

        int resId = holder.itemView.getContext().getResources().getIdentifier(
                movie.posterName, "drawable", holder.itemView.getContext().getPackageName());
        if (resId != 0) {
            holder.ivPoster.setImageResource(resId);
        } else {
            holder.ivPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        
        holder.tvFormat.setText("2D Phụ đề"); // Giả sử

        holder.rvShowtimes.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 2));
        ShowtimeAdapter adapter = new ShowtimeAdapter(showtimes, movie.duration, db);
        holder.rvShowtimes.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvAgeRating, tvMovieInfo, tvFormat;
        ImageView ivPoster;
        RecyclerView rvShowtimes;

        ViewHolder(View view) {
            super(view);
            tvMovieTitle = view.findViewById(R.id.tvMovieTitle);
            tvAgeRating = view.findViewById(R.id.tvAgeRating);
            tvMovieInfo = view.findViewById(R.id.tvMovieInfo);
            tvFormat = view.findViewById(R.id.tvFormat);
            ivPoster = view.findViewById(R.id.ivPoster);
            rvShowtimes = view.findViewById(R.id.rvShowtimes);
        }
    }

    // --- Inner Adapter for Showtimes Grid ---
    class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.STViewHolder> {
        private final List<Showtime> showtimes;
        private final int movieDuration;
        private final DatabaseHelper db;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        public ShowtimeAdapter(List<Showtime> showtimes, int movieDuration, DatabaseHelper db) {
            this.showtimes = showtimes;
            this.movieDuration = movieDuration;
            this.db = db;
        }

        @NonNull
        @Override
        public STViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_button, parent, false);
            return new STViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull STViewHolder holder, int position) {
            Showtime st = showtimes.get(position);
            holder.tvShowTime.setText(st.showTime);

            try {
                Date d = timeFormat.parse(st.showTime);
                Calendar cal = Calendar.getInstance();
                if (d != null) {
                    cal.setTime(d);
                    cal.add(Calendar.MINUTE, movieDuration);
                    holder.tvEndTime.setText("~" + timeFormat.format(cal.getTime()));
                }
            } catch (ParseException ignored) {}

            int booked = db.getBookedSeats(st.id).size();
            int remaining = st.totalSeats - booked;
            holder.tvRemainingSeats.setText("Còn " + remaining + "/" + st.totalSeats);

            if (remaining < st.totalSeats * 0.2) {
                // Sắp hết chỗ -> màu cam nhạt
                holder.tvRemainingSeats.setBackgroundColor(Color.parseColor("#FFF3E0"));
                holder.tvRemainingSeats.setTextColor(Color.parseColor("#E65100"));
            } else {
                holder.tvRemainingSeats.setBackgroundColor(Color.parseColor("#F9F9F9"));
                holder.tvRemainingSeats.setTextColor(Color.parseColor("#777777"));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), SeatActivity.class);
                intent.putExtra("showtime_id", st.id);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return showtimes.size();
        }

        class STViewHolder extends RecyclerView.ViewHolder {
            LinearLayout llShowtimeContainer;
            TextView tvShowTime, tvEndTime, tvRemainingSeats;

            STViewHolder(View view) {
                super(view);
                llShowtimeContainer = view.findViewById(R.id.llShowtimeContainer);
                tvShowTime = view.findViewById(R.id.tvShowTime);
                tvEndTime = view.findViewById(R.id.tvEndTime);
                tvRemainingSeats = view.findViewById(R.id.tvRemainingSeats);
            }
        }
    }
}