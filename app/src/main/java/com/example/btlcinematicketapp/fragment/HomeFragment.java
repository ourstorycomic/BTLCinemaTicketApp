package com.example.btlcinematicketapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.CinemaDetailActivity;
import com.example.btlcinematicketapp.MainActivity;
import com.example.btlcinematicketapp.MovieDetailActivity;
import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.adapter.FeaturedMovieAdapter;
import com.example.btlcinematicketapp.adapter.HomeCinemaAdapter;
import com.example.btlcinematicketapp.adapter.HorizontalMovieAdapter;
import com.example.btlcinematicketapp.adapter.PromoAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Cinema;
import com.example.btlcinematicketapp.model.Models.Movie;
import com.example.btlcinematicketapp.model.Models.Promotion;
import com.example.btlcinematicketapp.util.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private DatabaseHelper db;
    private TextView tvLocationName, tvGreeting;
    private HomeCinemaAdapter cinemaAdapter;
    private final List<Cinema> allCinemas = new ArrayList<>();
    private final List<Cinema> filteredCinemas = new ArrayList<>();
    private String selectedCity = "TP. Hồ Chí Minh";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = new DatabaseHelper(requireContext());
        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvGreeting = view.findViewById(R.id.tvGreeting);

        long userId = Session.getUserId(requireContext());
        String fullname = db.getFullname(userId);
        if (!fullname.isEmpty()) {
            tvGreeting.setText("Xin chào " + fullname + "! Bạn muốn xem phim gì hôm nay? 👋");
        }

        // Header Actions
        view.findViewById(R.id.btnLocation).setOnClickListener(v -> showCityPicker());
        view.findViewById(R.id.ivNotification).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Không có thông báo mới", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.ivHelp).setOnClickListener(v -> showHelpModal());
        view.findViewById(R.id.ivUser).setOnClickListener(v -> navigateToTab(R.id.nav_profile));
        view.findViewById(R.id.layoutSearch).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), MainActivity.class)));
        view.findViewById(R.id.tvSeeAll).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), MainActivity.class)));
        View seeAllFeatured = view.findViewById(R.id.tvSeeAllFeatured);
        if (seeAllFeatured != null) {
            seeAllFeatured.setOnClickListener(v -> startActivity(new Intent(getActivity(), MainActivity.class)));
        }
        View seeAllUpcoming = view.findViewById(R.id.tvSeeAllUpcoming);
        if (seeAllUpcoming != null) {
            seeAllUpcoming.setOnClickListener(v -> startActivity(new Intent(getActivity(), MainActivity.class)));
        }

        // Quick Action Buttons
        view.findViewById(R.id.btnActionQuickBook).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), MainActivity.class)));
        view.findViewById(R.id.btnActionUpcoming).setOnClickListener(v -> {
            View upcomingSection = view.findViewById(R.id.rvUpcomingMovies);
            if (upcomingSection != null) {
                upcomingSection.getParent().requestChildFocus(upcomingSection, upcomingSection);
            }
        });
        view.findViewById(R.id.btnActionPromo).setOnClickListener(v -> showPromoModal());
        view.findViewById(R.id.btnActionFood).setOnClickListener(v -> navigateToTab(R.id.nav_food));

        // Promotions Banner RecyclerView (Dynamic from DB)
        RecyclerView rvPromotions = view.findViewById(R.id.rvPromotions);
        if (rvPromotions != null) {
            rvPromotions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            List<Promotion> promotions = db.getPromotions();
            rvPromotions.setAdapter(new PromoAdapter(promotions));
        }

        // Movies Data
        List<Movie> allMovies = db.getMovies();

        // 1. Featured Top Movies (Rating sorted)
        List<Movie> sortedMovies = new ArrayList<>(allMovies);
        Collections.sort(sortedMovies, (m1, m2) -> Double.compare(m2.rating, m1.rating));
        List<Movie> featuredMovies = sortedMovies.subList(0, Math.min(3, sortedMovies.size()));

        RecyclerView rvFeatured = view.findViewById(R.id.rvFeaturedMovies);
        rvFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(new FeaturedMovieAdapter(featuredMovies, m -> openMovieDetail(m.id)));

        // 2. Now Showing Movies
        RecyclerView rvNowShowing = view.findViewById(R.id.rvNowShowingMovies);
        rvNowShowing.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNowShowing.setAdapter(new HorizontalMovieAdapter(allMovies, m -> openMovieDetail(m.id)));

        // 3. Upcoming Movies (sublist simulation)
        List<Movie> upcomingMovies = new ArrayList<>(allMovies);
        Collections.shuffle(upcomingMovies);
        RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcomingMovies);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcoming.setAdapter(new HorizontalMovieAdapter(upcomingMovies, m -> openMovieDetail(m.id)));

        // 4. Cinemas Section
        allCinemas.clear();
        allCinemas.addAll(db.getCinemas());
        RecyclerView rvCinemas = view.findViewById(R.id.rvCinemas);
        rvCinemas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        cinemaAdapter = new HomeCinemaAdapter(filteredCinemas, cinema -> {
            Intent intent = new Intent(getActivity(), CinemaDetailActivity.class);
            intent.putExtra("cinema_id", cinema.id);
            startActivity(intent);
        });
        rvCinemas.setAdapter(cinemaAdapter);
        filterCinemasByCity(selectedCity);

        return view;
    }

    private void openMovieDetail(long movieId) {
        Intent i = new Intent(getActivity(), MovieDetailActivity.class);
        i.putExtra("movie_id", movieId);
        startActivity(i);
    }

    private void navigateToTab(int menuId) {
        if (getActivity() != null) {
            BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) {
                nav.setSelectedItemId(menuId);
            }
        }
    }

    private void showCityPicker() {
        String[] cities = {"TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Hải Phòng"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn Tỉnh / Thành Phố")
                .setItems(cities, (dialog, which) -> {
                    selectedCity = cities[which];
                    tvLocationName.setText(selectedCity);
                    filterCinemasByCity(selectedCity);
                    Toast.makeText(getContext(), "Đã chọn: " + selectedCity, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void filterCinemasByCity(String city) {
        filteredCinemas.clear();
        for (Cinema c : allCinemas) {
            if (c.location != null && c.location.contains(city)) {
                filteredCinemas.add(c);
            }
        }
        if (filteredCinemas.isEmpty()) {
            filteredCinemas.addAll(allCinemas); // Fallback to all if city has no specific items
        }
        if (cinemaAdapter != null) {
            cinemaAdapter.notifyDataSetChanged();
        }
    }

    private void showHelpModal() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Trung Tâm Hỗ Trợ Khách Hàng")
                .setMessage("Hotline CineBox: 1900 6017 (08:00 - 22:00 hàng ngày)\n\n" +
                        "Email hỗ trợ: support@cinebox.vn\n\n" +
                        "Nếu gặp sự cố thanh toán hoặc nhận vé, vui lòng liên hệ hotline để được hỗ trợ nhanh chóng.")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showPromoModal() {
        List<Promotion> promos = db.getPromotions();
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (Promotion p : promos) {
            sb.append(idx++).append(". ").append(p.title).append(": ").append(p.description);
            if (p.discountCode != null && !p.discountCode.isEmpty()) {
                sb.append(" (Mã ưu đãi: ").append(p.discountCode).append(")");
            }
            sb.append("\n\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chương Trình Khuyến Mãi Hot")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Hiểu rồi", null)
                .show();
    }
}
