package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailsActivity extends AppCompatActivity {

    private String blogId;
    private String fragment;

    private CircleImageView userImg;
    private TextView userName;
    private TextView userStatus;
    private TextView follow;
    private ImageView postImg;
    private TextView postTitle;
    private TextView postDesc;
    private TextView likeNum;
    private TextView commentNum;
    private ImageView likeImg;
    private ImageView commentImg;
    private ImageView shareImg;

    private ImageView commentBtn;
    private TextInputLayout commentEdt;
    private RecyclerView commentRecyclerView;

    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        blogId = getIntent().getStringExtra("BlogId");
        fragment = getIntent().getStringExtra("Fragment");
        Log.i("blogId",blogId+"");

        //initialize all views
        userImg = findViewById(R.id.detail_user_img);
        userName = findViewById(R.id.detail_user_name);
        userStatus = findViewById(R.id.detail_user_status);
        follow = findViewById(R.id.detail_follow);

        postImg = findViewById(R.id.detail_post_img);
        postTitle = findViewById(R.id.detail_post_title);
        postDesc = findViewById(R.id.detail_post_desc);

        likeImg = findViewById(R.id.detail_like_img);
        likeNum = findViewById(R.id.detail_like_txt);

        shareImg = findViewById(R.id.detail_share_img);
        commentImg = findViewById(R.id.detail_comment_img);
        commentNum = findViewById(R.id.detail_comment_txt);

        commentEdt = findViewById(R.id.detail_comment_edt);
        commentBtn = findViewById(R.id.detail_comment_btn);

        commentRecyclerView = findViewById(R.id.detail_comment_recycler_view);

        //firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (fragment.equals("Home")){
            follow.setVisibility(View.VISIBLE);
        } else if (fragment.equals("Fav")){
            follow.setVisibility(View.INVISIBLE);
        }

        //add data to views of activity
        databaseReference.child("all_blog").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("key",snapshot.getChildren().iterator().next().getKey());

                Iterable<DataSnapshot> dataSnapshots = snapshot.getChildren();
                for(Iterator<DataSnapshot> iterator = dataSnapshots.iterator();iterator.hasNext();){
                    AllBlog allBlog = iterator.next().getValue(AllBlog.class);
                    assert allBlog != null;
                    if(allBlog.getBlog_id().equals(blogId)){
                        Picasso.get().load(allBlog.getBlog_image()).into(postImg);
                        postTitle.setText(allBlog.getBlog_title());
                        postDesc.setText(allBlog.getBlog_desc());

                        String userId = allBlog.getUser_id();

                        databaseReference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.i("user",snapshot.getValue(User.class).getName()+"");
                                User user = snapshot.getValue(User.class);
                                userName.setText(user.getName());
                                userStatus.setText(user.getStatus());
                                Picasso.get().load(user.getThumb_image()).into(userImg);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        databaseReference.child("comments").child(blogId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String totalComm = snapshot.getChildrenCount()+"";
                                commentNum.setText(totalComm);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        databaseReference.child("likes").child(blogId).child("like").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                likeNum.setText(snapshot.getValue()+"");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        databaseReference.child("liked").child(userId).child(blogId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("like")){
                                   likeImg.setImageResource(R.drawable.like_image);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentValue = commentEdt.getEditText().getText().toString();
                if(!TextUtils.isEmpty(commentValue)){
                    HashMap<String, Object> commentHashMap = new HashMap<>();
                    commentHashMap.put("comment",commentValue);
                    commentHashMap.put("date", ServerValue.TIMESTAMP);
                    commentHashMap.put("userId",currentUser.getUid());
                    databaseReference.child("comments").child(blogId).push().setValue(commentHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                commentEdt.getEditText().setText("");
                            }
                        }
                    });
                }
            }
        });

       likeImg.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               final String likes = likeNum.getText().toString();
                final int likeValue = Integer.parseInt(likes);
               databaseReference.child("liked").child(currentUser.getUid()).child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if (snapshot.hasChild("like")){
                           //already like so we are going to dislike it
                           final int dlk = likeValue - 1;
                           databaseReference.child("liked").child(currentUser.getUid()).child(blogId).child("like").removeValue();
                           likeImg.setImageResource(R.drawable.ic_baseline_favorite_24);

                           databaseReference.child("likes").child(blogId).child("like").setValue(dlk).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   Log.i("like","value is added successfully");
                                   likeNum.setText(dlk+"");
                               }
                           });
                       } else{
                           //not liked so we are going to like it
                           final int lk = likeValue + 1;
                           databaseReference.child("likes").child(blogId).child("like").setValue(lk).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful()){
                                       likeNum.setText(lk+"");
                                       databaseReference.child("liked").child(currentUser.getUid()).child(blogId).child("like").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()){
                                                   Log.i("like","liked");
                                                   likeImg.setImageResource(R.drawable.like_image);
                                               }
                                           }
                                       });
                                   }
                               }
                           });
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });
           }
       });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = databaseReference.child("comments").child(blogId).orderByChild("date");

        FirebaseRecyclerOptions<CommentModel> options = new FirebaseRecyclerOptions.Builder<CommentModel>().setQuery(query,CommentModel.class).build();
        FirebaseRecyclerAdapter<CommentModel,CommentViewHolderClass> adapter = new FirebaseRecyclerAdapter<CommentModel, CommentViewHolderClass>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolderClass holder, int position, @NonNull CommentModel model) {
                holder.singleComment.setText(model.getComment());

                databaseReference.child("users").child(model.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User userData = snapshot.getValue(User.class);
                        assert userData != null;
                        holder.singleUserName.setText(userData.getName());
                        Picasso.get().load(userData.getThumb_image()).into(holder.singleCommImg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public CommentViewHolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment,parent,false);
                return new CommentViewHolderClass(view);
            }
        };

        adapter.startListening();
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setAdapter(adapter);
    }

    public static class CommentViewHolderClass extends RecyclerView.ViewHolder{

        private CircleImageView singleCommImg;
        private TextView singleUserName;
        private TextView singleComment;
        public CommentViewHolderClass(@NonNull View itemView) {
            super(itemView);

            singleCommImg = itemView.findViewById(R.id.single_comment_img);
            singleComment = itemView.findViewById(R.id.single_com_comment);
            singleUserName = itemView.findViewById(R.id.single_com_user_name);
        }
    }
}