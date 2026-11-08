package com.example.btlcinematicketapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.Promotion;

import java.util.List;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {
    private final List<Promotion> promotions;

    public PromoAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promo_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion promo = promotions.get(position);
        holder.tvPromoTitle.setText(promo.title);
        holder.tvPromoDesc.setText(promo.description);
        holder.tvPromoAction.setText(promo.actionText != null ? promo.actionText : "Xem chi tiết >");

        Context ctx = holder.itemView.getContext();
        int bgResId = R.drawable.bg_promo_card1;
        if ("bg_promo_card2".equals(promo.bgGradient)) {
            bgResId = R.drawable.bg_promo_card2;
        } else if ("bg_promo_card3".equals(promo.bgGradient)) {
            bgResId = R.drawable.bg_promo_card3;
        }
        holder.rlPromoContainer.setBackgroundResource(bgResId);

        holder.itemView.setOnClickListener(v -> showPromoDetail(ctx, promo));
    }

    private void showPromoDetail(Context context, Promotion promo) {
        new AlertDialog.Builder(context)
                .setTitle("🎁 " + promo.title)
                .setMessage(promo.description + "\n\n" +
                        "Mã giảm giá: " + (promo.discountCode != null ? promo.discountCode : "CINEBOX2026") + "\n\n" +
                        "Áp dụng khi mua vé hoặc gói bắp nước trực tiếp trên ứng dụng CineBox.")
                .setPositiveButton("Áp dụng ngay", (d, w) -> {
                    // Copy or apply discount logic if needed
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlPromoContainer;
        TextView tvPromoTitle, tvPromoDesc, tvPromoAction;

        ViewHolder(View view) {
            super(view);
            rlPromoContainer = view.findViewById(R.id.rlPromoContainer);
            tvPromoTitle = view.findViewById(R.id.tvPromoTitle);
            tvPromoDesc = view.findViewById(R.id.tvPromoDesc);
            tvPromoAction = view.findViewById(R.id.tvPromoAction);
        }
    }
}
