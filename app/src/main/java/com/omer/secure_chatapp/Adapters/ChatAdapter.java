package com.omer.secure_chatapp.Adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omer.secure_chatapp.Models.ChatMessage;
import com.omer.secure_chatapp.databinding.ItemContainerRecievedMessageBinding;
import com.omer.secure_chatapp.databinding.ItemContainerSentMessageBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;
    public static final int VIEW_TYPE_SENT =1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new RecivedMessageViewHolder(
                    ItemContainerRecievedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setDate(chatMessages.get(position));
        }else{
            ((RecivedMessageViewHolder)holder).setDate(chatMessages.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        void setDate(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
        }
    }
    static class RecivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerRecievedMessageBinding binding;
        RecivedMessageViewHolder(ItemContainerRecievedMessageBinding itemContainerRecievedMessageBinding){
            super(itemContainerRecievedMessageBinding.getRoot());
            binding = itemContainerRecievedMessageBinding;
        }
        void setDate(ChatMessage chatMessage,Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }

    }
}
