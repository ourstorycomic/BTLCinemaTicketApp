package com.example.btlcinematicketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.adapter.ReviewAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Review;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.List;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private Movie movie;
    private TextView btnBookTicket;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_detail);
        UiUtil.applyInsets(findViewById(R.id.root));

        View btnBack = findViewById(R.id.ivBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        db = new DatabaseHelper(this);
        movie = db.getMovie(getIntent().getLongExtra("movie_id", -1));
        if (movie == null) {
            finish();
            return;
        }

        btnBookTicket = findViewById(R.id.btnBookTicket);

        // Bind Movie Data dynamically from SQLite Database
        ((ImageView) findViewById(R.id.imgPoster)).setImageResource(UiUtil.posterRes(this, movie.posterName));
        ((TextView) findViewById(R.id.tvTitle)).setText(movie.title);
        ((TextView) findViewById(R.id.tvAge)).setText(movie.ageRating != null ? movie.ageRating : "P");

        TextView tvRatingScore = findViewById(R.id.tvRatingScore);
        if (tvRatingScore != null) {
            tvRatingScore.setText(String.format(Locale.US, "%.1f / 10", movie.rating));
        }

        TextView tvDuration = findViewById(R.id.tvDuration);
        if (tvDuration != null) {
            tvDuration.setText(movie.duration + " phút");
        }

        TextView tvGenre = findViewById(R.id.tvGenre);
        if (tvGenre != null) {
            tvGenre.setText(movie.genre != null ? movie.genre : "Điện ảnh");
        }

        TextView tvLang = findViewById(R.id.tvLang);
        if (tvLang != null) {
            tvLang.setText(movie.language != null ? movie.language : "Tiếng Việt");
        }

        ((TextView) findViewById(R.id.tvDirector)).setText("Đạo diễn: " + (movie.director != null ? movie.director : "Đang cập nhật"));
        ((TextView) findViewById(R.id.tvCast)).setText("Diễn viên: " + (movie.cast != null ? movie.cast : "Đang cập nhật"));
        ((TextView) findViewById(R.id.tvRelease)).setText("Khởi chiếu: " + TimeUtil.fmtDate(movie.releaseDate));
        ((TextView) findViewById(R.id.tvDesc)).setText(movie.description != null ? movie.description : "Chưa có mô tả.");

        // Reviews RecyclerView
        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setNestedScrollingEnabled(false);

        View btnWriteReview = findViewById(R.id.btnWriteReview);
        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> showAddReviewDialog());
        }

        loadReviews();

        // 'Mua vé ngay' button -> Launches dedicated MovieShowtimeActivity
        if (btnBookTicket != null) {
            btnBookTicket.setOnClickListener(v -> {
                Intent intent = new Intent(MovieDetailActivity.this, MovieShowtimeActivity.class);
                intent.putExtra("movie_id", movie.id);
                startActivity(intent);
            });
        }
    }

    private void loadReviews() {
        reviewList = db.getReviewsForMovie(movie.id);
        TextView tvReviewCount = findViewById(R.id.tvReviewCount);
        if (tvReviewCount != null) {
            tvReviewCount.setText("(" + reviewList.size() + " đánh giá từ cộng đồng)");
        }
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void showAddReviewDialog() {
        final EditText etName = new EditText(this);
        etName.setHint("Tên của bạn");
        long userId = Session.getUserId(this);
        String savedName = db.getFullname(userId);
        if (!savedName.isEmpty()) etName.setText(savedName);

        final EditText etComment = new EditText(this);
        etComment.setHint("Nhập nhận xét của bạn về bộ phim...");
        etComment.setMinLines(3);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);
        container.addView(etName);
        container.addView(etComment);

        new AlertDialog.Builder(this)
                .setTitle("Viết Đánh Giá Phim")
                .setView(container)
                .setPositiveButton("Gửi đánh giá", (dialog, which) -> {
                    String author = etName.getText().toString().trim();
                    String text = etComment.getText().toString().trim();
                    if (author.isEmpty()) author = "Khách xem phim";
                    if (text.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.addReview(movie.id, author, 9.0, text);
                    Toast.makeText(this, "Cảm ơn bạn đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
