package com.example.movietracker.ui.chat;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.R;
import com.example.movietracker.ui.detail.DetailActivity;

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
            markwon = Markwon.builder(parent.getContext())
                .usePlugin(new io.noties.markwon.AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull io.noties.markwon.MarkwonConfiguration.Builder builder) {
                        builder.linkResolver(new io.noties.markwon.LinkResolver() {
                            @Override
                            public void resolve(@NonNull View view, @NonNull String link) {
                                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("tt\\d{7,}").matcher(link);
                                if (matcher.find()) {
                                    String imdbId = matcher.group();
                                    android.content.Context context = view.getContext();
                                    Intent intent = new Intent(context, DetailActivity.class);
                                    intent.putExtra(DetailActivity.EXTRA_IMDB_ID, imdbId);
                                    context.startActivity(intent);
                                } else {
                                    new io.noties.markwon.LinkResolverDef().resolve(view, link);
                                }
                            }
                        });
                    }
                })
                .build();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        markwon.setMarkdown(holder.textMessage, message.getText());
        holder.textMessage.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

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
