package com.example.btlcinematicketapp.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.SeatActivity;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Cinema;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CinemaShowtimeAdapter extends RecyclerView.Adapter<CinemaShowtimeAdapter.ViewHolder> {
    private final List<Cinema> cinemas;
    private final List<List<Showtime>> showtimesList;
    private final int movieDuration;
    private final DatabaseHelper db;

    public CinemaShowtimeAdapter(List<Cinema> cinemas, List<List<Showtime>> showtimesList, int movieDuration, DatabaseHelper db) {
        this.cinemas = cinemas;
        this.showtimesList = showtimesList;
        this.movieDuration = movieDuration;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema_with_showtimes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        List<Showtime> showtimes = showtimesList.get(position);

        holder.tvCinemaName.setText(cinema.name);
        
        String address = cinema.address;
        if (cinema.location != null && !cinema.location.isEmpty()) {
            address = address + " • " + cinema.location;
        }
        holder.tvCinemaAddress.setText(address != null ? address : "");

        if (holder.ivLogo != null) {
            holder.ivLogo.setImageResource(UiUtil.getCinemaLogoRes(holder.itemView.getContext(), cinema.brand));
        }

        if (!TextUtils.isEmpty(cinema.facilities)) {
            holder.tvCinemaFacilities.setVisibility(View.VISIBLE);
            holder.tvCinemaFacilities.setText("Tiện ích: " + cinema.facilities);
        } else {
            holder.tvCinemaFacilities.setVisibility(View.GONE);
        }

        holder.rvCinemaShowtimes.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 2));
        holder.rvCinemaShowtimes.setNestedScrollingEnabled(false);
        holder.rvCinemaShowtimes.setAdapter(new STGridAdapter(showtimes, movieDuration, db));
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvCinemaName, tvCinemaAddress, tvCinemaFacilities;
        RecyclerView rvCinemaShowtimes;

        ViewHolder(View view) {
            super(view);
            ivLogo = view.findViewById(R.id.ivLogo);
            tvCinemaName = view.findViewById(R.id.tvCinemaName);
            tvCinemaAddress = view.findViewById(R.id.tvCinemaAddress);
            tvCinemaFacilities = view.findViewById(R.id.tvCinemaFacilities);
            rvCinemaShowtimes = view.findViewById(R.id.rvCinemaShowtimes);
        }
    }

    static class STGridAdapter extends RecyclerView.Adapter<STGridAdapter.STViewHolder> {
        private final List<Showtime> showtimes;
        private final int movieDuration;
        private final DatabaseHelper db;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        public STGridAdapter(List<Showtime> showtimes, int movieDuration, DatabaseHelper db) {
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

            if (holder.tvFormatPrice != null) {
                holder.tvFormatPrice.setText(st.format + " • " + TimeUtil.money(st.price));
            }

            int booked = db.getBookedSeats(st.id).size();
            int remaining = st.totalSeats - booked;
            holder.tvRemainingSeats.setText("Còn " + remaining + "/" + st.totalSeats);

            if (remaining < st.totalSeats * 0.2) {
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

        static class STViewHolder extends RecyclerView.ViewHolder {
            TextView tvShowTime, tvEndTime, tvFormatPrice, tvRemainingSeats;

            STViewHolder(View view) {
                super(view);
                tvShowTime = view.findViewById(R.id.tvShowTime);
                tvEndTime = view.findViewById(R.id.tvEndTime);
                tvFormatPrice = view.findViewById(R.id.tvFormatPrice);
                tvRemainingSeats = view.findViewById(R.id.tvRemainingSeats);
            }
        }
    }
}
