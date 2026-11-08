package com.example.btlcinematicketapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.FoodItem;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Promotion;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.reminder.ReminderScheduler;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    private int foodTotal = 0;
    private int discountAmount = 0;
    private String appliedVoucherCode = "";
    private Spinner spReminder;
    private RadioGroup rgPayment;
    private EditText etVoucher;
    private TextView tvVoucherInfo, tvBreakdown;
    private CountDownTimer paymentTimer;

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, "Chưa cấp quyền thông báo - sẽ không hiện nhắc nhở",
                            Toast.LENGTH_LONG).show();
                }
                processBooking();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        UiUtil.applyInsets(findViewById(R.id.root));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

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
        int calculatedSeatTotal = 0;
        for (String s : seats) {
            calculatedSeatTotal += SeatActivity.getSeatPrice(s.trim(), showtime.price);
        }
        seatTotal = calculatedSeatTotal;

        long[] ids = in.getLongArrayExtra("food_ids");
        int[] qty = in.getIntArrayExtra("food_qty");
        StringBuilder foodText = new StringBuilder();
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

        tvBreakdown = findViewById(R.id.tvBreakdown);
        etVoucher = findViewById(R.id.etVoucher);
        tvVoucherInfo = findViewById(R.id.tvVoucherInfo);
        View btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        if (btnApplyVoucher != null) {
            btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        }

        updateBreakdown();

        rgPayment = findViewById(R.id.rgPayment);
        spReminder = findViewById(R.id.spReminder);
        spReminder.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, REMIND_LABELS));
        spReminder.setSelection(3);
        spReminder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateHint(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        findViewById(R.id.btnPay).setOnClickListener(v -> onConfirm());
        updateHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }
    }

    private void applyVoucher() {
        if (etVoucher == null) return;
        String code = etVoucher.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Promotion> promotions = db.getPromotions();
        Promotion matched = null;
        for (Promotion p : promotions) {
            if (p.discountCode != null && p.discountCode.equalsIgnoreCase(code)) {
                matched = p;
                break;
            }
        }

        int subTotal = seatTotal + foodTotal;
        if (matched != null || code.equals("WED50K") || code.equals("COUPLE20") || code.equals("STUDENT45") || code.equals("CINECLUB10")) {
            appliedVoucherCode = code;
            if (code.contains("20") || code.contains("10")) {
                discountAmount = (int) (subTotal * 0.2); // Giảm 20%
            } else {
                discountAmount = Math.min(subTotal, 50000); // Giảm 50k
            }
            tvVoucherInfo.setVisibility(View.VISIBLE);
            tvVoucherInfo.setText("Đã áp dụng mã " + code + ": Giảm -" + TimeUtil.money(discountAmount));
            Toast.makeText(this, "Áp dụng mã ưu đãi thành công!", Toast.LENGTH_SHORT).show();
        } else {
            discountAmount = 0;
            appliedVoucherCode = "";
            tvVoucherInfo.setVisibility(View.VISIBLE);
            tvVoucherInfo.setText("Mã ưu đãi không hợp lệ hoặc đã hết hạn!");
            Toast.makeText(this, "Mã ưu đãi không hợp lệ", Toast.LENGTH_SHORT).show();
        }
        updateBreakdown();
    }

    private void updateBreakdown() {
        int finalTotal = Math.max(0, seatTotal + foodTotal - discountAmount);
        StringBuilder sb = new StringBuilder();
        sb.append("Tiền vé (").append(seats.size()).append(" ghế): ").append(TimeUtil.money(seatTotal));
        sb.append("\nBắp nước: ").append(TimeUtil.money(foodTotal));
        if (discountAmount > 0) {
            sb.append("\nƯu đãi giảm giá (").append(appliedVoucherCode).append("): -").append(TimeUtil.money(discountAmount));
        }
        sb.append("\nTỔNG CỘNG: ").append(TimeUtil.money(finalTotal));
        tvBreakdown.setText(sb.toString());
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
        if (id == R.id.rbBank) return "Chuyển khoản ngân hàng (VietQR)";
        if (id == R.id.rbMomo) return "Ví điện tử MoMo";
        if (id == R.id.rbZaloPay) return "Ví điện tử ZaloPay";
        if (id == R.id.rbVNPay) return "Ví VNPay / Viettel Money";
        return "Thanh toán tại quầy";
    }

    private void onConfirm() {
        int days = REMIND_DAYS[spReminder.getSelectedItemPosition()];
        if (days > 0 && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        processBooking();
    }

    private void processBooking() {
        String method = paymentMethod();
        if (method.contains("ngân hàng") || method.contains("Ví") || method.contains("MoMo") || method.contains("ZaloPay") || method.contains("VNPay")) {
            showOnlinePaymentQRDialog();
        } else {
            book();
        }
    }

    private void showOnlinePaymentQRDialog() {
        int finalTotal = Math.max(0, seatTotal + foodTotal - discountAmount);
        String method = paymentMethod();
        String tempCode = String.format(Locale.US, "BTL%06d", System.currentTimeMillis() % 1000000);
        String paymentContent = "TT " + tempCode;

        Bitmap qrBitmap = UiUtil.generateQRCode("ONLINEPAY|" + method + "|0901234567|" + finalTotal + "|" + paymentContent, 360, 360);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(32, 24, 32, 24);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(method);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#E50914"));
        tvTitle.setGravity(Gravity.CENTER);

        ImageView imgQR = new ImageView(this);
        imgQR.setImageBitmap(qrBitmap);
        imgQR.setPadding(16, 16, 16, 16);

        String providerName = "MBBank";
        if (method.contains("MoMo")) providerName = "Ví MoMo";
        else if (method.contains("ZaloPay")) providerName = "Ví ZaloPay";
        else if (method.contains("VNPay")) providerName = "Ví VNPay";

        TextView tvDetails = new TextView(this);
        tvDetails.setText("Số tiền: " + TimeUtil.money(finalTotal) + "\n" +
                "Chủ tài khoản: CINEBOX VIETNAM JSC\n" +
                "Kênh thanh toán: " + providerName + "\n" +
                "Số TK / SĐT: 0901 234 567\n" +
                "Nội dung CK: " + paymentContent + "\n\n" +
                "⏳ HẠN THANH TOÁN:");
        tvDetails.setTextSize(13);
        tvDetails.setGravity(Gravity.CENTER);
        tvDetails.setTextColor(Color.parseColor("#333333"));

        TextView tvTimer = new TextView(this);
        tvTimer.setTextSize(26);
        tvTimer.setTypeface(null, Typeface.BOLD);
        tvTimer.setTextColor(Color.parseColor("#E50914"));
        tvTimer.setGravity(Gravity.CENTER);
        tvTimer.setPadding(0, 4, 0, 12);

        container.addView(tvTitle);
        container.addView(imgQR);
        container.addView(tvDetails);
        container.addView(tvTimer);

        AlertDialog paymentDialog = new AlertDialog.Builder(this)
                .setTitle("📲 Quét Mã QR Thanh Toán Online")
                .setView(container)
                .setPositiveButton("Đã chuyển khoản xong", (dialog, which) -> {
                    if (paymentTimer != null) paymentTimer.cancel();
                    Toast.makeText(this, "Hệ thống đã ghi nhận thanh toán online!", Toast.LENGTH_SHORT).show();
                    book();
                })
                .setNegativeButton("Hủy giao dịch", (dialog, which) -> {
                    if (paymentTimer != null) paymentTimer.cancel();
                })
                .setCancelable(false)
                .create();

        paymentTimer = new CountDownTimer(600_000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                paymentDialog.dismiss();
                Toast.makeText(CheckoutActivity.this, "Hết thời gian thanh toán! Vui lòng thực hiện lại.", Toast.LENGTH_LONG).show();
            }
        };

        paymentDialog.show();
        paymentTimer.start();
    }

    private void book() {
        int days = REMIND_DAYS[spReminder.getSelectedItemPosition()];
        long showMs = TimeUtil.showMillis(showtime.showDate, showtime.showTime);
        long remindAt = TimeUtil.reminderMillis(showMs, days);
        if (remindAt < 0) { remindAt = 0; days = 0; }

        int finalTotal = Math.max(0, seatTotal + foodTotal - discountAmount);

        long ticketId = db.bookTickets(Session.getUserId(this), showtime.id, seats, finalTotal - foodTotal,
                foodIds, foodQty, paymentMethod(), days, remindAt);
        if (ticketId < 0) {
            Toast.makeText(this, "Có ghế vừa được người khác đặt, vui lòng chọn lại ghế",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (remindAt > 0) ReminderScheduler.schedule(this, ticketId, remindAt);

        Toast.makeText(this, remindAt > 0 ? "Đặt vé thành công! Đã đặt nhắc lúc "
                + TimeUtil.fmtMillis(remindAt) : "Đặt vé thành công!", Toast.LENGTH_LONG).show();

        Intent main = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent tickets = new Intent(this, MyTicketsActivity.class);
        startActivities(new Intent[]{main, tickets});
        finish();
    }
}
