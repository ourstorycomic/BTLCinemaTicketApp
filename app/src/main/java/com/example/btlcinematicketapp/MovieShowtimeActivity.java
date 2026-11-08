package com.example.btlcinematicketapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.adapter.CinemaShowtimeAdapter;
import com.example.btlcinematicketapp.adapter.DateAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Cinema;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieShowtimeActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private Movie movie;
    private List<String> rawDates;
    private RecyclerView rvCinemasWithShowtimes;
    private TextView tvNoShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_showtime);
        UiUtil.applyInsets(findViewById(R.id.root));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        db = new DatabaseHelper(this);
        movie = db.getMovie(getIntent().getLongExtra("movie_id", -1));
        if (movie == null) {
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvMovieShowtimeTitle);
        if (tvTitle != null) {
            tvTitle.setText("Lịch chiếu: " + movie.title);
        }

        rvCinemasWithShowtimes = findViewById(R.id.rvCinemasWithShowtimes);
        rvCinemasWithShowtimes.setLayoutManager(new LinearLayoutManager(this));
        rvCinemasWithShowtimes.setNestedScrollingEnabled(false);
        tvNoShow = findViewById(R.id.tvNoShow);

        setupDates();
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

        int initialSelectedIndex = 0;
        for (int i = 0; i < rawDates.size(); i++) {
            if (hasShowtimesForDate(rawDates.get(i))) {
                initialSelectedIndex = i;
                break;
            }
        }

        RecyclerView rvDates = findViewById(R.id.rvDates);
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DateAdapter dateAdapter = new DateAdapter(displayDates, displayDays, dateStr -> {
            int index = displayDates.indexOf(dateStr);
            if (index >= 0) {
                loadShowtimesForDate(rawDates.get(index));
            }
        });
        dateAdapter.setSelectedPosition(initialSelectedIndex);
        rvDates.setAdapter(dateAdapter);

        if (!rawDates.isEmpty()) {
            loadShowtimesForDate(rawDates.get(initialSelectedIndex));
        }
    }

    private boolean hasShowtimesForDate(String date) {
        List<Showtime> allShowtimesForMovie = db.getShowtimes(movie.id);
        long now = System.currentTimeMillis();
        for (Showtime st : allShowtimesForMovie) {
            if (date.equals(st.showDate) && TimeUtil.showMillis(st.showDate, st.showTime) > now) {
                return true;
            }
        }
        return false;
    }

    private void loadShowtimesForDate(String date) {
        List<Showtime> allShowtimesForMovie = db.getShowtimes(movie.id);
        List<Showtime> dayShowtimes = new ArrayList<>();

        long now = System.currentTimeMillis();
        for (Showtime st : allShowtimesForMovie) {
            if (date.equals(st.showDate) && TimeUtil.showMillis(st.showDate, st.showTime) > now) {
                dayShowtimes.add(st);
            }
        }

        if (dayShowtimes.isEmpty()) {
            tvNoShow.setVisibility(View.VISIBLE);
            rvCinemasWithShowtimes.setVisibility(View.GONE);
            return;
        }

        tvNoShow.setVisibility(View.GONE);
        rvCinemasWithShowtimes.setVisibility(View.VISIBLE);

        // Group showtimes on this day by Cinema ID
        Map<Long, List<Showtime>> cinemaMap = new LinkedHashMap<>();
        for (Showtime st : dayShowtimes) {
            if (!cinemaMap.containsKey(st.cinemaId)) {
                cinemaMap.put(st.cinemaId, new ArrayList<>());
            }
            cinemaMap.get(st.cinemaId).add(st);
        }

        List<Cinema> cinemaList = new ArrayList<>();
        List<List<Showtime>> cinemaShowtimesList = new ArrayList<>();

        for (Long cinemaId : cinemaMap.keySet()) {
            Cinema cinema = db.getCinema(cinemaId);
            if (cinema != null) {
                cinemaList.add(cinema);
                cinemaShowtimesList.add(cinemaMap.get(cinemaId));
            }
        }

        CinemaShowtimeAdapter adapter = new CinemaShowtimeAdapter(cinemaList, cinemaShowtimesList, movie.duration, db);
        rvCinemasWithShowtimes.setAdapter(adapter);
    }
}
