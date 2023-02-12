package com.omer.secure_chatapp.Activities;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.omer.secure_chatapp.Adapters.ChatAdapter;
import com.omer.secure_chatapp.Models.ChatMessage;
import com.omer.secure_chatapp.Models.User;
import com.omer.secure_chatapp.databinding.ActivityChatBinding;
import com.omer.secure_chatapp.utils.Constants;
import com.omer.secure_chatapp.utils.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
        getkeys();
    }

    private void getkeys() {
        database.collection(Constants.KEY_AES_KEYS).document(Constants.KEY_THE_KEYS)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        key = documentSnapshot.getString("iv");
                    }
                });
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList,
                getBitmapFromEncString(receiverUser.getImage()),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.getId());
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null){
            updateConversations(binding.inputMessage.getText().toString()); //enc

        }else{
            HashMap<String,Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID,receiverUser.getId());
            conversation.put(Constants.KEY_RECEIVER_NAME,receiverUser.getName());
            conversation.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.getImage());
            conversation.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString()); //enc
            conversation.put(Constants.KEY_TIMESTAMP,new Date());
            addConversations(conversation);
        }
        binding.inputMessage.setText(null);
    }
    private void listenAvailablityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.getId()
        ).addSnapshotListener(ChatActivity.this,((value, error) -> {
            if(error != null){
                return;
            }
            if(value != null){
                if(value.getLong(Constants.KEY_AVAILABLITY)!= null){
                    int availablity = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABLITY)
                    ).intValue();
                    isReceiverAvailable = availablity == 1;
                }
                receiverUser.setToken(value.getString(Constants.KEY_FCM_TOKEN));
            }
            if(isReceiverAvailable){
                binding.textAvailability.setVisibility(View.VISIBLE);
            }else{
                binding.textAvailability.setVisibility(View.GONE);
            }

        }));
    }
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error)->{

        if(error != null){
            return;
        }
        if(value != null){
            int count = chatMessageList.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE)); //decr
                    chatMessage.setDateTime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    chatMessage.setDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList,(obj1,obj2)->obj1.getDate().compareTo(obj2.getDate()));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(),chatMessageList.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversationId == null){
            checkForConversations();
        }
    };
    private Bitmap getBitmapFromEncString(String encImage){
        byte[] bytes = Base64.decode(encImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.getName());
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
        binding.layoutSend.setOnClickListener(v-> {
            sendMessage();
        });
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a" , Locale.getDefault()).format(date);
    }
    private void addConversations(HashMap<String,Object> conversation){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversations(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkForConversations(){
        if(chatMessageList.size() != 0){
            checkForConversationsRemotly(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.getId()
            );
            checkForConversationsRemotly(
                    receiverUser.getId(),
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }
    private void checkForConversationsRemotly(String senderId,String recevierId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,recevierId)
                .get()
                .addOnCompleteListener(conversationOnCompleteLister);
    }
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteLister = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailablityOfReceiver();
    }
}