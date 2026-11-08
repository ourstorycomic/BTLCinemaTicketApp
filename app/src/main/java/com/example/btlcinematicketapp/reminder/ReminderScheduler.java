package com.example.btlcinematicketapp.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderScheduler {
    public static final String EXTRA_TICKET_ID = "ticket_id";

    /**
     * Đặt báo thức nhắc lịch chiếu. Dùng setAndAllowWhileIdle (không cần quyền
     * SCHEDULE_EXACT_ALARM); có thể lệch vài phút - chấp nhận được với nhắc theo ngày.
     */
    public static void schedule(Context ctx, long ticketId, long triggerAt) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending(ctx, ticketId));
    }

    public static void cancel(Context ctx, long ticketId) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(pending(ctx, ticketId));
    }

    private static PendingIntent pending(Context ctx, long ticketId) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        i.putExtra(EXTRA_TICKET_ID, ticketId);
        return PendingIntent.getBroadcast(ctx, (int) ticketId, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
