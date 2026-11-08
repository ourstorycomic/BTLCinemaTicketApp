package com.example.btlcinematicketapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.adapter.FoodMenuAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.FoodItem;

import java.util.List;

public class FoodMenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_menu, container, false);

        RecyclerView rvFoodMenu = view.findViewById(R.id.rvFoodMenu);
        rvFoodMenu.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<FoodItem> foods = db.getFoods();

        FoodMenuAdapter adapter = new FoodMenuAdapter(foods);
        rvFoodMenu.setAdapter(adapter);

        return view;
    }
}