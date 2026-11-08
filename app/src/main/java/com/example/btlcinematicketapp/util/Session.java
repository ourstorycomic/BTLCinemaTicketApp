package com.example.btlcinematicketapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public static long getUserId(Context c) { return sp(c).getLong("user_id", -1); }

    public static void login(Context c, long id) { sp(c).edit().putLong("user_id", id).apply(); }

    public static void logout(Context c) { sp(c).edit().clear().apply(); }
}
