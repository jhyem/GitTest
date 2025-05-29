package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardMenuAdapter extends RecyclerView.Adapter<CardMenuAdapter.ViewHolder> {

    public static class CardMenuItem {
        public final String title;
        public CardMenuItem(String title) {
            this.title = title;
        }
    }

    public interface OnCardClickListener {
        void onCardClick(String selected);
    }

    private final List<CardMenuItem> items;
    private final OnCardClickListener listener;

    public CardMenuAdapter(List<CardMenuItem> items, OnCardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardMenuItem item = items.get(position);
        holder.label.setText(item.title);
        holder.label.setOnClickListener(v -> listener.onCardClick(item.title));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.menuLabel);
        }
    }
}
