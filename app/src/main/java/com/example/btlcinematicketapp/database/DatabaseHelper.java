package com.example.btlcinematicketapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.btlcinematicketapp.model.Models.FoodItem;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.model.Models.Ticket;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CinemaBooking.db";
    private static final int DATABASE_VERSION = 3;

    private static final String MOVIE_COLS =
            "id, title, description, poster_url, duration, genre, director, cast_list, " +
            "age_rating, language, release_date, rating ";
    private static final String SHOW_COLS =
            "id, movie_id, show_date, show_time, price, room, format ";
    private static final String TICKET_SELECT =
            "SELECT t.id, t.user_id, t.showtime_id, t.seat_number, t.booking_date, t.total_price, " +
            "t.remind_days, t.remind_at, m.title, s.show_date, s.show_time, m.duration, " +
            "s.room, s.format, t.food_total, t.payment_method " +
            "FROM Tickets t JOIN Showtimes s ON s.id = t.showtime_id " +
            "JOIN Movies m ON m.id = s.movie_id ";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, password TEXT, fullname TEXT, phone TEXT)");

        db.execSQL("CREATE TABLE Movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, description TEXT, poster_url TEXT, duration INTEGER, " +
                "genre TEXT, director TEXT, cast_list TEXT, age_rating TEXT, " +
                "language TEXT, release_date TEXT, rating REAL)");

        db.execSQL("CREATE TABLE Showtimes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, show_date TEXT, show_time TEXT, price INTEGER, " +
                "room TEXT, format TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES Movies(id))");

        db.execSQL("CREATE TABLE FoodItems (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, description TEXT, category TEXT, price INTEGER, emoji TEXT)");

        // seat_number: danh sách ghế của 1 lần đặt, vd "A1,A2"
        db.execSQL("CREATE TABLE Tickets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, showtime_id INTEGER, seat_number TEXT, booking_date TEXT, " +
                "total_price INTEGER, food_total INTEGER DEFAULT 0, payment_method TEXT, " +
                "remind_days INTEGER DEFAULT 0, remind_at INTEGER DEFAULT 0, " +
                "FOREIGN KEY(user_id) REFERENCES Users(id), " +
                "FOREIGN KEY(showtime_id) REFERENCES Showtimes(id))");

        db.execSQL("CREATE TABLE TicketFoods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ticket_id INTEGER, food_id INTEGER, quantity INTEGER, unit_price INTEGER, " +
                "FOREIGN KEY(ticket_id) REFERENCES Tickets(id), " +
                "FOREIGN KEY(food_id) REFERENCES FoodItems(id))");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TicketFoods");
        db.execSQL("DROP TABLE IF EXISTS Tickets");
        db.execSQL("DROP TABLE IF EXISTS FoodItems");
        db.execSQL("DROP TABLE IF EXISTS Showtimes");
        db.execSQL("DROP TABLE IF EXISTS Movies");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    /** Dữ liệu mẫu. Tất cả phim dùng chung poster_default - thay ảnh này (hoặc đổi tên ở cột poster_url). */
    private void seed(SQLiteDatabase db) {
        // title, description, duration, genre, director, cast, age, language, release, rating
        String[][] movies = {
                {"Biệt Đội Săn Ma", "Nhóm bạn trẻ mở dịch vụ trừ tà và vướng vào một vụ án bí ẩn trong căn biệt thự bỏ hoang.",
                        "112", "Kinh dị, Hài", "Nguyễn Hải Phong", "Lan Anh, Quốc Việt, Thu Hà", "T13", "Tiếng Việt", "2026-10-24", "7.4"},
                {"Tình Yêu Mùa Thu", "Chuyện tình lặng lẽ giữa hai người xa lạ gặp nhau ở Hà Nội vào cuối thu.",
                        "98", "Tình cảm", "Trần Minh Khuê", "Bảo Ngọc, Hoàng Nam", "T16", "Tiếng Việt", "2026-10-31", "8.1"},
                {"Hành Trình Vũ Trụ", "Phi hành đoàn lên đường tìm kiếm một hành tinh mới để cứu nhân loại.",
                        "135", "Khoa học viễn tưởng", "Daniel Hart", "Ethan Cole, Mira Sol", "T13", "Phụ đề tiếng Việt", "2026-11-07", "8.6"},
                {"Thám Tử Hà Nội", "Vị thám tử già điều tra chuỗi vụ mất tích bí ẩn trong khu phố cổ.",
                        "104", "Trinh thám", "Lê Quang Huy", "Đức Thịnh, Mai Phương", "T16", "Tiếng Việt", "2026-11-01", "7.9"},
                {"Rừng Xanh Phiêu Lưu", "Chú gấu nhỏ cùng những người bạn rừng xanh bảo vệ ngôi nhà của mình.",
                        "92", "Hoạt hình, Gia đình", "Sofia Marin", "Lồng tiếng: Hà My, Tuấn Anh", "P", "Lồng tiếng Việt", "2026-10-20", "8.3"},
                {"Cuộc Đua Sinh Tử", "Tay đua cũ buộc phải quay lại đường đua bất hợp pháp để cứu em trai.",
                        "118", "Hành động", "Marcus Reed", "Jake Turner, Linh Đan", "T18", "Phụ đề tiếng Việt", "2026-11-05", "7.6"}
        };
        String[] posters = {
                "poster_biet_doi",
                "poster_tinh_yeu",
                "poster_vu_tru",
                "poster_tham_tu",
                "poster_rung_xanh",
                "poster_cuoc_dua"
        };
        String[] times = {"10:00", "14:30", "19:30", "21:45"};
        int[] prices = {90000, 90000, 110000, 100000};
        String[] rooms = {"Phòng 1", "Phòng 2", "Phòng 3", "Phòng 1"};
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        int idx = 0;
        for (String[] m : movies) {
            idx++;
            ContentValues mv = new ContentValues();
            mv.put("title", m[0]);
            mv.put("description", m[1]);
            mv.put("poster_url", posters[idx - 1]);
            mv.put("duration", Integer.parseInt(m[2]));
            mv.put("genre", m[3]);
            mv.put("director", m[4]);
            mv.put("cast_list", m[5]);
            mv.put("age_rating", m[6]);
            mv.put("language", m[7]);
            mv.put("release_date", m[8]);
            mv.put("rating", Double.parseDouble(m[9]));
            long movieId = db.insert("Movies", null, mv);

            for (int d = 0; d <= 6; d++) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, d);
                String date = f.format(c.getTime());
                for (int i = 0; i < times.length; i++) {
                    ContentValues sv = new ContentValues();
                    sv.put("movie_id", movieId);
                    sv.put("show_date", date);
                    sv.put("show_time", times[i]);
                    sv.put("price", prices[i]);
                    sv.put("room", rooms[i]);
                    // phim 3 và 6: suất tối chiếu 3D
                    sv.put("format", (idx == 3 || idx == 6) && i >= 2 ? "3D" : "2D");
                    db.insert("Showtimes", null, sv);
                }
            }
        }

        // name, description, category, price, emoji
        String[][] foods = {
                {"Combo Solo", "1 bắp ngọt + 1 nước", "Combo", "79000", "🍿"},
                {"Combo Couple", "1 bắp lớn + 2 nước", "Combo", "109000", "🍿"},
                {"Combo Family", "2 bắp lớn + 4 nước", "Combo", "189000", "🍿"},
                {"Bắp ngọt", "Bắp rang vị ngọt (size M)", "Bắp", "55000", "🍿"},
                {"Bắp phô mai", "Bắp rang phủ bột phô mai", "Bắp", "65000", "🧀"},
                {"Bắp caramel", "Bắp rang caramel giòn", "Bắp", "65000", "🍿"},
                {"Coca-Cola", "Ly 22oz", "Nước", "35000", "🥤"},
                {"Sprite", "Ly 22oz", "Nước", "35000", "🥤"},
                {"Trà đào", "Trà đào cam sả", "Nước", "45000", "🍑"},
                {"Nước suối", "Chai 500ml", "Nước", "20000", "💧"},
                {"Khoai tây chiên", "Phần vừa, kèm sốt", "Đồ ăn nhẹ", "45000", "🍟"},
                {"Hotdog", "Xúc xích Đức kẹp bánh mì", "Đồ ăn nhẹ", "49000", "🌭"}
        };
        for (String[] fd : foods) {
            ContentValues cv = new ContentValues();
            cv.put("name", fd[0]);
            cv.put("description", fd[1]);
            cv.put("category", fd[2]);
            cv.put("price", Integer.parseInt(fd[3]));
            cv.put("emoji", fd[4]);
            db.insert("FoodItems", null, cv);
        }
    }

    // ---------------- Users ----------------

    private static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }

    /** @return id user mới, -1 nếu tên đăng nhập đã tồn tại */
    public long registerUser(String username, String password, String fullname, String phone) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", hash(password));
        cv.put("fullname", fullname);
        cv.put("phone", phone);
        return getWritableDatabase().insert("Users", null, cv);
    }

    /** @return id user, -1 nếu sai thông tin */
    public long login(String username, String password) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM Users WHERE username = ? AND password = ?",
                new String[]{username, hash(password)})) {
            return c.moveToFirst() ? c.getLong(0) : -1;
        }
    }

    public String getFullname(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT fullname FROM Users WHERE id = ?", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }

    // ---------------- Movies / Showtimes / Food ----------------

    public List<Movie> getMovies() {
        List<Movie> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + MOVIE_COLS + "FROM Movies ORDER BY id", null)) {
            while (c.moveToNext()) list.add(toMovie(c));
        }
        return list;
    }

    public Movie getMovie(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + MOVIE_COLS + "FROM Movies WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? toMovie(c) : null;
        }
    }

    public List<Showtime> getShowtimes(long movieId) {
        List<Showtime> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + SHOW_COLS + "FROM Showtimes WHERE movie_id = ? " +
                "ORDER BY show_date, show_time", new String[]{String.valueOf(movieId)})) {
            while (c.moveToNext()) list.add(toShowtime(c));
        }
        return list;
    }

    public Showtime getShowtime(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + SHOW_COLS + "FROM Showtimes WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? toShowtime(c) : null;
        }
    }

    public List<FoodItem> getFoods() {
        List<FoodItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, description, category, price, emoji FROM FoodItems ORDER BY id", null)) {
            while (c.moveToNext()) list.add(toFood(c));
        }
        return list;
    }

    public FoodItem getFood(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, description, category, price, emoji FROM FoodItems WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? toFood(c) : null;
        }
    }

    // ---------------- Tickets ----------------

    public Set<String> getBookedSeats(long showtimeId) {
        return getBookedSeats(getReadableDatabase(), showtimeId);
    }

    private Set<String> getBookedSeats(SQLiteDatabase db, long showtimeId) {
        Set<String> seats = new HashSet<>();
        try (Cursor c = db.rawQuery("SELECT seat_number FROM Tickets WHERE showtime_id = ?",
                new String[]{String.valueOf(showtimeId)})) {
            while (c.moveToNext()) {
                for (String s : c.getString(0).split(",")) seats.add(s.trim());
            }
        }
        return seats;
    }

    /**
     * Lưu 1 lần đặt (vé + bắp nước) trong 1 transaction.
     * foodIds/foodQty: danh sách đồ ăn đã chọn (có thể rỗng); giá lấy từ bảng FoodItems.
     * @return id vé, -1 nếu có ghế vừa bị người khác đặt
     */
    public long bookTickets(long userId, long showtimeId, List<String> seats, int seatTotal,
                            List<Long> foodIds, List<Integer> foodQty, String paymentMethod,
                            int remindDays, long remindAt) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Set<String> booked = getBookedSeats(db, showtimeId);
            for (String s : seats) if (booked.contains(s)) return -1;

            // tính tiền đồ ăn từ DB
            int foodTotal = 0;
            int[] prices = new int[foodIds.size()];
            for (int i = 0; i < foodIds.size(); i++) {
                try (Cursor c = db.rawQuery("SELECT price FROM FoodItems WHERE id = ?",
                        new String[]{String.valueOf(foodIds.get(i))})) {
                    prices[i] = c.moveToFirst() ? c.getInt(0) : 0;
                }
                foodTotal += prices[i] * foodQty.get(i);
            }

            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("showtime_id", showtimeId);
            cv.put("seat_number", TextUtils.join(",", seats));
            cv.put("booking_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    .format(Calendar.getInstance().getTime()));
            cv.put("total_price", seatTotal + foodTotal);
            cv.put("food_total", foodTotal);
            cv.put("payment_method", paymentMethod);
            cv.put("remind_days", remindDays);
            cv.put("remind_at", remindAt);
            long ticketId = db.insert("Tickets", null, cv);

            for (int i = 0; i < foodIds.size(); i++) {
                ContentValues fv = new ContentValues();
                fv.put("ticket_id", ticketId);
                fv.put("food_id", foodIds.get(i));
                fv.put("quantity", foodQty.get(i));
                fv.put("unit_price", prices[i]);
                db.insert("TicketFoods", null, fv);
            }
            db.setTransactionSuccessful();
            return ticketId;
        } finally {
            db.endTransaction();
        }
    }

    public List<Ticket> getTickets(long userId) {
        List<Ticket> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                TICKET_SELECT + "WHERE t.user_id = ? ORDER BY s.show_date, s.show_time",
                new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) list.add(toTicket(c));
        }
        for (Ticket t : list) t.foodSummary = foodSummary(t.id);
        return list;
    }

    public Ticket getTicket(long id) {
        Ticket t = null;
        try (Cursor c = getReadableDatabase().rawQuery(
                TICKET_SELECT + "WHERE t.id = ?", new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) t = toTicket(c);
        }
        if (t != null) t.foodSummary = foodSummary(t.id);
        return t;
    }

    /** Các vé còn nhắc trong tương lai - dùng để đặt lại báo thức sau khi khởi động lại máy. */
    public List<Ticket> getPendingReminders(long now) {
        List<Ticket> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                TICKET_SELECT + "WHERE t.remind_at > ?", new String[]{String.valueOf(now)})) {
            while (c.moveToNext()) list.add(toTicket(c));
        }
        return list;
    }

    public void deleteTicket(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("TicketFoods", "ticket_id = ?", new String[]{String.valueOf(id)});
            db.delete("Tickets", "id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private String foodSummary(long ticketId) {
        List<String> parts = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT tf.quantity, f.name FROM TicketFoods tf " +
                "JOIN FoodItems f ON f.id = tf.food_id WHERE tf.ticket_id = ?",
                new String[]{String.valueOf(ticketId)})) {
            while (c.moveToNext()) parts.add(c.getInt(0) + "x " + c.getString(1));
        }
        return TextUtils.join(", ", parts);
    }

    // ---------------- Mapping ----------------

    private Movie toMovie(Cursor c) {
        Movie m = new Movie();
        m.id = c.getLong(0);
        m.title = c.getString(1);
        m.description = c.getString(2);
        m.posterName = c.getString(3);
        m.duration = c.getInt(4);
        m.genre = c.getString(5);
        m.director = c.getString(6);
        m.cast = c.getString(7);
        m.ageRating = c.getString(8);
        m.language = c.getString(9);
        m.releaseDate = c.getString(10);
        m.rating = c.getDouble(11);
        return m;
    }

    private Showtime toShowtime(Cursor c) {
        Showtime s = new Showtime();
        s.id = c.getLong(0);
        s.movieId = c.getLong(1);
        s.showDate = c.getString(2);
        s.showTime = c.getString(3);
        s.price = c.getInt(4);
        s.room = c.getString(5);
        s.format = c.getString(6);
        return s;
    }

    private FoodItem toFood(Cursor c) {
        FoodItem f = new FoodItem();
        f.id = c.getLong(0);
        f.name = c.getString(1);
        f.description = c.getString(2);
        f.category = c.getString(3);
        f.price = c.getInt(4);
        f.emoji = c.getString(5);
        return f;
    }

    private Ticket toTicket(Cursor c) {
        Ticket t = new Ticket();
        t.id = c.getLong(0);
        t.userId = c.getLong(1);
        t.showtimeId = c.getLong(2);
        t.seats = c.getString(3);
        t.bookingDate = c.getString(4);
        t.totalPrice = c.getInt(5);
        t.remindDays = c.getInt(6);
        t.remindAt = c.getLong(7);
        t.movieTitle = c.getString(8);
        t.showDate = c.getString(9);
        t.showTime = c.getString(10);
        t.duration = c.getInt(11);
        t.room = c.getString(12);
        t.format = c.getString(13);
        t.foodTotal = c.getInt(14);
        t.paymentMethod = c.getString(15);
        t.foodSummary = "";
        return t;
    }
}
