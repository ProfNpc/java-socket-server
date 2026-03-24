package com.chat.client;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_OWN    = 0;
    private static final int VIEW_OTHER  = 1;
    private static final int VIEW_SYSTEM = 2;

    private final List<ChatMessage> messages = new ArrayList<>();

    public void add(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    public void clear() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override public int getItemViewType(int position) {
        switch (messages.get(position).type) {
            case OWN:    return VIEW_OWN;
            case OTHER:  return VIEW_OTHER;
            default:     return VIEW_SYSTEM;
        }
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_OWN:
                return new OwnHolder(inf.inflate(R.layout.item_message_own, parent, false));
            case VIEW_OTHER:
                return new OtherHolder(inf.inflate(R.layout.item_message_other, parent, false));
            default:
                return new SystemHolder(inf.inflate(R.layout.item_message_system, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof OwnHolder) {
            ((OwnHolder) holder).text.setText(msg.text);
        } else if (holder instanceof OtherHolder) {
            ((OtherHolder) holder).sender.setText(msg.sender);
            ((OtherHolder) holder).text.setText(msg.text);
        } else {
            ((SystemHolder) holder).text.setText(msg.text);
        }
    }

    @Override public int getItemCount() { return messages.size(); }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    static class OwnHolder extends RecyclerView.ViewHolder {
        TextView text;
        OwnHolder(View v) {
            super(v);
            text = v.findViewById(R.id.tv_text);
        }
    }

    static class OtherHolder extends RecyclerView.ViewHolder {
        TextView sender, text;
        OtherHolder(View v) {
            super(v);
            sender = v.findViewById(R.id.tv_sender);
            text   = v.findViewById(R.id.tv_text);
        }
    }

    static class SystemHolder extends RecyclerView.ViewHolder {
        TextView text;
        SystemHolder(View v) {
            super(v);
            text = v.findViewById(R.id.tv_text);
        }
    }
}
