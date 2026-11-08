package com.example.btlcinematicketapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.btlcinematicketapp.LoginActivity;
import com.example.btlcinematicketapp.MyTicketsActivity;
import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.database.DatabaseHelper;
import com.example.btlcinematicketapp.model.Models.Promotion;
import com.example.btlcinematicketapp.model.Models.Ticket;
import com.example.btlcinematicketapp.util.Session;

import java.util.List;

public class ProfileFragment extends Fragment {
    private DatabaseHelper db;
    private long userId;
    private TextView tvFullname, tvUsername, tvEmail, tvPhone, tvTicketCount, tvMemberRank, tvMemberTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new DatabaseHelper(requireContext());

        tvFullname = view.findViewById(R.id.tvFullname);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvTicketCount = view.findViewById(R.id.tvTicketCount);
        tvMemberRank = view.findViewById(R.id.tvMemberRank);
        tvMemberTitle = view.findViewById(R.id.tvMemberTitle);

        // Action items
        view.findViewById(R.id.btnMyTickets).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), MyTicketsActivity.class)));
        view.findViewById(R.id.btnStatTickets).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), MyTicketsActivity.class)));
        view.findViewById(R.id.btnStatVouchers).setOnClickListener(v -> showVouchersDialog());
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        view.findViewById(R.id.btnSupport).setOnClickListener(v -> showSupportDialog());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        loadUserData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        userId = Session.getUserId(requireContext());
        if (userId <= 0) {
            userId = 1;
            Session.login(requireContext(), 1);
        }

        String[] details = db.getUserDetails(userId);
        tvUsername.setText("@" + (details[0].isEmpty() ? "user" : details[0]));
        tvFullname.setText(details[1].isEmpty() ? "Thành viên CineBox" : details[1]);
        tvEmail.setText(details[2].isEmpty() ? "Email: Chưa cập nhật" : "Email: " + details[2]);
        tvPhone.setText(details[3].isEmpty() ? "SĐT: Chưa cập nhật" : "SĐT: " + details[3]);

        List<Ticket> tickets = db.getTickets(userId);
        int ticketCount = tickets.size();
        tvTicketCount.setText(String.valueOf(ticketCount));

        // Dynamic Rank based on actual bookings from SQLite DB
        if (ticketCount >= 8) {
            tvMemberTitle.setText("Thành viên VIP CineClub");
            tvMemberRank.setText("Hạng Kim Cương");
        } else if (ticketCount >= 4) {
            tvMemberTitle.setText("Thành viên VIP CineClub");
            tvMemberRank.setText("Hạng Vàng");
        } else if (ticketCount >= 1) {
            tvMemberTitle.setText("Thành viên CineClub");
            tvMemberRank.setText("Hạng Bạc");
        } else {
            tvMemberTitle.setText("Thành viên CineClub");
            tvMemberRank.setText("Thành Viên Mới");
        }
    }

    private void showEditProfileDialog() {
        userId = Session.getUserId(requireContext());
        String[] details = db.getUserDetails(userId);

        final EditText etName = new EditText(getContext());
        etName.setHint("Họ và tên");
        etName.setText(details[1]);

        final EditText etEmailInput = new EditText(getContext());
        etEmailInput.setHint("Địa chỉ Email");
        etEmailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmailInput.setText(details[2]);

        final EditText etPhoneInput = new EditText(getContext());
        etPhoneInput.setHint("Số điện thoại");
        etPhoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        etPhoneInput.setText(details[3]);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(etName);
        layout.addView(etEmailInput);
        layout.addView(etPhoneInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh Sửa Thông Tin Cá Nhân")
                .setView(layout)
                .setPositiveButton("Lưu thay đổi", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmailInput.getText().toString().trim();
                    String phone = etPhoneInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Họ tên không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (email.isEmpty()) {
                        Toast.makeText(getContext(), "Email không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = db.updateUserProfile(userId, name, email, phone);
                    if (success) {
                        Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        loadUserData();
                    } else {
                        Toast.makeText(getContext(), "Lỗi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        userId = Session.getUserId(requireContext());
        String[] details = db.getUserDetails(userId);

        final EditText etEmailVerify = new EditText(getContext());
        etEmailVerify.setHint("Nhập Email / Username xác nhận");
        etEmailVerify.setText(details[2].isEmpty() ? details[0] : details[2]);

        final EditText etOldPass = new EditText(getContext());
        etOldPass.setHint("Mật khẩu hiện tại");
        etOldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final EditText etNewPass = new EditText(getContext());
        etNewPass.setHint("Mật khẩu mới (tối thiểu 4 ký tự)");
        etNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(etEmailVerify);
        layout.addView(etOldPass);
        layout.addView(etNewPass);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi Mật Khẩu Bảo Mật")
                .setView(layout)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String email = etEmailVerify.getText().toString().trim();
                    String oldPass = etOldPass.getText().toString();
                    String newPass = etNewPass.getText().toString();

                    if (email.isEmpty() || oldPass.isEmpty() || newPass.length() < 4) {
                        Toast.makeText(getContext(), "Vui lòng nhập đầy đủ Email, mật khẩu cũ & mới (từ 4 ký tự)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean success = db.updateUserPassword(userId, email, oldPass, newPass);
                    if (success) {
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Email hoặc mật khẩu hiện tại không chính xác!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showVouchersDialog() {
        List<Promotion> promotions = db.getPromotions();
        StringBuilder sb = new StringBuilder();
        sb.append("Danh sách mã giảm giá khả dụng của bạn:\n\n");
        int idx = 1;
        for (Promotion p : promotions) {
            sb.append(idx++).append(". ").append(p.title).append(" - ").append(p.badge != null ? p.badge : "").append("\n");
            sb.append("   Mã voucher: ").append(p.discountCode).append("\n");
            sb.append("   Mô tả: ").append(p.description).append("\n\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("🎁 Kho Voucher Ưu Đãi")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Sử dụng khi đặt vé", null)
                .show();
    }

    private void showSupportDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Trung Tâm Hỗ Trợ Khách Hàng")
                .setMessage("Hotline CineBox: 1900 6017 (08:00 - 22:00 hàng ngày)\n\n" +
                        "Email hỗ trợ: support@cinebox.vn\n\n" +
                        "Địa chỉ trụ sở: Tầng 6, TTTM Icon 68, 2 Hải Triều, Quận 1, TP. Hồ Chí Minh")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng Xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản khỏi thiết bị?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    Session.logout(requireContext());
                    Toast.makeText(getContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
