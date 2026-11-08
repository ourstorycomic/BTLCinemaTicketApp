package com.example.btlcinematicketapp.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Ticket;

/** Báo thức bị xóa khi khởi động lại máy -> đặt lại cho các vé còn nhắc. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        for (Ticket t : new DatabaseHelper(ctx).getPendingReminders(System.currentTimeMillis())) {
            ReminderScheduler.schedule(ctx, t.id, t.remindAt);
        }
    }
}
