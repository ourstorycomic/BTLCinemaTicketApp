package com.example.btlcinematicketapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CinemaBooking.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng Người dùng
        String CREATE_TABLE_USERS = "CREATE TABLE Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "fullname TEXT, " +
                "phone TEXT)";
        db.execSQL(CREATE_TABLE_USERS);

        // 2. Bảng Phim
        String CREATE_TABLE_MOVIES = "CREATE TABLE Movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "description TEXT, " +
                "poster_url TEXT, " +
                "duration INTEGER)";
        db.execSQL(CREATE_TABLE_MOVIES);

        // 3. Bảng Ghế & Suất chiếu
        String CREATE_TABLE_SHOWTIMES = "CREATE TABLE Showtimes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, " +
                "show_date TEXT, " +
                "show_time TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES Movies(id))";
        db.execSQL(CREATE_TABLE_SHOWTIMES);

        // 4. Bảng Vé
        String CREATE_TABLE_TICKETS = "CREATE TABLE Tickets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "showtime_id INTEGER, " +
                "seat_number TEXT, " +
                "booking_date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES Users(id), " +
                "FOREIGN KEY(showtime_id) REFERENCES Showtimes(id))";
        db.execSQL(CREATE_TABLE_TICKETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Tickets");
        db.execSQL("DROP TABLE IF EXISTS Showtimes");
        db.execSQL("DROP TABLE IF EXISTS Movies");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }
}