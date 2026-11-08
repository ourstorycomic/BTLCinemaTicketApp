package com.example.btlcinematicketapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.btlcinematicketapp.adapter.DateAdapter;
import com.example.btlcinematicketapp.adapter.MovieShowtimeAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Cinema;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.util.UiUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CinemaDetailActivity extends AppCompatActivity {
    
    private DatabaseHelper db;
    private long cinemaId;
    private List<String> rawDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cinema_detail);
        UiUtil.applyInsets(findViewById(R.id.root));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        
        cinemaId = getIntent().getLongExtra("cinema_id", -1);
        db = new DatabaseHelper(this);
        
        Cinema cinema = db.getCinema(cinemaId);
        if (cinema != null) {
            ((TextView) findViewById(R.id.tvCinemaNameHeader)).setText(cinema.name);
        }

        setupDates();
        setupTimeFilters();
    }

    private void setupTimeFilters() {
        List<String> filters = Arrays.asList("Tất cả", "15:00 - 18:00", "18:00 - 21:00", "21:00 - 24:00");
        RecyclerView rvTimeFilters = findViewById(R.id.rvTimeFilters);
        rvTimeFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTimeFilters.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            private int selectedPos = 0;
            @androidx.annotation.NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_filter, parent, false)) {};
            }
            @Override
            public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {
                TextView tv = (TextView) holder.itemView;
                tv.setText(filters.get(position));
                GradientDrawable bg = (GradientDrawable) tv.getBackground();
                if (position == selectedPos) {
                    tv.setTextColor(Color.WHITE);
                    bg.setColor(Color.parseColor("#D81B60"));
                    bg.setStroke(0, Color.TRANSPARENT);
                } else {
                    tv.setTextColor(Color.parseColor("#333333"));
                    bg.setColor(Color.parseColor("#F5F5F5"));
                    bg.setStroke(1, Color.parseColor("#DDDDDD"));
                }
                tv.setOnClickListener(v -> {
                    int old = selectedPos;
                    selectedPos = holder.getBindingAdapterPosition();
                    notifyItemChanged(old);
                    notifyItemChanged(selectedPos);
                });
            }
            @Override
            public int getItemCount() { return filters.size(); }
        });
    }
    
    private void setupDates() {
        rawDates = new ArrayList<>();
        List<String> displayDates = new ArrayList<>();
        List<String> displayDays = new ArrayList<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM", Locale.US);
        
        for (int i = 0; i < 7; i++) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, i);
            rawDates.add(sdf.format(c.getTime()));
            displayDates.add(sdfDisplay.format(c.getTime()));
            
            if (i == 0) displayDays.add("H.nay");
            else {
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SUNDAY) displayDays.add("CN");
                else displayDays.add("Thứ " + dayOfWeek);
            }
        }

        RecyclerView rvDates = findViewById(R.id.rvDates);
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DateAdapter dateAdapter = new DateAdapter(displayDates, displayDays, dateStr -> {
            int index = displayDates.indexOf(dateStr);
            if (index >= 0) {
                loadMoviesForDate(rawDates.get(index));
            }
        });
        rvDates.setAdapter(dateAdapter);
        
        if (!rawDates.isEmpty()) {
            loadMoviesForDate(rawDates.get(0));
        }
    }
    
    private void loadMoviesForDate(String date) {
        List<Showtime> showtimes = db.getShowtimesForCinema(cinemaId, date);
        
        // Nhóm suất chiếu theo phim
        LinkedHashMap<Long, List<Showtime>> map = new LinkedHashMap<>();
        for (Showtime st : showtimes) {
            if (!map.containsKey(st.movieId)) {
                map.put(st.movieId, new ArrayList<>());
            }
            map.get(st.movieId).add(st);
        }
        
        List<Movie> movies = new ArrayList<>();
        List<List<Showtime>> stList = new ArrayList<>();
        
        for (Long movieId : map.keySet()) {
            Movie m = db.getMovie(movieId);
            if (m != null) {
                movies.add(m);
                stList.add(map.get(movieId));
            }
        }
        
        RecyclerView rvMovies = findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        MovieShowtimeAdapter adapter = new MovieShowtimeAdapter(movies, stList, db);
        rvMovies.setAdapter(adapter);
    }
}