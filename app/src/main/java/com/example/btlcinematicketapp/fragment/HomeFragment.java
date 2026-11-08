package com.example.btlcinematicketapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.MainActivity;
import com.example.btlcinematicketapp.MovieDetailActivity;
import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.adapter.FeaturedMovieAdapter;
import com.example.btlcinematicketapp.adapter.HorizontalMovieAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Movie> allMovies = db.getMovies();
        
        List<Movie> sortedMovies = new ArrayList<>(allMovies);
        Collections.sort(sortedMovies, (m1, m2) -> Double.compare(m2.rating, m1.rating));
        
        List<Movie> featuredMovies = sortedMovies.subList(0, Math.min(3, sortedMovies.size()));

        RecyclerView rvFeatured = view.findViewById(R.id.rvFeaturedMovies);
        rvFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        FeaturedMovieAdapter featuredAdapter = new FeaturedMovieAdapter(featuredMovies, m -> {
            Intent i = new Intent(getActivity(), MovieDetailActivity.class);
            i.putExtra("movie_id", m.id);
            startActivity(i);
        });
        rvFeatured.setAdapter(featuredAdapter);

        RecyclerView rvNowShowing = view.findViewById(R.id.rvNowShowingMovies);
        rvNowShowing.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        HorizontalMovieAdapter nowShowingAdapter = new HorizontalMovieAdapter(allMovies, m -> {
            Intent i = new Intent(getActivity(), MovieDetailActivity.class);
            i.putExtra("movie_id", m.id);
            startActivity(i);
        });
        rvNowShowing.setAdapter(nowShowingAdapter);

        return view;
    }
}