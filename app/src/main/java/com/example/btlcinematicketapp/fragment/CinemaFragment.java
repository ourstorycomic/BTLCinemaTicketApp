package com.example.btlcinematicketapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.CinemaDetailActivity;
import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.adapter.BrandAdapter;
import com.example.btlcinematicketapp.adapter.CinemaAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Cinema;

import java.util.ArrayList;
import java.util.List;

public class CinemaFragment extends Fragment {
    private List<Cinema> allCinemas;
    private CinemaAdapter cinemaAdapter;
    private RecyclerView rvCinemas;
    private TextView tvCinemaCount, tvLocationFilter;
    private String selectedBrand = "CGV";
    private String selectedCity = "TP.HCM";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cinema, container, false);

        tvCinemaCount = view.findViewById(R.id.tvCinemaCount);
        tvLocationFilter = view.findViewById(R.id.tvLocationFilter);
        rvCinemas = view.findViewById(R.id.rvCinemas);

        if (rvCinemas != null) {
            rvCinemas.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        DatabaseHelper db = new DatabaseHelper(requireContext());
        allCinemas = db.getCinemas();

        // City Location Filter
        if (tvLocationFilter != null) {
            tvLocationFilter.setOnClickListener(v -> showCityPicker());
        }

        // Setup Brand RecyclerView (Dynamic from DB)
        List<String> brands = db.getBrands();
        if (brands.isEmpty()) {
            brands.add("CGV");
            brands.add("Lotte");
            brands.add("Galaxy");
            brands.add("Beta");
            brands.add("BHD");
        }
        selectedBrand = brands.get(0);

        RecyclerView rvBrands = view.findViewById(R.id.rvBrands);
        if (rvBrands != null) {
            rvBrands.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            BrandAdapter brandAdapter = new BrandAdapter(brands, brand -> {
                selectedBrand = brand;
                filterCinemas();
            });
            rvBrands.setAdapter(brandAdapter);
        }

        // Setup Search Listener
        EditText etSearch = view.findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterCinemasSearch(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Initial load
        filterCinemas();

        return view;
    }

    private void showCityPicker() {
        String[] cities = {"TP.HCM", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Hải Phòng"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn Tỉnh / Thành Phố")
                .setItems(cities, (dialog, which) -> {
                    selectedCity = cities[which];
                    if (tvLocationFilter != null) tvLocationFilter.setText(selectedCity);
                    filterCinemas();
                    Toast.makeText(getContext(), "Đã chọn: " + selectedCity, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void filterCinemas() {
        List<Cinema> filteredList = new ArrayList<>();
        for (Cinema c : allCinemas) {
            if (c.brand != null && c.brand.equalsIgnoreCase(selectedBrand)) {
                if (selectedCity.equals("TP.HCM") && c.location != null && c.location.contains("TP. Hồ Chí Minh")) {
                    filteredList.add(c);
                } else if (c.location != null && c.location.contains(selectedCity)) {
                    filteredList.add(c);
                } else if (allCinemas.indexOf(c) < 3) {
                    filteredList.add(c);
                }
            }
        }

        if (filteredList.isEmpty()) {
            for (Cinema c : allCinemas) {
                if (c.brand != null && c.brand.equalsIgnoreCase(selectedBrand)) {
                    filteredList.add(c);
                }
            }
        }

        if (tvCinemaCount != null) {
            tvCinemaCount.setText(selectedBrand + " (" + filteredList.size() + ")");
        }

        cinemaAdapter = new CinemaAdapter(filteredList, cinema -> {
            Intent intent = new Intent(getActivity(), CinemaDetailActivity.class);
            intent.putExtra("cinema_id", cinema.id);
            startActivity(intent);
        });

        if (rvCinemas != null) {
            rvCinemas.setAdapter(cinemaAdapter);
        }
    }

    private void filterCinemasSearch(String query) {
        if (query.trim().isEmpty()) {
            filterCinemas();
            return;
        }
        String key = query.toLowerCase();
        List<Cinema> filteredList = new ArrayList<>();
        for (Cinema c : allCinemas) {
            if ((c.name != null && c.name.toLowerCase().contains(key)) ||
                (c.address != null && c.address.toLowerCase().contains(key)) ||
                (c.location != null && c.location.toLowerCase().contains(key))) {
                filteredList.add(c);
            }
        }

        if (tvCinemaCount != null) {
            tvCinemaCount.setText("Kết quả (" + filteredList.size() + ")");
        }

        cinemaAdapter = new CinemaAdapter(filteredList, cinema -> {
            Intent intent = new Intent(getActivity(), CinemaDetailActivity.class);
            intent.putExtra("cinema_id", cinema.id);
            startActivity(intent);
        });

        if (rvCinemas != null) {
            rvCinemas.setAdapter(cinemaAdapter);
        }
    }
}
