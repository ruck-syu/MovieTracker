package com.example.movietracker.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.R;

import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;
    private Markwon markwon;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (markwon == null) {
            markwon = Markwon.create(parent.getContext());
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        markwon.setMarkdown(holder.textMessage, message.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textMessage.getLayoutParams();
        if (message.isUser()) {
            holder.messageContainer.setGravity(Gravity.END);
            holder.textMessage.setBackgroundResource(R.drawable.bg_chat_user);
            holder.textMessage.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black, null));
            params.gravity = Gravity.END;
        } else {
            holder.messageContainer.setGravity(Gravity.START);
            holder.textMessage.setBackgroundResource(R.drawable.bg_chat_bot);
            holder.textMessage.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black, null));
            params.gravity = Gravity.START;
        }
        holder.textMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView textMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }
}
