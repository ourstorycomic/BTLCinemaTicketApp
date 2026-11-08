package com.example.btlcinematicketapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlcinematicketapp.R;
import com.example.btlcinematicketapp.model.Models.Review;

import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.foods = null; // Unused
        this.reviews = reviews;
    }

    private final List<Object> foods; // Dummy for backwards safety

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review r = reviews.get(position);
        holder.tvReviewerName.setText(r.userName != null ? r.userName : "Khách xem phim");
        holder.tvReviewRating.setText(String.format(Locale.US, "%.1f / 10", r.rating));
        holder.tvReviewComment.setText(r.comment != null ? r.comment : "Phim rất hay!");
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvReviewRating, tvReviewComment;

        ViewHolder(View view) {
            super(view);
            tvReviewerName = view.findViewById(R.id.tvReviewerName);
            tvReviewRating = view.findViewById(R.id.tvReviewRating);
            tvReviewComment = view.findViewById(R.id.tvReviewComment);
        }
    }
}
