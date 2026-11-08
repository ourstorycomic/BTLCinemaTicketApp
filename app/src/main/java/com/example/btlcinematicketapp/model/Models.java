package com.example.btlcinematicketapp.model;

public class Models {
    public static class Movie {
        public long id;
        public String title, description;
        public String posterName;   // tên file drawable, vd "poster_default"
        public int duration;        // phút
        public String genre, director, cast, ageRating, language, releaseDate;
        public double rating;       // điểm /10
    }

    public static class Showtime {
        public long id, movieId;
        public String showDate, showTime; // yyyy-MM-dd, HH:mm
        public int price;
        public String room, format;       // "Phòng 1", "2D"
    }

    public static class FoodItem {
        public long id;
        public String name, description, category, emoji;
        public int price;
    }

    public static class Ticket {
        public long id, userId, showtimeId;
        public String seats;          // "A1,A2"
        public String bookingDate;
        public int totalPrice;        // đã gồm bắp nước
        public int foodTotal;
        public String paymentMethod;
        public int remindDays;        // 0 = không nhắc
        public long remindAt;         // millis, 0 = không nhắc
        public String movieTitle, showDate, showTime, room, format;
        public int duration;
        public String foodSummary;    // "2x Bắp ngọt, 1x Coca" (rỗng nếu không có)

        public String code() { return String.format("BTL%06d", id); }
    }
}
