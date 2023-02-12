package com.omer.secure_chatapp.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omer.secure_chatapp.Listeners.ConversationListener;
import com.omer.secure_chatapp.Models.ChatMessage;
import com.omer.secure_chatapp.Models.User;
import com.omer.secure_chatapp.databinding.ItemContainerRecentConversationBinding;

import java.util.List;

import javax.xml.transform.sax.TemplatesHandler;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>{
    private final List<ChatMessage> chatMessageList;
    private final ConversationListener conversationListener;
    public RecentConversationAdapter(List<ChatMessage> thelist,ConversationListener conversationListener){
        this.chatMessageList = thelist;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setDate(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversationBinding binding;
        ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding){
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }
        void setDate(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConvImage(chatMessage.getConversationImage()));
            binding.textName.setText(chatMessage.getConversationName());
            binding.textRecentMessage.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener(v->{
                User user = new User();
                user.setId(chatMessage.getConvertsationId());
                user.setName(chatMessage.getConversationName());
                user.setImage(chatMessage.getConversationImage());
                conversationListener.onConversationClickd(user);
            });
        }
    }
    private Bitmap getConvImage(String encImage){
        byte []bytes = Base64.decode(encImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
