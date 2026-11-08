package com.example.btlcinematicketapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.adapter.MovieAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.UiUtil;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final List<Movie> allMovies = new ArrayList<>();
    private MovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long userId = Session.getUserId(this);
        if (userId < 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        UiUtil.applyInsets(findViewById(R.id.root));

        DatabaseHelper db = new DatabaseHelper(this);
        ((TextView) findViewById(R.id.tvGreeting)).setText("Xin chào, " + db.getFullname(userId));

        findViewById(R.id.btnTickets).setOnClickListener(
                v -> startActivity(new Intent(this, MyTicketsActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Session.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        allMovies.addAll(db.getMovies());
        adapter = new MovieAdapter(m -> {
            Intent i = new Intent(this, MovieDetailActivity.class);
            i.putExtra("movie_id", m.id);
            startActivity(i);
        });
        adapter.setItems(allMovies);

        RecyclerView rv = findViewById(R.id.rvMovies);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(adapter);

        ((EditText) findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });
    }

    private void filter(String q) {
        String key = plain(q);
        List<Movie> out = new ArrayList<>();
        for (Movie m : allMovies) {
            if (plain(m.title).contains(key) || plain(m.genre).contains(key)) out.add(m);
        }
        adapter.setItems(out);
    }

    /** Bỏ dấu + chữ thường để tìm kiếm "tinh yeu" vẫn ra "Tình Yêu". */
    private static String plain(String s) {
        String n = Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd');
    }
}
