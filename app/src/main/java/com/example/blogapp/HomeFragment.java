package com.example.blogapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private RecyclerView homeRecyclerView;

    //firebase
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;

    private String userId;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeRecyclerView = view.findViewById(R.id.homeRecyclerView);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userId = currentUser.getUid();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Query query = mDatabaseRef.child("all_blog").orderByChild("date");

        final FirebaseRecyclerOptions<AllBlog> options = new FirebaseRecyclerOptions.Builder<AllBlog>().setQuery(query,AllBlog.class).build();
        FirebaseRecyclerAdapter<AllBlog,HomeViewHolder> adapter = new FirebaseRecyclerAdapter<AllBlog, HomeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final HomeViewHolder holder, int position, @NonNull final AllBlog model) {

                holder.setData(model.getBlog_title(),model.getBlog_desc(),model.getBlog_image(),model.getDate());
                String homeUserId = model.getUser_id();

                //set user data
                mDatabaseRef.child("users").child(homeUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User userData = snapshot.getValue(User.class);
                        assert userData != null;
                        holder.setUserData(userData.getName(),userData.getStatus(),userData.getThumb_image());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //like numbers
                mDatabaseRef.child("likes").child(model.getBlog_id()).child("like").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.setLike(snapshot.getValue()+"");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //check for already liked
                mDatabaseRef.child("liked").child(userId).child(model.getBlog_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("like")){
                            holder.likeImage.setImageResource(R.drawable.like_image);
                        } else {
                            holder.likeImage.setImageResource(R.drawable.ic_baseline_favorite_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //onClickListener for like button
                holder.likeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String like = holder.likeNum.getText().toString();
                        final int value = Integer.parseInt(like);

                        mDatabaseRef.child("liked").child(userId).child(model.getBlog_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                Log.i("exist",snapshot.hasChild("like")+"");
                                if(snapshot.hasChild("like")){
                                    //dislike the post
                                    final int dlk = value - 1;

                                        mDatabaseRef.child("likes").child(model.getBlog_id()).child("like").setValue(dlk).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.i("dislike","successful");
                                                    holder.setLike(dlk+"");
                                                    mDatabaseRef.child("liked").child(userId).child(model.getBlog_id()).child("like").removeValue();
                                                    //holder.likeImage.getDrawable().setTint(getResources().getColor(R.color.colorAccent,null));
                                                    holder.likeImage.setImageResource(R.drawable.ic_baseline_favorite_24);
                                                }
                                            }
                                        });


                                } else{
                                    //like the post
                                    final int lk = value + 1;
                                    mDatabaseRef.child("likes").child(model.getBlog_id()).child("like").setValue(lk).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.i("liked","liked successfully");
                                                holder.setLike(lk+"");

                                                mDatabaseRef.child("liked").child(userId).child(model.getBlog_id()).child("like").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Log.i("taskLike","true");
                                                            holder.likeImage.setImageResource(R.drawable.like_image);
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


                //onClickListener for comments

                holder.commentImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(),PostDetailsActivity.class);
                        intent.putExtra("BlogId",model.getBlog_id());
                        intent.putExtra("Fragment","Home");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_left,R.anim.no_anim);
                    }
                });

                mDatabaseRef.child("comments").child(model.getBlog_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String commentNum = snapshot.getChildrenCount()+"";
                        holder.commentTxt.setText(commentNum);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.postImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(),PostDetailsActivity.class);
                        intent.putExtra("BlogId",model.getBlog_id());
                        intent.putExtra("Fragment","Home");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_left,R.anim.no_anim);
                    }
                });

                holder.postView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(),PostUserDetail.class);
                        intent.putExtra("userId",model.getUser_id());
                        startActivity(intent);
                    }
                });

                holder.followTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (holder.followTxt.getText().equals("Following")){

                            holder.followTxt.setEnabled(false);

                        } else{
                            mDatabaseRef.child("following").child(currentUser.getUid()).child(model.getUser_id()).child("following").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Log.i("following","following is successful");

                                        mDatabaseRef.child("follower").child(model.getUser_id()).child(currentUser.getUid()).child("follower").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.i("follower","follower is successful");
                                                    holder.followTxt.setText("Following");
                                                    Toast.makeText(getContext(), "You are following this user!!", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                });

                mDatabaseRef.child("following").child(currentUser.getUid()).child(model.getUser_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("following")){
                            holder.followTxt.setText("Following");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_blog_post,parent,false);
                return new HomeViewHolder(view);
            }
        };

        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.startListening();
        homeRecyclerView.setAdapter(adapter);
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private TextView description;
        private ImageView postImg;
        private TextView date;

        private CircleImageView profileImg;
        private TextView userName;
        private TextView userAbout;
        private TextView followTxt;

        private ImageView likeImage;
        private TextView likeNum;

        private ImageView commentImage;
        private TextView commentTxt;

        private View postView;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.single_post_title);
            description = itemView.findViewById(R.id.single_post_desc);
            date = itemView.findViewById(R.id.single_post_date);
            postImg = itemView.findViewById(R.id.single_post_image);

            profileImg = itemView.findViewById(R.id.single_pro_img);
            userName = itemView.findViewById(R.id.single_com_user_name);
            userAbout = itemView.findViewById(R.id.single_status);
            followTxt = itemView.findViewById(R.id.single_follow);

            likeImage = itemView.findViewById(R.id.like_image);
            likeNum = itemView.findViewById(R.id.like_num);

            commentImage = itemView.findViewById(R.id.commentImage);
            commentTxt = itemView.findViewById(R.id.commentNum);

            postView = itemView.findViewById(R.id.single_post_view);
        }

        public void setData(String title,String desc, String image, long date){
            this.title.setText(title);
            this.description.setText(desc);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String simpleDateFormat = dateFormat.format(date);
            this.date.setText(simpleDateFormat);
            Picasso.get().load(image).into(postImg);
        }

        public void setUserData(String userName, String userStatus, String profileImage){
            this.userName.setText(userName);
            this.userAbout.setText(userStatus);
            Picasso.get().load(profileImage).into(profileImg);
        }

        public void setLike(String like){
            //likeImage.getDrawable().setTint(getResources().getColor(R.color.colorLike,null));
            likeNum.setText(like);
        }
    }
}