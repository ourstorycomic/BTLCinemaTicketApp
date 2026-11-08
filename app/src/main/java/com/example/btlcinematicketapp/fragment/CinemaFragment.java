package com.example.btlcinematicketapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private TextView tvCinemaCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cinema, container, false);

        tvCinemaCount = view.findViewById(R.id.tvCinemaCount);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        allCinemas = db.getCinemas();

        // Setup Brand RecyclerView
        List<String> brands = new ArrayList<>();
        brands.add("CGV");
        brands.add("Lotte");
        brands.add("Galaxy");
        brands.add("Beta");

        RecyclerView rvBrands = view.findViewById(R.id.rvBrands);
        rvBrands.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        BrandAdapter brandAdapter = new BrandAdapter(brands, this::filterByBrand);
        rvBrands.setAdapter(brandAdapter);

        // Setup Cinema RecyclerView
        RecyclerView rvCinemas = view.findViewById(R.id.rvCinemas);
        rvCinemas.setLayoutManager(new LinearLayoutManager(getContext()));
        cinemaAdapter = new CinemaAdapter(new ArrayList<>(), cinema -> {
            Intent intent = new Intent(getActivity(), CinemaDetailActivity.class);
            intent.putExtra("cinema_id", cinema.id);
            startActivity(intent);
        });
        rvCinemas.setAdapter(cinemaAdapter);

        // Initial load
        if (!brands.isEmpty()) {
            filterByBrand(brands.get(0));
        }

        return view;
    }

    private void filterByBrand(String brand) {
        List<Cinema> filteredList = new ArrayList<>();
        for (Cinema c : allCinemas) {
            if (c.brand.equals(brand)) {
                filteredList.add(c);
            }
        }
        
        tvCinemaCount.setText(brand + " (" + filteredList.size() + ")");
        
        cinemaAdapter = new CinemaAdapter(filteredList, cinema -> {
            Intent intent = new Intent(getActivity(), CinemaDetailActivity.class);
            intent.putExtra("cinema_id", cinema.id);
            startActivity(intent);
        });
        
        View view = getView();
        if (view != null) {
            RecyclerView rvCinemas = view.findViewById(R.id.rvCinemas);
            if (rvCinemas != null) {
                rvCinemas.setAdapter(cinemaAdapter);
            }
        }
    }
}