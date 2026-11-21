package com.example.btlcinematicketapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
    private List<String> brands = new ArrayList<>();
    private CinemaAdapter cinemaAdapter;
    private BrandAdapter brandAdapter;
    private RecyclerView rvCinemas;
    private TextView tvCinemaCount, tvLocationFilter;
    private String selectedBrand = "Tất cả";
    private String selectedCity = "TP.HCM";
    private DatabaseHelper db;

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

        db = new DatabaseHelper(requireContext());
        allCinemas = db.getCinemas();

        // City Location Filter
        if (tvLocationFilter != null) {
            tvLocationFilter.setOnClickListener(v -> showCityPicker());
        }

        // Button Tạo rạp riêng
        View btnCreateCinema = view.findViewById(R.id.btnCreateCinema);
        if (btnCreateCinema != null) {
            btnCreateCinema.setOnClickListener(v -> showAddCinemaDialog());
        }

        // Setup Brand RecyclerView (Dynamic from DB)
        loadBrands();

        RecyclerView rvBrands = view.findViewById(R.id.rvBrands);
        if (rvBrands != null) {
            rvBrands.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            brandAdapter = new BrandAdapter(brands, brand -> {
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

    private void loadBrands() {
        brands.clear();
        brands.add("Tất cả");
        List<String> dbBrands = db.getBrands();
        if (dbBrands.isEmpty()) {
            brands.add("CineCinema 1");
            brands.add("CineCinema 2");
            brands.add("CineCinema 3");
            brands.add("CineCinema 4");
            brands.add("CineCinema 5");
            brands.add("CineCinema 6");
            brands.add("CineCinema 7");
        } else {
            for (String b : dbBrands) {
                if (!b.equalsIgnoreCase("Tất cả") && !brands.contains(b)) {
                    brands.add(b);
                }
            }
        }
        selectedBrand = brands.get(0);
    }

    private void showAddCinemaDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cinema, null);
        EditText etCinemaName = dialogView.findViewById(R.id.etCinemaName);
        Spinner spCity = dialogView.findViewById(R.id.spCity);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etFacilities = dialogView.findViewById(R.id.etFacilities);
        EditText etOperatingHours = dialogView.findViewById(R.id.etOperatingHours);

        String[] cities = {"TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Hải Phòng"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cities);
        spCity.setAdapter(cityAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo Rạp Chiếu Phim Mới")
                .setView(dialogView)
                .setPositiveButton("Tạo rạp", (dialog, which) -> {
                    String name = etCinemaName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên rạp!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String city = (String) spCity.getSelectedItem();
                    String address = etAddress.getText().toString().trim();
                    if (address.isEmpty()) address = "Đang cập nhật địa chỉ";
                    String phone = etPhone.getText().toString().trim();
                    if (phone.isEmpty()) phone = "1900 2026";
                    String facilities = etFacilities.getText().toString().trim();
                    if (facilities.isEmpty()) facilities = "Standard 2D/3D, Dolby Atmos, Parking";
                    String hours = etOperatingHours.getText().toString().trim();
                    if (hours.isEmpty()) hours = "08:00 - 23:30";

                    // Extract or determine brand
                    String brand = name.contains("-") ? name.split("-")[0].trim() : name;

                    long newId = db.addCinema(brand, name, address, city, phone, facilities, hours, 4.8);
                    if (newId > 0) {
                        Toast.makeText(getContext(), "Tạo rạp \"" + name + "\" thành công!", Toast.LENGTH_LONG).show();
                        allCinemas = db.getCinemas();
                        loadBrands();
                        if (brandAdapter != null) brandAdapter.notifyDataSetChanged();
                        filterCinemas();
                    } else {
                        Toast.makeText(getContext(), "Thêm rạp thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
            boolean matchesBrand = selectedBrand.equalsIgnoreCase("Tất cả")
                    || (c.brand != null && c.brand.equalsIgnoreCase(selectedBrand))
                    || (c.name != null && c.name.toLowerCase().contains(selectedBrand.toLowerCase()));

            if (matchesBrand) {
                if (selectedCity.equals("TP.HCM") && c.location != null && c.location.contains("TP. Hồ Chí Minh")) {
                    filteredList.add(c);
                } else if (c.location != null && c.location.contains(selectedCity)) {
                    filteredList.add(c);
                } else if (selectedBrand.equalsIgnoreCase("Tất cả") && allCinemas.indexOf(c) < 3) {
                    filteredList.add(c);
                } else if (!selectedBrand.equalsIgnoreCase("Tất cả")) {
                    filteredList.add(c);
                }
            }
        }

        if (filteredList.isEmpty()) {
            for (Cinema c : allCinemas) {
                boolean matchesBrand = selectedBrand.equalsIgnoreCase("Tất cả")
                        || (c.brand != null && c.brand.equalsIgnoreCase(selectedBrand))
                        || (c.name != null && c.name.toLowerCase().contains(selectedBrand.toLowerCase()));
                if (matchesBrand) {
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
