package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostUserDetail extends AppCompatActivity {

    private String userId;
    private ImageView collapseImg;
    private MaterialToolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    //firebase
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user_detail);

        userId = getIntent().getStringExtra("userId");
        collapseImg = findViewById(R.id.collapseImage);
        toolbar = findViewById(R.id.material_app_bar);
        collapsingToolbarLayout = findViewById(R.id.collapseToolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        //firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("userName",snapshot.getValue(User.class).getName()+"");
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getThumb_image()).into(collapseImg);
                collapsingToolbarLayout.setTitle(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}