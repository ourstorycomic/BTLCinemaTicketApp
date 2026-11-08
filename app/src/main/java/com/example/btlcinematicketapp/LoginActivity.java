package com.example.btlcinematicketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.util.Session;
import com.example.btlcinematicketapp.util.UiUtil;

public class LoginActivity extends AppCompatActivity {
    private boolean registerMode = false;
    private EditText etFullname, etPhone, etUsername, etPassword;
    private Button btnSubmit;
    private TextView tvToggle;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        UiUtil.applyInsets(findViewById(R.id.root));

        db = new DatabaseHelper(this);
        etFullname = findViewById(R.id.etFullname);
        etPhone = findViewById(R.id.etPhone);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvToggle = findViewById(R.id.tvToggle);

        tvToggle.setOnClickListener(v -> {
            registerMode = !registerMode;
            updateMode();
        });
        btnSubmit.setOnClickListener(v -> submit());
        updateMode();
    }

    private void updateMode() {
        int vis = registerMode ? View.VISIBLE : View.GONE;
        etFullname.setVisibility(vis);
        etPhone.setVisibility(vis);
        btnSubmit.setText(registerMode ? "Đăng ký" : "Đăng nhập");
        tvToggle.setText(registerMode ? "Đã có tài khoản? Đăng nhập" : "Chưa có tài khoản? Đăng ký");
    }

    private void submit() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString();
        if (user.isEmpty() || pass.length() < 4) {
            Toast.makeText(this, "Nhập tài khoản và mật khẩu (từ 4 ký tự)", Toast.LENGTH_SHORT).show();
            return;
        }
        long id;
        if (registerMode) {
            String name = etFullname.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }
            id = db.registerUser(user, pass, name, etPhone.getText().toString().trim());
            if (id < 0) {
                Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            id = db.login(user, pass);
            if (id < 0) {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Session.login(this, id);
        openMain();
    }

    private void openMain() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
