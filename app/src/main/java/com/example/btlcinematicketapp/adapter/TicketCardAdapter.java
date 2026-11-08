package com.example.btlcinematicketapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.Ticket;
import com.example.btlcinematicketapp.util.TimeUtil;
import com.example.btlcinematicketapp.util.UiUtil;

import java.util.List;

public class TicketCardAdapter extends RecyclerView.Adapter<TicketCardAdapter.ViewHolder> {
    private final List<Ticket> tickets;
    private final OnTicketActionListener listener;

    public interface OnTicketActionListener {
        void onAddToCalendar(Ticket ticket);
        void onCancelTicket(Ticket ticket);
    }

    public TicketCardAdapter(List<Ticket> tickets, OnTicketActionListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket t = tickets.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvMovieTitle.setText(t.movieTitle != null ? t.movieTitle : "Phim chiếu rạp");
        holder.tvTicketCode.setText(t.code());

        String cinema = t.cinemaName != null ? t.cinemaName : "CineBox";
        holder.tvCinemaInfo.setText("Rạp: " + cinema + " • " + (t.room != null ? t.room : "Phòng chiếu"));
        holder.tvShowTimeInfo.setText("Thời gian: " + t.showTime + " - " + TimeUtil.fmtDate(t.showDate) + " (" + (t.format != null ? t.format : "2D") + ")");
        holder.tvSeatInfo.setText("Vị trí ghế: " + t.seats);

        String food = t.foodSummary == null || t.foodSummary.isEmpty() ? "Không đặt bắp nước" : t.foodSummary;
        holder.tvFoodInfo.setText("Bắp nước: " + food);
        holder.tvPaymentInfo.setText("Tổng tiền: " + TimeUtil.money(t.totalPrice) + " • " + (t.paymentMethod != null ? t.paymentMethod : "Đã thanh toán"));

        holder.ivCinemaLogo.setImageResource(UiUtil.getCinemaLogoRes(ctx, cinema));

        // Generate 1D Barcode Bitmap
        Bitmap barcodeBitmap = UiUtil.generateBarcode(t.code(), 480, 140);
        if (holder.ivBarcode != null) {
            holder.ivBarcode.setImageBitmap(barcodeBitmap);
        }
        if (holder.tvBarcodeNumber != null) {
            holder.tvBarcodeNumber.setText(t.code());
        }

        // Generate 2D QR Code Bitmap
        Bitmap qrBitmap = UiUtil.generateQRCode(t.code(), 200, 200);
        if (holder.ivQRCode != null) {
            holder.ivQRCode.setImageBitmap(qrBitmap);
        }

        // Click Barcode / QR Code -> Open Zoomed Barcode Dialog for scanning
        View.OnClickListener scanClickListener = v -> showFullBarcodeDialog(ctx, t, barcodeBitmap, qrBitmap);
        if (holder.ivBarcode != null) holder.ivBarcode.setOnClickListener(scanClickListener);
        if (holder.ivQRCode != null) holder.ivQRCode.setOnClickListener(scanClickListener);

        holder.btnAddToCalendar.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCalendar(t);
        });

        holder.btnCancelTicket.setOnClickListener(v -> {
            if (listener != null) listener.onCancelTicket(t);
        });
    }

    private void showFullBarcodeDialog(Context context, Ticket ticket, Bitmap barcodeBitmap, Bitmap qrBitmap) {
        ImageView imgBarcode = new ImageView(context);
        imgBarcode.setImageBitmap(barcodeBitmap);
        imgBarcode.setPadding(20, 20, 20, 10);
        imgBarcode.setAdjustViewBounds(true);

        ImageView imgQR = new ImageView(context);
        imgQR.setImageBitmap(qrBitmap);
        imgQR.setPadding(20, 10, 20, 20);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("MÃ SOÁT VÉ RẠP: " + ticket.code() + "\n" + ticket.movieTitle);
        tvTitle.setTextSize(16);
        tvTitle.setGravity(android.view.Gravity.CENTER);
        tvTitle.setTextColor(android.graphics.Color.BLACK);
        tvTitle.setPadding(20, 20, 20, 10);

        TextView tvInstruction = new TextView(context);
        tvInstruction.setText("Vui lòng đưa màn hình này cho nhân viên soát vé quét mã vạch/QR tại cửa rạp.");
        tvInstruction.setTextSize(12);
        tvInstruction.setGravity(android.view.Gravity.CENTER);
        tvInstruction.setTextColor(android.graphics.Color.GRAY);
        tvInstruction.setPadding(20, 0, 20, 20);

        android.widget.LinearLayout container = new android.widget.LinearLayout(context);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.addView(tvTitle);
        container.addView(imgBarcode);
        container.addView(imgQR);
        container.addView(tvInstruction);

        new AlertDialog.Builder(context)
                .setTitle("🔍 Quét Mã Vạch Soát Vé")
                .setView(container)
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCinemaLogo, ivBarcode, ivQRCode;
        TextView tvMovieTitle, tvTicketCode, tvCinemaInfo, tvShowTimeInfo, tvSeatInfo, tvFoodInfo, tvPaymentInfo, tvBarcodeNumber;
        TextView btnAddToCalendar, btnCancelTicket;

        ViewHolder(View view) {
            super(view);
            ivCinemaLogo = view.findViewById(R.id.ivCinemaLogo);
            tvMovieTitle = view.findViewById(R.id.tvMovieTitle);
            tvTicketCode = view.findViewById(R.id.tvTicketCode);
            tvCinemaInfo = view.findViewById(R.id.tvCinemaInfo);
            tvShowTimeInfo = view.findViewById(R.id.tvShowTimeInfo);
            tvSeatInfo = view.findViewById(R.id.tvSeatInfo);
            tvFoodInfo = view.findViewById(R.id.tvFoodInfo);
            tvPaymentInfo = view.findViewById(R.id.tvPaymentInfo);
            ivBarcode = view.findViewById(R.id.ivBarcode);
            tvBarcodeNumber = view.findViewById(R.id.tvBarcodeNumber);
            ivQRCode = view.findViewById(R.id.ivQRCode);
            btnAddToCalendar = view.findViewById(R.id.btnAddToCalendar);
            btnCancelTicket = view.findViewById(R.id.btnCancelTicket);
        }
    }
}
