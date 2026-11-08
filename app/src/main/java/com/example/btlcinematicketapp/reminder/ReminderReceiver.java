package com.example.btlcinematicketapp.reminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.btlcinematicketapp.MyTicketsActivity;
import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Ticket;
import com.example.btlcinematicketapp.util.TimeUtil;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "cinema_reminder";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        long ticketId = intent.getLongExtra(ReminderScheduler.EXTRA_TICKET_ID, -1);
        Ticket t = new DatabaseHelper(ctx).getTicket(ticketId);
        if (t == null) return; // vé đã bị hủy

        createChannel(ctx);

        long showMs = TimeUtil.showMillis(t.showDate, t.showTime);
        long days = (showMs - System.currentTimeMillis()) / 86_400_000L;
        String when = days >= 1 ? "Còn " + days + " ngày" : "Sắp đến giờ chiếu";
        String text = when + ": " + t.movieTitle + " lúc " + t.showTime + " ngày "
                + TimeUtil.fmtDate(t.showDate) + " - ghế " + t.seats;

        Intent open = new Intent(ctx, MyTicketsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, (int) ticketId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhắc lịch xem phim")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // người dùng chưa cấp quyền thông báo
        }
        NotificationManagerCompat.from(ctx).notify((int) ticketId, nb.build());
    }

    private void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Nhắc lịch chiếu phim",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Thông báo nhắc trước ngày xem phim");
        ctx.getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }
}
