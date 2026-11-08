package com.example.btlcinematicketapp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    private static final SimpleDateFormat PARSER =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    /** Thời điểm bắt đầu suất chiếu (millis), -1 nếu lỗi. */
    public static long showMillis(String date, String time) {
        try {
            Date d = PARSER.parse(date + " " + time);
            return d == null ? -1 : d.getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    /**
     * Thời điểm nhắc: 09:00 sáng, trước giờ chiếu days ngày.
     * - Mốc đó đã qua (đặt vé sát ngày) -> nhắc sau 1 phút.
     * - Trả về -1 nếu suất chiếu đã qua hoặc days <= 0.
     */
    public static long reminderMillis(long showMs, int days) {
        long now = System.currentTimeMillis();
        if (days <= 0 || showMs <= now) return -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(showMs);
        c.add(Calendar.DAY_OF_YEAR, -days);
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long t = c.getTimeInMillis();
        if (t <= now) t = now + 60_000;
        return t < showMs ? t : -1;
    }

    /** yyyy-MM-dd -> dd/MM/yyyy */
    public static String fmtDate(String d) {
        String[] p = d.split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : d;
    }

    public static String fmtMillis(long ms) {
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date(ms));
    }

    public static String money(int v) {
        return String.format(Locale.US, "%,d", v).replace(',', '.') + "đ";
    }
}
