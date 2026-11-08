package com.example.btlcinematicketapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;
import com.google.android.material.button.MaterialButton;

import java.util.Set;
import java.util.TreeSet;

public class SeatActivity extends AppCompatActivity {
    private static final String[] ROWS = {"A", "B", "C", "D", "E", "F"};
    private static final int COLS = 8;
    private static final int FREE = 0xFFE0E0E0, SELECTED = 0xFFE50914, BOOKED = 0xFF757575;

    private DatabaseHelper db;
    private Showtime showtime;
    private final Set<String> selected = new TreeSet<>();
    private GridLayout grid;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seat);
        UiUtil.applyInsets(findViewById(R.id.root));

        db = new DatabaseHelper(this);
        showtime = db.getShowtime(getIntent().getLongExtra("showtime_id", -1));
        Movie movie = showtime == null ? null : db.getMovie(showtime.movieId);
        if (movie == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvHeader)).setText(movie.title + "\n"
                + showtime.showTime + " - " + TimeUtil.fmtDate(showtime.showDate)
                + " | " + showtime.room + " | " + showtime.format
                + "\n" + TimeUtil.money(showtime.price) + "/ghế");

        grid = findViewById(R.id.gridSeats);
        tvTotal = findViewById(R.id.tvTotal);
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onNext());
        buildGrid();
        updateTotal();
    }

    private void buildGrid() {
        grid.removeAllViews();
        Set<String> booked = db.getBookedSeats(showtime.id);
        int margin = UiUtil.dp(this, 3);
        for (String row : ROWS) {
            for (int c = 1; c <= COLS; c++) {
                final String code = row + c;
                MaterialButton b = new MaterialButton(this);
                b.setText(code);
                b.setTextSize(11);
                b.setAllCaps(false);
                b.setPadding(0, 0, 0, 0);
                b.setInsetTop(0);
                b.setInsetBottom(0);
                b.setMinWidth(0);
                b.setMinimumWidth(0);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
                lp.width = 0;
                lp.height = UiUtil.dp(this, 44);
                lp.setMargins(margin, margin, margin, margin);
                b.setLayoutParams(lp);

                if (booked.contains(code)) {
                    paint(b, BOOKED, Color.WHITE);
                    b.setEnabled(false);
                } else {
                    boolean on = selected.contains(code);
                    paint(b, on ? SELECTED : FREE, on ? Color.WHITE : Color.BLACK);
                    b.setOnClickListener(v -> {
                        if (selected.contains(code)) selected.remove(code);
                        else if (selected.size() >= 8) {
                            Toast.makeText(this, "Tối đa 8 ghế mỗi lần đặt", Toast.LENGTH_SHORT).show();
                            return;
                        } else selected.add(code);
                        boolean now = selected.contains(code);
                        paint((MaterialButton) v, now ? SELECTED : FREE, now ? Color.WHITE : Color.BLACK);
                        updateTotal();
                    });
                }
                grid.addView(b);
            }
        }
    }

    private void paint(MaterialButton b, int bg, int fg) {
        b.setBackgroundTintList(ColorStateList.valueOf(bg));
        b.setTextColor(fg);
    }

    private void updateTotal() {
        tvTotal.setText(selected.isEmpty() ? "Chưa chọn ghế"
                : "Ghế: " + TextUtils.join(", ", selected) + "\nTiền vé: "
                + TimeUtil.money(selected.size() * showtime.price));
    }

    private void onNext() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Hãy chọn ít nhất 1 ghế", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, FoodActivity.class);
        i.putExtra("showtime_id", showtime.id);
        i.putExtra("seats", TextUtils.join(",", selected));
        startActivity(i);
    }
}
