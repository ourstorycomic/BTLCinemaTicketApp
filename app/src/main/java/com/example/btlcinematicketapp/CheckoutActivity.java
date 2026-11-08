package com.example.btlcinematicketapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.FoodItem;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.reminder.ReminderScheduler;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private static final String[] REMIND_LABELS =
            {"Không nhắc", "Trước 1 ngày", "Trước 2 ngày", "Trước 3 ngày", "Trước 5 ngày"};
    private static final int[] REMIND_DAYS = {0, 1, 2, 3, 5};

    private DatabaseHelper db;
    private Showtime showtime;
    private Movie movie;
    private List<String> seats;
    private final List<Long> foodIds = new ArrayList<>();
    private final List<Integer> foodQty = new ArrayList<>();
    private int seatTotal;
    private Spinner spReminder;
    private RadioGroup rgPayment;

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, "Chưa cấp quyền thông báo - sẽ không hiện nhắc nhở",
                            Toast.LENGTH_LONG).show();
                }
                book(); // vẫn đặt vé, chỉ thông báo có thể không hiện
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        UiUtil.applyInsets(findViewById(R.id.root));

        db = new DatabaseHelper(this);
        Intent in = getIntent();
        showtime = db.getShowtime(in.getLongExtra("showtime_id", -1));
        movie = showtime == null ? null : db.getMovie(showtime.movieId);
        String seatStr = in.getStringExtra("seats");
        if (movie == null || seatStr == null) {
            finish();
            return;
        }
        seats = new ArrayList<>(Arrays.asList(seatStr.split(",")));
        seatTotal = seats.size() * showtime.price;

        long[] ids = in.getLongArrayExtra("food_ids");
        int[] qty = in.getIntArrayExtra("food_qty");
        StringBuilder foodText = new StringBuilder();
        int foodTotal = 0;
        if (ids != null && qty != null) {
            for (int i = 0; i < ids.length && i < qty.length; i++) {
                FoodItem f = db.getFood(ids[i]);
                if (f == null || qty[i] <= 0) continue;
                foodIds.add(f.id);
                foodQty.add(qty[i]);
                foodTotal += f.price * qty[i];
                foodText.append(qty[i]).append("x ").append(f.name)
                        .append("  -  ").append(TimeUtil.money(f.price * qty[i])).append("\n");
            }
        }

        ((TextView) findViewById(R.id.tvMovie)).setText(movie.title);
        ((TextView) findViewById(R.id.tvShow)).setText(showtime.showTime + " - "
                + TimeUtil.fmtDate(showtime.showDate) + "\n" + showtime.room + " | " + showtime.format
                + " | Ghế: " + android.text.TextUtils.join(", ", seats));
        ((TextView) findViewById(R.id.tvFood)).setText(foodText.length() == 0
                ? "Không đặt bắp nước" : foodText.toString().trim());
        ((TextView) findViewById(R.id.tvBreakdown)).setText(
                "Tiền vé (" + seats.size() + " ghế): " + TimeUtil.money(seatTotal)
                        + "\nBắp nước: " + TimeUtil.money(foodTotal)
                        + "\nTỔNG CỘNG: " + TimeUtil.money(seatTotal + foodTotal));

        rgPayment = findViewById(R.id.rgPayment);
        spReminder = findViewById(R.id.spReminder);
        spReminder.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, REMIND_LABELS));
        spReminder.setSelection(3); // mặc định nhắc trước 3 ngày
        spReminder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateHint(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        findViewById(R.id.btnPay).setOnClickListener(v -> onConfirm());
        updateHint();
    }

    private void updateHint() {
        TextView hint = findViewById(R.id.tvRemindHint);
        int days = REMIND_DAYS[spReminder.getSelectedItemPosition()];
        long at = TimeUtil.reminderMillis(TimeUtil.showMillis(showtime.showDate, showtime.showTime), days);
        hint.setText(days == 0 ? "" : at > 0 ? "Sẽ nhắc lúc: " + TimeUtil.fmtMillis(at)
                : "Suất chiếu quá gần, không đặt được nhắc");
    }

    private String paymentMethod() {
        int id = rgPayment.getCheckedRadioButtonId();
        if (id == R.id.rbBank) return "Chuyển khoản ngân hàng";
        if (id == R.id.rbWallet) return "Ví điện tử";
        return "Thanh toán tại quầy";
    }

    private void onConfirm() {
        int days = REMIND_DAYS[spReminder.getSelectedItemPosition()];
        if (days > 0 && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        book();
    }

    private void book() {
        int days = REMIND_DAYS[spReminder.getSelectedItemPosition()];
        long showMs = TimeUtil.showMillis(showtime.showDate, showtime.showTime);
        long remindAt = TimeUtil.reminderMillis(showMs, days);
        if (remindAt < 0) { remindAt = 0; days = 0; }

        long ticketId = db.bookTickets(Session.getUserId(this), showtime.id, seats, seatTotal,
                foodIds, foodQty, paymentMethod(), days, remindAt);
        if (ticketId < 0) {
            Toast.makeText(this, "Có ghế vừa được người khác đặt, vui lòng chọn lại ghế",
                    Toast.LENGTH_LONG).show();
            finish(); // quay lại chọn ghế
            return;
        }
        if (remindAt > 0) ReminderScheduler.schedule(this, ticketId, remindAt);

        Toast.makeText(this, remindAt > 0 ? "Đặt vé thành công! Đã đặt nhắc lúc "
                + TimeUtil.fmtMillis(remindAt) : "Đặt vé thành công!", Toast.LENGTH_LONG).show();

        // Về trang chủ rồi mở "Vé của tôi" (bỏ các màn hình đặt vé khỏi back stack)
        Intent main = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent tickets = new Intent(this, MyTicketsActivity.class);
        startActivities(new Intent[]{main, tickets});
        finish();
    }
}
