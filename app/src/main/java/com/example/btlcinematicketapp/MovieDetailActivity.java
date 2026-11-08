package com.example.btlcinematicketapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MovieDetailActivity extends AppCompatActivity {
    private static final int RED = 0xFFE50914, GREY = 0xFF3A3A3A;
    private static final String[] DOW = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

    private final Map<String, List<Showtime>> byDate = new LinkedHashMap<>();
    private final List<MaterialButton> dateButtons = new ArrayList<>();
    private LinearLayout dateContainer;
    private GridLayout gridShowtimes;
    private TextView tvNoShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_detail);
        UiUtil.applyInsets(findViewById(R.id.root));

        DatabaseHelper db = new DatabaseHelper(this);
        Movie m = db.getMovie(getIntent().getLongExtra("movie_id", -1));
        if (m == null) {
            finish();
            return;
        }

        ((ImageView) findViewById(R.id.imgPoster)).setImageResource(UiUtil.posterRes(this, m.posterName));
        ((TextView) findViewById(R.id.tvTitle)).setText(m.title);
        ((TextView) findViewById(R.id.tvAge)).setText(m.ageRating);
        ((TextView) findViewById(R.id.tvMeta)).setText(m.genre + " • " + m.duration + " phút • ★ " + m.rating);
        ((TextView) findViewById(R.id.tvDirector)).setText("Đạo diễn: " + m.director);
        ((TextView) findViewById(R.id.tvCast)).setText("Diễn viên: " + m.cast);
        ((TextView) findViewById(R.id.tvLang)).setText("Ngôn ngữ: " + m.language);
        ((TextView) findViewById(R.id.tvRelease)).setText("Khởi chiếu: " + TimeUtil.fmtDate(m.releaseDate));
        ((TextView) findViewById(R.id.tvDesc)).setText(m.description);

        dateContainer = findViewById(R.id.dateContainer);
        gridShowtimes = findViewById(R.id.gridShowtimes);
        tvNoShow = findViewById(R.id.tvNoShow);

        // Gom suất chưa chiếu theo ngày
        for (Showtime s : db.getShowtimes(m.id)) {
            if (TimeUtil.showMillis(s.showDate, s.showTime) <= System.currentTimeMillis()) continue;
            if (!byDate.containsKey(s.showDate)) byDate.put(s.showDate, new ArrayList<>());
            byDate.get(s.showDate).add(s);
        }

        if (byDate.isEmpty()) {
            tvNoShow.setVisibility(View.VISIBLE);
            return;
        }
        buildDateChips();
        selectDate(0);
    }

    private void buildDateChips() {
        int margin = UiUtil.dp(this, 4);
        for (String date : byDate.keySet()) {
            String[] p = date.split("-");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));

            MaterialButton b = new MaterialButton(this);
            b.setText(DOW[c.get(Calendar.DAY_OF_WEEK) - 1] + "\n" + p[2] + "/" + p[1]);
            b.setAllCaps(false);
            b.setTextColor(Color.WHITE);
            b.setBackgroundTintList(ColorStateList.valueOf(GREY));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    UiUtil.dp(this, 68), UiUtil.dp(this, 58));
            lp.setMargins(margin, 0, margin, 0);
            b.setLayoutParams(lp);

            final int index = dateButtons.size();
            b.setOnClickListener(v -> selectDate(index));
            dateButtons.add(b);
            dateContainer.addView(b);
        }
    }

    private void selectDate(int index) {
        for (int i = 0; i < dateButtons.size(); i++) {
            dateButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(i == index ? RED : GREY));
        }
        String date = new ArrayList<>(byDate.keySet()).get(index);
        gridShowtimes.removeAllViews();
        int margin = UiUtil.dp(this, 4);

        for (Showtime s : byDate.get(date)) {
            MaterialButton b = new MaterialButton(this, null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            b.setText(s.showTime + "\n" + s.format + " • " + s.room + "\n" + TimeUtil.money(s.price));
            b.setAllCaps(false);
            b.setTextSize(12);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.height = UiUtil.dp(this, 84);
            lp.setMargins(margin, margin, margin, margin);
            b.setLayoutParams(lp);

            b.setOnClickListener(v -> {
                Intent i = new Intent(this, SeatActivity.class);
                i.putExtra("showtime_id", s.id);
                startActivity(i);
            });
            gridShowtimes.addView(b);
        }
    }
}
