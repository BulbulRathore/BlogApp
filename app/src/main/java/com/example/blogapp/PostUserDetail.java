package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostUserDetail extends AppCompatActivity implements View.OnClickListener{

    private String userId;
    private ImageView collapseImg;
    private MaterialToolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private TextView about;
    private TextView followers;
    private TextView followings;
    private TextView posts;

    private Button followBtn;
    private Button requestBtn;

    //firebase
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user_detail);

        userId = getIntent().getStringExtra("userId");
        collapseImg = findViewById(R.id.collapseImage);
        toolbar = findViewById(R.id.material_app_bar);
        collapsingToolbarLayout = findViewById(R.id.collapseToolbar);

        about = findViewById(R.id.user_detail_about);
        followers = findViewById(R.id.user_detail_followers);
        followings = findViewById(R.id.user_detail_following);
        posts = findViewById(R.id.user_details_posts);

        followBtn = findViewById(R.id.user_detail_follow_btn);
        requestBtn = findViewById(R.id.user_detail_request_btn);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        //firebase

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (userId.equals(currentUser.getUid())){
            followBtn.setVisibility(View.INVISIBLE);
            requestBtn.setVisibility(View.INVISIBLE);
            followBtn.setEnabled(false);
            requestBtn.setEnabled(false);
        }



        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("userName",snapshot.getValue(User.class).getName()+"");
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getThumb_image()).into(collapseImg);
                collapsingToolbarLayout.setTitle(user.getName());

                about.setText(user.getStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        followBtn.setOnClickListener(this);
        requestBtn.setOnClickListener(this);

        setFollowers();
        setFollowing();
        setPosts();

        mDatabaseRef.child("following").child(currentUser.getUid()).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("unfollowBtn",snapshot.hasChild("following")+"");
                //setText to unfollow
                if (snapshot.hasChild("following")){
                    followBtn.setText("unFollow");
                } else {
                    followBtn.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void unFollowTheUser() {

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.user_detail_request_btn:
                break;
            case R.id.user_detail_follow_btn:
                if (followBtn.getText().equals("unFollow")){
                    Log.i("unfollow","unfollowbtn");
                 doUnFollow();
                } else {
                    doFollow();
                }
                break;
        }
    }

    private void doUnFollow() {

        mDatabaseRef.child("following").child(currentUser.getUid()).child(userId).child("following").removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                mDatabaseRef.child("follower").child(userId).child(currentUser.getUid()).child("follower").removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        followBtn.setText("Follow");
                    }
                });
            }
        });

    }

    private void doFollow() {
        mDatabaseRef.child("following").child(currentUser.getUid()).child(userId).child("following").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.i("following","following is successful");

                    mDatabaseRef.child("follower").child(userId).child(currentUser.getUid()).child("follower").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.i("follower","follower is successful");
                                followBtn.setText("unFollow");
                                Toast.makeText(PostUserDetail.this, "You are following this user!!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });
    }

    private void setFollowers(){
        mDatabaseRef.child("follower").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("follower",snapshot.getChildrenCount()+"");
                long follower = snapshot.getChildrenCount();
                if (follower != 0){
                    followers.setText("number : "+follower+"");
                } else{
                    followers.setText("number : "+0+"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setFollowing(){
        mDatabaseRef.child("following").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("following",snapshot.getChildrenCount()+"");
                long following = snapshot.getChildrenCount();
                if (following != 0){
                    followings.setText("number : "+following+"");
                } else {
                    followings.setText("number : "+0+"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPosts() {
        mDatabaseRef.child("user_blog").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long postNum = snapshot.getChildrenCount();
                posts.setText("number : "+postNum+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}