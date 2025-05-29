package com.example.project;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.chatBubble.setText(msg.message);

        LinearLayout parentLayout = (LinearLayout) holder.itemView;
        if (msg.isUser) {
            holder.chatBubble.setBackgroundResource(R.drawable.bubble_right);
            parentLayout.setGravity(Gravity.END);
            holder.chatBubble.setTextColor(Color.WHITE);
        } else {
            holder.chatBubble.setBackgroundResource(R.drawable.bubble_left);
            parentLayout.setGravity(Gravity.START);
            holder.chatBubble.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatBubble;

        public ViewHolder(View itemView) {
            super(itemView);
            chatBubble = itemView.findViewById(R.id.chatBubble);
        }
    }
}
