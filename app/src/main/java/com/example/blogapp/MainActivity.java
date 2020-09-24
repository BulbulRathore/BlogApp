package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout main_user_name, main_email, main_password;
    private Button main_sign_in,main_sign_up;
    private TextView warning;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_user_name = findViewById(R.id.main_user_name);
        main_email = findViewById(R.id.main_email);
        main_password = findViewById(R.id.main_password);

        main_sign_up = findViewById(R.id.main_sign_up);
        main_sign_in = findViewById(R.id.main_sign_in);

        warning = findViewById(R.id.main_warning_txt);

        main_sign_up.setOnClickListener(this);
        main_sign_in.setOnClickListener(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.main_sign_up:
                //sign up the user
                signUp();
                break;
            case R.id.main_sign_in:
                Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.no_anim);
                finish();
                break;
        }
    }

    private void signUp() {

        final String userName = main_user_name.getEditText().getText()+"";
        String email = main_email.getEditText().getText()+"";
        String password = main_password.getEditText().getText()+"";

        if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            warning.setText("Please, Provide user name, email and password");
            warning.setVisibility(View.VISIBLE);
        } else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //save data to the database
                        String userId = mAuth.getCurrentUser().getUid();

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("name",userName);
                        hashMap.put("image","default");
                        hashMap.put("thumb_image","default");
                        hashMap.put("status","Hey! I am new Blogger here!!!");
                        databaseRef.child("users").child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MainActivity.this, "data is added to database", Toast.LENGTH_SHORT).show();
                                    moveToNextActivity();
                                } else{
                                    Toast.makeText(MainActivity.this, "error while uploading data to database", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else{
                        Toast.makeText(MainActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //move to next activity
            moveToNextActivity();
        }
    }

    private void moveToNextActivity(){
        Intent intent = new Intent(MainActivity.this,BlogActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }
}