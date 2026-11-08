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

    // Distinct Seat Colors
    private static final int COLOR_STANDARD = 0xFFE8ECEF;
    private static final int COLOR_VIP = 0xFFFFB300;
    private static final int COLOR_COUPLE = 0xFF8E24AA;
    private static final int COLOR_SELECTED = 0xFFE50914;
    private static final int COLOR_BOOKED = 0xFF6C757D;

    private DatabaseHelper db;
    private Showtime showtime;
    private final Set<String> selected = new TreeSet<>();
    private GridLayout grid;
    private TextView tvTotal, tvStandardPrice, tvVipPrice, tvCouplePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seat);
        UiUtil.applyInsets(findViewById(R.id.root));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        db = new DatabaseHelper(this);
        showtime = db.getShowtime(getIntent().getLongExtra("showtime_id", -1));
        Movie movie = showtime == null ? null : db.getMovie(showtime.movieId);
        com.example.btlcinematicketapp.model.Models.Cinema cinema = showtime == null ? null : db.getCinema(showtime.cinemaId);
        if (movie == null || cinema == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvHeader)).setText(movie.title + "\n"
                + cinema.name + " • " + showtime.room + " (" + showtime.format + ")" + "\n"
                + showtime.showTime + " - " + TimeUtil.fmtDate(showtime.showDate));

        tvStandardPrice = findViewById(R.id.tvStandardPrice);
        tvVipPrice = findViewById(R.id.tvVipPrice);
        tvCouplePrice = findViewById(R.id.tvCouplePrice);

        tvStandardPrice.setText("Ghế Thường (" + TimeUtil.money(getSeatPrice("A1", showtime.price)) + ")");
        tvVipPrice.setText("Ghế VIP (" + TimeUtil.money(getSeatPrice("D1", showtime.price)) + ")");
        tvCouplePrice.setText("Ghế Couple (" + TimeUtil.money(getSeatPrice("F1", showtime.price)) + ")");

        grid = findViewById(R.id.gridSeats);
        tvTotal = findViewById(R.id.tvTotal);
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onNext());
        buildGrid();
        updateTotal();
    }

    public static int getSeatPrice(String seatCode, int basePrice) {
        if (seatCode == null || seatCode.isEmpty()) return basePrice;
        char row = seatCode.charAt(0);
        if (row == 'D' || row == 'E') {
            return basePrice + 20000; // Ghế VIP
        } else if (row == 'F') {
            return basePrice + 30000; // Ghế Couple
        }
        return basePrice; // Ghế thường
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
                    paint(b, COLOR_BOOKED, Color.WHITE);
                    b.setEnabled(false);
                } else {
                    boolean on = selected.contains(code);
                    applySeatStyle(b, code, on);
                    b.setOnClickListener(v -> {
                        if (selected.contains(code)) selected.remove(code);
                        else if (selected.size() >= 8) {
                            Toast.makeText(this, "Tối đa 8 ghế mỗi lần đặt", Toast.LENGTH_SHORT).show();
                            return;
                        } else selected.add(code);
                        boolean now = selected.contains(code);
                        applySeatStyle((MaterialButton) v, code, now);
                        updateTotal();
                    });
                }
                grid.addView(b);
            }
        }
    }

    private void applySeatStyle(MaterialButton b, String code, boolean isSelected) {
        if (isSelected) {
            paint(b, COLOR_SELECTED, Color.WHITE);
            return;
        }
        char row = code.charAt(0);
        if (row == 'D' || row == 'E') {
            paint(b, COLOR_VIP, Color.parseColor("#1A1A1A")); // Amber Gold (Dark text)
        } else if (row == 'F') {
            paint(b, COLOR_COUPLE, Color.WHITE); // Purple (White text)
        } else {
            paint(b, COLOR_STANDARD, Color.parseColor("#2C3E50")); // Standard Gray (Dark Slate text)
        }
    }

    private void paint(MaterialButton b, int bg, int fg) {
        b.setBackgroundTintList(ColorStateList.valueOf(bg));
        b.setTextColor(fg);
    }

    private void updateTotal() {
        if (selected.isEmpty()) {
            tvTotal.setText("Chưa chọn ghế nào");
            return;
        }

        int totalPrice = 0;
        for (String s : selected) {
            totalPrice += getSeatPrice(s, showtime.price);
        }

        tvTotal.setText("Đã chọn: " + TextUtils.join(", ", selected) + "\nTổng tiền vé: " + TimeUtil.money(totalPrice));
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
