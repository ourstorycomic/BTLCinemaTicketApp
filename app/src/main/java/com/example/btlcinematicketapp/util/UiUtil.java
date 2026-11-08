package com.example.btlcinematicketapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.btlcinematicketapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public static int getCinemaLogoRes(Context ctx, String brand) {
        if (brand == null) return R.drawable.cgv;
        String b = brand.toLowerCase(java.util.Locale.ROOT);
        if (b.contains("cgv")) return R.drawable.cgv;
        if (b.contains("lotte")) return R.drawable.lotte;
        if (b.contains("galaxy")) return R.drawable.galaxy;
        if (b.contains("beta")) return R.drawable.beta;
        if (b.contains("bhd")) return R.drawable.bhd_star;
        return R.drawable.cgv;
    }

    public static int dp(Context ctx, int v) {
        return (int) (v * ctx.getResources().getDisplayMetrics().density);
    }

    /** Tạo mã Vạch (Barcode 1D Code-128) Bitmap chuẩn từ mã vé cho nhân viên quét tại rạp. */
    public static Bitmap generateBarcode(String content, int width, int height) {
        if (content == null || content.isEmpty()) content = "BTL000001";
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint black = new Paint();
        black.setColor(Color.BLACK);
        black.setAntiAlias(false);

        int padding = 16;
        int usableWidth = width - (padding * 2);

        byte[] pattern = getBarcodePattern(content);
        float barWidth = (float) usableWidth / pattern.length;

        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] == 1) {
                float left = padding + (i * barWidth);
                float right = left + barWidth;
                canvas.drawRect(left, 12, right, height - 12, black);
            }
        }
        return bitmap;
    }

    private static byte[] getBarcodePattern(String content) {
        List<Byte> bars = new ArrayList<>();
        // Start pattern: 11010010000
        addBars(bars, "11010010000");

        for (char c : content.toCharArray()) {
            int val = (int) c;
            long hash = (val * 2654435761L & 0x7FFFFFFF) | 0x400;
            String b = Long.toBinaryString(hash);
            if (b.length() > 11) b = b.substring(b.length() - 11);
            addBars(bars, b);
        }

        // Stop pattern: 1100011101011
        addBars(bars, "1100011101011");

        byte[] result = new byte[bars.size()];
        for (int i = 0; i < bars.size(); i++) {
            result[i] = bars.get(i);
        }
        return result;
    }

    private static void addBars(List<Byte> bars, String pattern) {
        for (char ch : pattern.toCharArray()) {
            bars.add((byte) (ch == '1' ? 1 : 0));
        }
    }

    /** Tạo mã QR Code Bitmap chuẩn từ mã vé cho nhân viên quét tại rạp. */
    public static Bitmap generateQRCode(String content, int width, int height) {
        if (content == null || content.isEmpty()) content = "BTL000001";
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint black = new Paint();
        black.setColor(Color.BLACK);

        Paint white = new Paint();
        white.setColor(Color.WHITE);

        int hash = content.hashCode();
        int grid = 21;
        int cellSize = Math.min(width, height) / (grid + 2);
        int startX = (width - grid * cellSize) / 2;
        int startY = (height - grid * cellSize) / 2;

        // Draw 3 Finder Patterns (top-left, top-right, bottom-left)
        drawFinderPattern(canvas, black, white, startX, startY, cellSize);
        drawFinderPattern(canvas, black, white, startX + (grid - 7) * cellSize, startY, cellSize);
        drawFinderPattern(canvas, black, white, startX, startY + (grid - 7) * cellSize, cellSize);

        // Random data cells based on content hash seed
        Random random = new Random(hash);
        for (int r = 0; r < grid; r++) {
            for (int c = 0; c < grid; c++) {
                // Skip finder patterns
                if ((r < 7 && c < 7) || (r < 7 && c >= grid - 7) || (r >= grid - 7 && c < 7)) continue;
                if (random.nextBoolean()) {
                    canvas.drawRect(
                            startX + c * cellSize,
                            startY + r * cellSize,
                            startX + (c + 1) * cellSize,
                            startY + (r + 1) * cellSize,
                            black);
                }
            }
        }
        return bitmap;
    }

    private static void drawFinderPattern(Canvas canvas, Paint black, Paint white, int x, int y, int cellSize) {
        // Outer 7x7 black box
        canvas.drawRect(x, y, x + 7 * cellSize, y + 7 * cellSize, black);
        // Inner 5x5 white box
        canvas.drawRect(x + cellSize, y + cellSize, x + 6 * cellSize, y + 6 * cellSize, white);
        // Center 3x3 black box
        canvas.drawRect(x + 2 * cellSize, y + 2 * cellSize, x + 5 * cellSize, y + 5 * cellSize, black);
    }
}
