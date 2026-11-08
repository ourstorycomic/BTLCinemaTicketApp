package com.example.btlcinematicketapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Ticket;
import com.example.btlcinematicketapp.reminder.ReminderScheduler;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private final List<Ticket> tickets = new ArrayList<>();
    private ArrayAdapter<Ticket> adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_tickets);
        UiUtil.applyInsets(findViewById(R.id.root));

        db = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        ListView lv = findViewById(R.id.lvTickets);
        adapter = new ArrayAdapter<Ticket>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, tickets) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                Ticket t = getItem(position);
                String remind = t.remindAt > 0 ? "Nhắc: " + TimeUtil.fmtMillis(t.remindAt)
                        : "Không đặt nhắc";
                String food = t.foodSummary == null || t.foodSummary.isEmpty()
                        ? "" : "\nBắp nước: " + t.foodSummary;
                String cinema = t.cinemaName != null ? "\nRạp: " + t.cinemaName : "";
                ((TextView) v.findViewById(android.R.id.text1)).setText(t.movieTitle + "  •  " + t.code());
                ((TextView) v.findViewById(android.R.id.text2)).setText(
                        t.showTime + " - " + TimeUtil.fmtDate(t.showDate) + " | " + t.room + " | " + t.format
                                + cinema
                                + "\nGhế: " + t.seats + food
                                + "\nTổng: " + TimeUtil.money(t.totalPrice) + " | " + t.paymentMethod
                                + "\n" + remind);
                return v;
            }
        };
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((p, v, pos, id) -> showActions(tickets.get(pos)));
        load();
    }

    private void load() {
        tickets.clear();
        tickets.addAll(db.getTickets(Session.getUserId(this)));
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(tickets.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showActions(Ticket t) {
        new AlertDialog.Builder(this)
                .setTitle(t.movieTitle)
                .setItems(new String[]{"Thêm vào lịch điện thoại", "Hủy vé"}, (d, which) -> {
                    if (which == 0) addToCalendar(t);
                    else confirmCancel(t);
                })
                .show();
    }

    /** Mở app Lịch với sự kiện điền sẵn; người dùng bấm Lưu (không cần xin quyền). */
    private void addToCalendar(Ticket t) {
        long start = TimeUtil.showMillis(t.showDate, t.showTime);
        Intent i = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "Xem phim: " + t.movieTitle)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "BTL Cinema - " + t.room)
                .putExtra(CalendarContract.Events.DESCRIPTION, "Mã vé " + t.code() + " - Ghế: " + t.seats)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + t.duration * 60_000L);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng Lịch", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmCancel(Ticket t) {
        new AlertDialog.Builder(this)
                .setMessage("Hủy vé " + t.movieTitle + " (ghế " + t.seats + ")?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy vé", (d, w) -> {
                    ReminderScheduler.cancel(this, t.id);
                    db.deleteTicket(t.id);
                    load();
                })
                .show();
    }
}
