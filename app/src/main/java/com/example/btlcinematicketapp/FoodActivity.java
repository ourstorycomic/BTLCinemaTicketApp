package com.example.btlcinematicketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.adapter.FoodAdapter;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

public class FoodActivity extends AppCompatActivity {
    private FoodAdapter adapter;
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food);
        UiUtil.applyInsets(findViewById(R.id.root));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        long showtimeId = getIntent().getLongExtra("showtime_id", -1);
        String seats = getIntent().getStringExtra("seats");
        if (showtimeId < 0 || seats == null) {
            finish();
            return;
        }

        tvSummary = findViewById(R.id.tvSummary);
        adapter = new FoodAdapter(new DatabaseHelper(this).getFoods(), this::updateSummary);
        RecyclerView rv = findViewById(R.id.rvFood);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        updateSummary();

        findViewById(R.id.btnSkip).setOnClickListener(v -> goCheckout(showtimeId, seats, true));
        findViewById(R.id.btnNext).setOnClickListener(v -> goCheckout(showtimeId, seats, false));
    }

    private void updateSummary() {
        tvSummary.setText(adapter.count() == 0 ? "Chưa chọn món nào"
                : adapter.count() + " món • " + TimeUtil.money(adapter.total()));
    }

    private void goCheckout(long showtimeId, String seats, boolean skip) {
        Intent i = new Intent(this, CheckoutActivity.class);
        i.putExtra("showtime_id", showtimeId);
        i.putExtra("seats", seats);
        i.putExtra("food_ids", skip ? new long[0] : adapter.selectedIds());
        i.putExtra("food_qty", skip ? new int[0] : adapter.selectedQty());
        startActivity(i);
    }
}
