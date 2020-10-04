package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostUserDetail extends AppCompatActivity implements View.OnClickListener{

    private String userId;
    private CircleImageView userImage;
    private TextView userName;

    private TextView about;
    private TextView followers;
    private TextView followings;
    private TextView posts;

    private Button followBtn;
    private Button requestBtn;

    private RecyclerView recyclerView;

    //firebase
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user_detail);

        userId = getIntent().getStringExtra("userId");


        about = findViewById(R.id.user_detail_about);
        followers = findViewById(R.id.user_detail_followers);
        followings = findViewById(R.id.user_detail_following);
        posts = findViewById(R.id.user_details_posts);
        userImage = findViewById(R.id.user_detail_image);
        userName = findViewById(R.id.user_detail_name);

        followBtn = findViewById(R.id.user_detail_follow_btn);
        requestBtn = findViewById(R.id.user_detail_request_btn);

        recyclerView = findViewById(R.id.user_detail_recycler_view);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        setPostImages();

        //firebase

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (userId.equals(currentUser.getUid())){
            followBtn.setVisibility(View.INVISIBLE);
            requestBtn.setVisibility(View.INVISIBLE);
            followBtn.setEnabled(false);
            requestBtn.setEnabled(false);
        }

        mDatabaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("userName",snapshot.getValue(User.class).getName()+"");
                User user = snapshot.getValue(User.class);

                Picasso.get().load(user.getThumb_image()).into(userImage);
                userName.setText(user.getName());
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

        mDatabaseRef.child("Requests").child(currentUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("request")){
                    String requestType = snapshot.child("request").getValue(String.class);
                    if (requestType.equals("sent")){
                        requestBtn.setText("Cancel Request");
                    } else if (requestType.equals("receive")){
                        requestBtn.setText("Accept Request");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

        mDatabaseRef.child("Friends").child(currentUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("friend")){
                    requestBtn.setText("Message");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.user_detail_request_btn:
                if (requestBtn.getText().equals("Cancel Request")){
                    Log.i("cancel","cancel request");
                    cancelRequest();
                } else if (requestBtn.getText().equals("Accept Request")){
                    Log.i("accept","accept request");
                    acceptRequest();
                } else if (requestBtn.getText().equals("Message")){
                    //Move to next activity to make a chat with the user
                    Intent intent = new Intent(PostUserDetail.this,MessageActivity.class);
                    intent.putExtra("userId",userId);
                    startActivity(intent);
                }else {
                    MakeRequest();
                }
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

    private void acceptRequest() {
        cancelRequest();
        mDatabaseRef.child("Friends").child(currentUser.getUid()).child(userId).child("friend").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDatabaseRef.child("Friends").child(userId).child(currentUser.getUid()).child("friend").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        requestBtn.setText("Message");
                    }
                });
            }
        });
    }

    private void cancelRequest() {

        mDatabaseRef.child("Requests").child(currentUser.getUid()).child(userId).child("request").removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                mDatabaseRef.child("Requests").child(userId).child(currentUser.getUid()).child("request").removeValue();
                requestBtn.setText("Request");
            }
        });
    }

    private void MakeRequest() {
        mDatabaseRef.child("Requests").child(currentUser.getUid()).child(userId).child("request").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Log.i("Request","Request is sent successfully");
                    requestBtn.setText("Cancel Request");

                    mDatabaseRef.child("Requests").child(userId).child(currentUser.getUid()).child("request").setValue("receive").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.i("Request","Request is received is added to database");
                            }
                        }
                    });
                }
            }
        });
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
                    followers.setText(follower+"");
                } else{
                    followers.setText(0+"");
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
                    followings.setText(following+"");
                } else {
                    followings.setText(0+"");
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
                posts.setText(postNum+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPostImages(){
        Query query = mDatabaseRef.child("user_blog").child(userId);
        FirebaseRecyclerOptions<PostModel> options = new FirebaseRecyclerOptions.Builder<PostModel>().setQuery(query,PostModel.class).build();
        FirebaseRecyclerAdapter<PostModel,UserDetailPosts> adapter = new FirebaseRecyclerAdapter<PostModel, UserDetailPosts>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserDetailPosts holder, int position, @NonNull PostModel model) {
                    Picasso.get().load(model.getPost_image()).into(holder.imageView);
            }

            @NonNull
            @Override
            public UserDetailPosts onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_image_layout,parent,false);
                return new UserDetailPosts(view);
            }
        };

        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }


    public static class UserDetailPosts extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public UserDetailPosts(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.single_post_layout_image);
        }
    }
}