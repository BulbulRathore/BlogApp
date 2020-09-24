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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout sign_email, sign_password;
    private Button sign_in, sign_up;
    private TextView txt_warning;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sign_email = findViewById(R.id.sign_email);
        sign_password = findViewById(R.id.sign_password);
        txt_warning = findViewById(R.id.sign_warning);

        sign_in = findViewById(R.id.sign_in);
        sign_up = findViewById(R.id.sign_up);

        sign_in.setOnClickListener(this);
        sign_up.setOnClickListener(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in:
                // sign in method
                signIn();
                break;
            case R.id.sign_up:
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_down,R.anim.no_anim);
                finish();
                break;
        }
    }

    private void signIn() {
        String email = sign_email.getEditText().getText()+"";
        String password = sign_password.getEditText().getText() + "";

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            txt_warning.setText("please provide email and password");
            txt_warning.setVisibility(View.VISIBLE);
        } else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SignInActivity.this, "Sign In successful", Toast.LENGTH_SHORT).show();
                        //move to next activity
                        moveToNextActivity();
                    } else{
                        Toast.makeText(SignInActivity.this, "Error in sign in the user", Toast.LENGTH_SHORT).show();
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

    private void moveToNextActivity() {
        Intent intent = new Intent(SignInActivity.this,BlogActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }
}