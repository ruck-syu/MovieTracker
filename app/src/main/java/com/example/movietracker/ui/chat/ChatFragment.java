package com.example.movietracker.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietracker.BuildConfig;
import com.example.movietracker.R;
import com.example.movietracker.data.database.DatabaseHelper;
import com.example.movietracker.data.repository.GeminiRepository;
import com.example.movietracker.model.Show;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private GeminiRepository geminiRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        adapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        dbHelper = new DatabaseHelper(requireContext());
        geminiRepository = new GeminiRepository(BuildConfig.GEMINI_API_KEY);

        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String prompt = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(prompt)) return;

        messages.add(new ChatMessage(prompt, true));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
        editTextMessage.setText("");

        fetchRecommendation(prompt);
    }

    private void fetchRecommendation(String prompt) {
        List<Show> allShows = dbHelper.getAllShows();
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(allShows.size(), 50); i++) {
            Show show = allShows.get(i);
            contextBuilder.append("- ").append(show.getTitle())
                    .append(" (").append(show.getType()).append(", ")
                    .append(show.getYear()).append(") - Status: ")
                    .append(show.getWatchStatus().name());
            if (show.getUserScore() != null) {
                contextBuilder.append(", Score: ").append(show.getUserScore());
            }
            contextBuilder.append("\n");
        }

        messages.add(new ChatMessage("Thinking...", false));
        final int thinkingIndex = messages.size() - 1;
        adapter.notifyItemInserted(thinkingIndex);
        recyclerView.scrollToPosition(thinkingIndex);

        geminiRepository.generateRecommendation(contextBuilder.toString(), prompt, new GeminiRepository.GeminiCallback() {
            @Override
            public void onSuccess(String responseText) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    messages.set(thinkingIndex, new ChatMessage(responseText, false));
                    adapter.notifyItemChanged(thinkingIndex);
                    recyclerView.scrollToPosition(thinkingIndex);
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    messages.set(thinkingIndex, new ChatMessage("Error: " + errorMessage, false));
                    adapter.notifyItemChanged(thinkingIndex);
                    Toast.makeText(getContext(), "Failed to get response", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
