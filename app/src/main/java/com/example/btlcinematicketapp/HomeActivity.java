package com.example.btlcinematicketapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.btlcinematicketapp.fragment.CinemaFragment;
import com.example.btlcinematicketapp.fragment.FoodMenuFragment;
import com.example.btlcinematicketapp.fragment.HomeFragment;
import com.example.btlcinematicketapp.fragment.PlaceholderFragment;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.UiUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Session.getUserId(this) < 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_home);
        UiUtil.applyInsets(findViewById(R.id.root));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_cinema) {
                selectedFragment = new CinemaFragment();
            } else if (id == R.id.nav_food) {
                selectedFragment = new FoodMenuFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = PlaceholderFragment.newInstance("Tôi");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}