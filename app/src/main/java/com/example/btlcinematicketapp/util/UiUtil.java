package com.example.btlcinematicketapp.util;

import android.content.Context;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.btlcinematicketapp.R;

public class UiUtil {
    /** Chừa chỗ cho status bar / navigation bar khi bật edge-to-edge. */
    public static void applyInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets s = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(s.left, s.top, s.right, s.bottom);
            return insets;
        });
    }

    /** Lấy id drawable theo tên; không có thì dùng poster_default. */
    public static int posterRes(Context ctx, String name) {
        int id = 0;
        if (name != null) {
            id = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        }
        return id != 0 ? id : R.drawable.poster_default;
    }

    public static int dp(Context ctx, int v) {
        return (int) (v * ctx.getResources().getDisplayMetrics().density);
    }
}
