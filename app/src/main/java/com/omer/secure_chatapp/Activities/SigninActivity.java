package com.omer.secure_chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omer.secure_chatapp.databinding.ActivitySignInBinding;
import com.omer.secure_chatapp.utils.Constants;
import com.omer.secure_chatapp.utils.PreferenceManager;

public class SigninActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private ActivitySignInBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v->{
            if(isValidSignIn()){
                signIn();
            }
        });
    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task->{
                   if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocumentChanges().size() > 0){
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                       preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                       preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                       preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }
                   else{
                       loading(false);
                       showToasts("Unable to sign you in!");
                   }
                });
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBarr.setVisibility(View.VISIBLE);
        }else{
            binding.progressBarr.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
    private void showToasts(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignIn(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToasts("Enter Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToasts("Enter Valid Email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToasts("Enter Password");
            return false;
        }
        else{
            return true;
        }
    }
}