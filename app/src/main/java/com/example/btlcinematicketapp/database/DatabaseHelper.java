package com.example.btlcinematicketapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.btlcinematicketapp.model.Models.Cinema;
import com.example.btlcinematicketapp.model.Models.FoodItem;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Promotion;
import com.example.btlcinematicketapp.model.Models.Review;
import com.example.btlcinematicketapp.model.Models.Showtime;
import com.example.btlcinematicketapp.model.Models.Ticket;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CinemaBooking.db";
    private static final int DATABASE_VERSION = 9;

    private static final String MOVIE_COLS =
            "id, title, description, poster_url, duration, genre, director, cast_list, " +
            "age_rating, language, release_date, rating ";
    private static final String SHOW_COLS =
            "id, movie_id, cinema_id, show_date, show_time, price, room, format, total_seats ";
    private static final String TICKET_SELECT =
            "SELECT t.id, t.user_id, t.showtime_id, t.seat_number, t.booking_date, t.total_price, " +
            "t.remind_days, t.remind_at, m.title, s.show_date, s.show_time, m.duration, " +
            "s.room, s.format, t.food_total, t.payment_method, c.name " +
            "FROM Tickets t JOIN Showtimes s ON s.id = t.showtime_id " +
            "JOIN Movies m ON m.id = s.movie_id " +
            "JOIN Cinemas c ON c.id = s.cinema_id ";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, password TEXT, fullname TEXT, email TEXT, phone TEXT)");

        db.execSQL("CREATE TABLE Movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, description TEXT, poster_url TEXT, duration INTEGER, " +
                "genre TEXT, director TEXT, cast_list TEXT, age_rating TEXT, " +
                "language TEXT, release_date TEXT, rating REAL)");

        db.execSQL("CREATE TABLE Cinemas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "brand TEXT, name TEXT, address TEXT, location TEXT, " +
                "phone TEXT, facilities TEXT, operating_hours TEXT, rating REAL)");

        db.execSQL("CREATE TABLE Showtimes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, cinema_id INTEGER, show_date TEXT, show_time TEXT, price INTEGER, " +
                "room TEXT, format TEXT, total_seats INTEGER, " +
                "FOREIGN KEY(movie_id) REFERENCES Movies(id), " +
                "FOREIGN KEY(cinema_id) REFERENCES Cinemas(id))");

        db.execSQL("CREATE TABLE FoodItems (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, description TEXT, category TEXT, price INTEGER, emoji TEXT)");

        db.execSQL("CREATE TABLE Promotions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, description TEXT, action_text TEXT, badge TEXT, bg_gradient TEXT, discount_code TEXT)");

        db.execSQL("CREATE TABLE Reviews (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, user_name TEXT, rating REAL, comment TEXT, created_at TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES Movies(id))");

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
        db.execSQL("DROP TABLE IF EXISTS Reviews");
        db.execSQL("DROP TABLE IF EXISTS Promotions");
        db.execSQL("DROP TABLE IF EXISTS FoodItems");
        db.execSQL("DROP TABLE IF EXISTS Showtimes");
        db.execSQL("DROP TABLE IF EXISTS Cinemas");
        db.execSQL("DROP TABLE IF EXISTS Movies");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        // --- SEED DEFAULT USER ---
        ContentValues defaultUser = new ContentValues();
        defaultUser.put("id", 1);
        defaultUser.put("username", "user");
        defaultUser.put("password", hash("1234"));
        defaultUser.put("fullname", "Nguyễn Văn A");
        defaultUser.put("email", "user@cinebox.vn");
        defaultUser.put("phone", "0901234567");
        db.insert("Users", null, defaultUser);

        // --- CINEMAS ---
        String[][] cinemas = {
                {"CGV", "CGV Aeon Bình Tân", "Tầng 3, TTTM Aeon Mall Bình Tân, 1 Đường số 17A, P. Bình Trị Đông B", "Quận Bình Tân, TP. Hồ Chí Minh", "1900 6017", "4DX, IMAX, Dolby Atmos, Sweetbox, Parking", "08:00 - 23:30", "4.8"},
                {"CGV", "CGV Crescent Mall", "Tầng 5, Crescent Mall, 101 Tôn Dật Tiên, P. Tân Phú", "Quận 7, TP. Hồ Chí Minh", "1900 6017", "IMAX, Gold Class, Sweetbox, Parking", "08:00 - 23:30", "4.9"},
                {"CGV", "CGV Vincom Bà Triệu", "Tầng 6, Vincom Center, 191 Bà Triệu, Q. Hai Bà Trưng", "Quận Hai Bà Trưng, Hà Nội", "1900 6017", "IMAX, 4DX, Premium Lounge, Parking", "08:30 - 23:30", "4.8"},
                {"Lotte", "Lotte Cinema Gò Vấp", "Tầng 3, Lotte Mart, 242 Nguyễn Văn Lượng, P. 10", "Quận Gò Vấp, TP. Hồ Chí Minh", "028 3620 2020", "Super Plex, Dolby Atmos, Parking", "08:00 - 23:00", "4.6"},
                {"Galaxy", "Galaxy Nguyễn Du", "116 Nguyễn Du, P. Bến Thành", "Quận 1, TP. Hồ Chí Minh", "1900 2224", "Dolby Atmos, Food Court, Parking", "08:00 - 23:30", "4.7"},
                {"Beta", "Beta Cinemas Quang Trung", "645 Quang Trung, P. 11", "Quận Gò Vấp, TP. Hồ Chí Minh", "0934 632 682", "Standard 2D/3D, Student Discount, Parking", "08:00 - 23:00", "4.5"},
                {"BHD", "BHD Star Bitexco", "Tầng 3 & 4, TTTM Icon 68, 2 Hải Triều", "Quận 1, TP. Hồ Chí Minh", "1900 2099", "3D, First Class Lounge, Parking", "08:30 - 23:30", "4.6"}
        };
        List<Long> cinemaIds = new ArrayList<>();
        for (String[] c : cinemas) {
            ContentValues cv = new ContentValues();
            cv.put("brand", c[0]);
            cv.put("name", c[1]);
            cv.put("address", c[2]);
            cv.put("location", c[3]);
            cv.put("phone", c[4]);
            cv.put("facilities", c[5]);
            cv.put("operating_hours", c[6]);
            cv.put("rating", Double.parseDouble(c[7]));
            cinemaIds.add(db.insert("Cinemas", null, cv));
        }

        // --- MOVIES ---
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
                "poster_biet_doi", "poster_tinh_yeu", "poster_vu_tru",
                "poster_tham_tu", "poster_rung_xanh", "poster_cuoc_dua"
        };
        String[] times = {"17:40", "19:00", "20:00", "21:30", "22:30", "23:25"};
        int[] prices = {90000, 110000, 110000, 110000, 90000, 90000};
        String[] rooms = {"Phòng 1", "Phòng 2", "Phòng 3", "Phòng 1", "Phòng 2", "Phòng 3"};
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

            for (long cinemaId : cinemaIds) {
                for (int d = 0; d <= 6; d++) {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_YEAR, d);
                    String date = f.format(c.getTime());
                    
                    // Create some random showtimes for this movie at this cinema
                    int showCount = (int) (Math.random() * 3) + 2; // 2 to 4 shows
                    for (int i = 0; i < showCount; i++) {
                        ContentValues sv = new ContentValues();
                        sv.put("movie_id", movieId);
                        sv.put("cinema_id", cinemaId);
                        sv.put("show_date", date);
                        sv.put("show_time", times[(idx + i + d) % times.length]);
                        sv.put("price", prices[i % prices.length]);
                        sv.put("room", rooms[i % rooms.length]);
                        sv.put("format", (idx == 3 || idx == 6) && i >= 1 ? "3D" : "2D");
                        sv.put("total_seats", (int) (Math.random() * 100) + 100); // 100 to 200 seats
                        db.insert("Showtimes", null, sv);
                    }
                }
            }
        }

        // --- REVIEWS SEED ---
        String[][] sampleReviews = {
                {"Hoàng Nam", "9.0", "Phim xem cực kỳ đã mắt! Kỹ xảo hình ảnh và âm thanh vòm sống động vô cùng."},
                {"Thu Hà", "8.5", "Diễn xuất đỉnh cao, kịch bản hấp dẫn từ đầu đến cuối. Rất đáng tiền vé xem rạp!"},
                {"Minh Khoa", "9.5", "Tác phẩm điện ảnh xuất sắc nhất năm nay. Sẽ rủ thêm bạn bè đi xem lại lần nữa."},
                {"Bảo Ngọc", "8.8", "Nội dung cảm động, nhịp phim vừa phải và tạo hình nhân vật rất tuyệt vời!"}
        };

        // Seed reviews for each movie
        for (long mId = 1; mId <= movies.length; mId++) {
            for (String[] rv : sampleReviews) {
                ContentValues cv = new ContentValues();
                cv.put("movie_id", mId);
                cv.put("user_name", rv[0]);
                cv.put("rating", Double.parseDouble(rv[1]));
                cv.put("comment", rv[2]);
                cv.put("created_at", "2026-11-08");
                db.insert("Reviews", null, cv);
            }
        }

        // --- PROMOTIONS SEED ---
        String[][] promotions = {
                {"THỨ 4 VUI VẺ", "Đồng giá 50.000đ/vé 2D mọi suất chiếu vào Thứ 4 hàng tuần", "Nhận mã ngay >", "ĐỒNG GIÁ 50K", "bg_promo_card1", "WED50K"},
                {"COMBO COUPLE GIẢM 20%", "Giảm ngay 20% gói Bắp & Nước khi mua cùng cặp vé đôi", "Áp dụng ngay >", "GIẢM 20%", "bg_promo_card2", "COUPLE20"},
                {"HỌC SINH - SINH VIÊN", "Giá vé ưu đãi chỉ từ 45.000đ từ T2 đến T6 cho HSSV", "Xem chi tiết >", "VÉ 45K", "bg_promo_card3", "STUDENT45"},
                {"CINECLUB THÀNH VIÊN", "Tích điểm 10% cho mỗi giao dịch mua vé & bắp nước tại CineBox", "Đăng ký thẻ >", "TÍCH ĐIỂM 10%", "bg_promo_card1", "CINECLUB10"}
        };
        for (String[] pm : promotions) {
            ContentValues cv = new ContentValues();
            cv.put("title", pm[0]);
            cv.put("description", pm[1]);
            cv.put("action_text", pm[2]);
            cv.put("badge", pm[3]);
            cv.put("bg_gradient", pm[4]);
            cv.put("discount_code", pm[5]);
            db.insert("Promotions", null, cv);
        }

        // --- FOODS SEED (Real Cinema Concessions) ---
        String[][] foods = {
                {"Combo Solo", "1 Bắp rang bơ (Size L) + 1 Nước ngọt Pepsi (22oz)", "Combo", "79000", "🍿"},
                {"Combo Couple", "1 Bắp rang bơ lớn (Size L) + 2 Nước ngọt Pepsi (22oz)", "Combo", "109000", "🍿"},
                {"Combo Party", "2 Bắp rang bơ lớn (Size L) + 4 Nước ngọt Pepsi + 1 Snack Khoai Tây", "Combo", "199000", "🍿"},
                {"Combo Phô Mai Special", "1 Bắp phô mai giòn rụm (Size XL) + 2 Nước ngọt Pepsi (32oz)", "Combo", "129000", "🍿"},
                {"Bắp Rang Bơ (Size L)", "Bắp rang bơ nóng hổi vị ngọt giòn thơm", "Bắp", "55000", "🍿"},
                {"Bắp Rang Phô Mai (Size L)", "Bắp rang vị phô mai đậm đà giòn rụm", "Bắp", "65000", "🍿"},
                {"Nước Ngọt Pepsi (22oz)", "Ly nước ngọt Pepsi mát lạnh 22oz", "Nước", "35000", "🥤"},
                {"Nước Ngọt Coca-Cola (22oz)", "Ly nước ngọt Coca-Cola mát lạnh 22oz", "Nước", "35000", "🥤"}
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

    public long registerUser(String username, String password, String fullname, String phone) {
        return registerUser(username, password, fullname, username + "@gmail.com", phone);
    }

    public long registerUser(String username, String password, String fullname, String email, String phone) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", hash(password));
        cv.put("fullname", fullname);
        cv.put("email", email != null && !email.isEmpty() ? email : username + "@gmail.com");
        cv.put("phone", phone);
        return getWritableDatabase().insert("Users", null, cv);
    }

    public long login(String username, String password) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM Users WHERE (username = ? OR email = ?) AND password = ?",
                new String[]{username, username, hash(password)})) {
            return c.moveToFirst() ? c.getLong(0) : -1;
        }
    }

    public String getFullname(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT fullname FROM Users WHERE id = ?", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }

    public String[] getUserDetails(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT username, fullname, email, phone FROM Users WHERE id = ?", new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                String un = c.getString(0) != null ? c.getString(0) : "";
                String fn = c.getString(1) != null ? c.getString(1) : "";
                String em = c.getString(2) != null ? c.getString(2) : un + "@gmail.com";
                String ph = c.getString(3) != null ? c.getString(3) : "";
                return new String[]{un, fn, em, ph};
            }
        }
        return new String[]{"", "", "", ""};
    }

    public boolean updateUserProfile(long userId, String fullname, String email, String phone) {
        ContentValues cv = new ContentValues();
        cv.put("fullname", fullname);
        cv.put("email", email);
        cv.put("phone", phone);
        int rows = getWritableDatabase().update("Users", cv, "id = ?", new String[]{String.valueOf(userId)});
        if (rows <= 0 && userId > 0) {
            cv.put("id", userId);
            cv.put("username", "user");
            cv.put("password", hash("1234"));
            return getWritableDatabase().insert("Users", null, cv) > 0;
        }
        return rows > 0;
    }

    public boolean updateUserPassword(long userId, String email, String currentPassword, String newPassword) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM Users WHERE id = ? AND (email = ? OR username = ?) AND password = ?",
                new String[]{String.valueOf(userId), email, email, hash(currentPassword)})) {
            if (!c.moveToFirst()) {
                // Check if default user without explicit email
                try (Cursor c2 = getReadableDatabase().rawQuery(
                        "SELECT id FROM Users WHERE id = ? AND password = ?",
                        new String[]{String.valueOf(userId), hash(currentPassword)})) {
                    if (!c2.moveToFirst()) return false;
                }
            }
        }
        ContentValues cv = new ContentValues();
        cv.put("password", hash(newPassword));
        return getWritableDatabase().update("Users", cv, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    // ---------------- Movies / Cinemas / Showtimes / Food ----------------

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

    public List<Cinema> getCinemas() {
        List<Cinema> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, brand, name, address, location, phone, facilities, operating_hours, rating FROM Cinemas ORDER BY id", null)) {
            while (c.moveToNext()) {
                Cinema cinema = new Cinema();
                cinema.id = c.getLong(0);
                cinema.brand = c.getString(1);
                cinema.name = c.getString(2);
                cinema.address = c.getString(3);
                cinema.location = c.getString(4);
                cinema.phone = c.getString(5);
                cinema.facilities = c.getString(6);
                cinema.operatingHours = c.getString(7);
                cinema.rating = c.getDouble(8);
                list.add(cinema);
            }
        }
        return list;
    }

    public List<String> getBrands() {
        List<String> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT brand FROM Cinemas WHERE brand IS NOT NULL AND brand != '' ORDER BY id", null)) {
            while (c.moveToNext()) {
                list.add(c.getString(0));
            }
        }
        return list;
    }

    public Cinema getCinema(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, brand, name, address, location, phone, facilities, operating_hours, rating FROM Cinemas WHERE id = ?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                Cinema cinema = new Cinema();
                cinema.id = c.getLong(0);
                cinema.brand = c.getString(1);
                cinema.name = c.getString(2);
                cinema.address = c.getString(3);
                cinema.location = c.getString(4);
                cinema.phone = c.getString(5);
                cinema.facilities = c.getString(6);
                cinema.operatingHours = c.getString(7);
                cinema.rating = c.getDouble(8);
                return cinema;
            }
            return null;
        }
    }

    public List<Review> getReviewsForMovie(long movieId) {
        List<Review> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, movie_id, user_name, rating, comment, created_at FROM Reviews WHERE movie_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(movieId)})) {
            while (c.moveToNext()) {
                Review r = new Review();
                r.id = c.getLong(0);
                r.movieId = c.getLong(1);
                r.userName = c.getString(2);
                r.rating = c.getDouble(3);
                r.comment = c.getString(4);
                r.createdAt = c.getString(5);
                list.add(r);
            }
        }
        return list;
    }

    public List<Promotion> getPromotions() {
        List<Promotion> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, title, description, action_text, badge, bg_gradient, discount_code FROM Promotions ORDER BY id", null)) {
            while (c.moveToNext()) {
                Promotion p = new Promotion();
                p.id = c.getLong(0);
                p.title = c.getString(1);
                p.description = c.getString(2);
                p.actionText = c.getString(3);
                p.badge = c.getString(4);
                p.bgGradient = c.getString(5);
                p.discountCode = c.getString(6);
                list.add(p);
            }
        }
        return list;
    }

    public long addReview(long movieId, String userName, double rating, String comment) {
        ContentValues cv = new ContentValues();
        cv.put("movie_id", movieId);
        cv.put("user_name", userName);
        cv.put("rating", rating);
        cv.put("comment", comment);
        cv.put("created_at", new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        return getWritableDatabase().insert("Reviews", null, cv);
    }

    public List<Showtime> getShowtimesForCinema(long cinemaId, String date) {
        List<Showtime> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + SHOW_COLS + "FROM Showtimes WHERE cinema_id = ? AND show_date = ? " +
                "ORDER BY show_time", new String[]{String.valueOf(cinemaId), date})) {
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

    public List<Showtime> getShowtimes(long movieId) {
        List<Showtime> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + SHOW_COLS + "FROM Showtimes WHERE movie_id = ? " +
                "ORDER BY show_date, show_time", new String[]{String.valueOf(movieId)})) {
            while (c.moveToNext()) list.add(toShowtime(c));
        }
        return list;
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

    public long bookTickets(long userId, long showtimeId, List<String> seats, int seatTotal,
                            List<Long> foodIds, List<Integer> foodQty, String paymentMethod,
                            int remindDays, long remindAt) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Set<String> booked = getBookedSeats(db, showtimeId);
            for (String s : seats) if (booked.contains(s)) return -1;

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
        s.cinemaId = c.getLong(2);
        s.showDate = c.getString(3);
        s.showTime = c.getString(4);
        s.price = c.getInt(5);
        s.room = c.getString(6);
        s.format = c.getString(7);
        s.totalSeats = c.getInt(8);
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
        t.cinemaName = c.getString(16);
        t.foodSummary = "";
        return t;
    }
}