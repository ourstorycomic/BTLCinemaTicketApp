package com.example.btlcinematicketapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.adapter.TicketCardAdapter;
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
    private TicketCardAdapter adapter;
    private RecyclerView rvTicketCards;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_tickets);
        UiUtil.applyInsets(findViewById(R.id.root));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        db = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvTicketCards = findViewById(R.id.rvTicketCards);
        rvTicketCards.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TicketCardAdapter(tickets, new TicketCardAdapter.OnTicketActionListener() {
            @Override
            public void onAddToCalendar(Ticket ticket) {
                addToCalendar(ticket);
            }

            @Override
            public void onCancelTicket(Ticket ticket) {
                confirmCancel(ticket);
            }
        });
        rvTicketCards.setAdapter(adapter);

        load();
    }

    private void load() {
        tickets.clear();
        tickets.addAll(db.getTickets(Session.getUserId(this)));
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(tickets.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addToCalendar(Ticket t) {
        long start = TimeUtil.showMillis(t.showDate, t.showTime);
        Intent i = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "Xem phim: " + t.movieTitle)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, (t.cinemaName != null ? t.cinemaName : "CineBox") + " - " + t.room)
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
                .setTitle("Hủy Vé Xem Phim")
                .setMessage("Bạn có chắc muốn hủy vé " + t.movieTitle + " (ghế " + t.seats + ")?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy vé", (d, w) -> {
                    ReminderScheduler.cancel(this, t.id);
                    db.deleteTicket(t.id);
                    load();
                    Toast.makeText(this, "Đã hủy vé thành công", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
