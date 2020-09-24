package com.example.blogapp;

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

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;

    //firebase
    private FirebaseUser currentUser;
    private DatabaseReference mDatabaseRef;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        recyclerView = view.findViewById(R.id.fav_recycler_view);

    }

    @Override
    public void onStart() {
        super.onStart();
        final String userId = currentUser.getUid();

        final Query query = mDatabaseRef.child("user_blog").child(userId);
        FirebaseRecyclerOptions<PostModel> options = new FirebaseRecyclerOptions.Builder<PostModel>()
                .setQuery(query,PostModel.class).build();
        FirebaseRecyclerAdapter<PostModel,PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostModel, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, final int position, @NonNull PostModel model) {
                holder.setData(model.getPost_title(),model.getPost_desc(),model.getPost_image(),model.getDate());

                mDatabaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       User user = snapshot.getValue(User.class);
                        assert user != null;
                        holder.setUserData(user.getName(),user.getStatus(),user.getThumb_image());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                String likes = holder.likeTxt.getText().toString();
                final int likeValue = Integer.parseInt(likes);
                final String key = this.getRef(position).getKey();

                mDatabaseRef.child("likes").child(key).child("like").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final int likeN = Integer.parseInt(snapshot.getValue()+"");
                        Log.i("likeNum",likeN+"");
                        holder.likeTxt.setText(likeN+"");
                        //check for liked or not
                        mDatabaseRef.child("liked").child(userId).child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("like")){
                                    holder.likeImg.setImageResource(R.drawable.like_image);
                                } else{
                                    holder.likeImg.setImageResource(R.drawable.ic_baseline_favorite_24);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.likeImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mDatabaseRef.child("liked").child(currentUser.getUid()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.i("likeValue",snapshot.hasChild("like")+"");

                                if (snapshot.hasChild("like")){
                                    //already liked so we have to dislike the post
                                    final int lk = Integer.parseInt(holder.likeTxt.getText()+"");
                                    mDatabaseRef.child("liked").child(currentUser.getUid()).child(key).removeValue();
                                    mDatabaseRef.child("likes").child(key).child("like").setValue(lk - 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Log.i("like","post is disliked");
                                                holder.likeTxt.setText((lk - 1)+"");
                                                holder.likeImg.setImageResource(R.drawable.ic_baseline_favorite_24);
                                            }
                                        }
                                    });
                                } else{
                                    //not liked so we have to like the post
                                    mDatabaseRef.child("liked").child(userId).child(key).child("like").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mDatabaseRef.child("likes").child(key).child("like").setValue(likeValue + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            holder.likeImg.setImageResource(R.drawable.like_image);
                                                            holder.likeTxt.setText((likeValue + 1)+"");
                                                            Log.i("favLike","liked successfully");
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


                //add comments to it
                mDatabaseRef.child("comments").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i("number",snapshot.getChildrenCount()+"");
                        long number = snapshot.getChildrenCount();
                        holder.commentTxt.setText(number+"");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.commentImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(),PostDetailsActivity.class);
                        intent.putExtra("BlogId",key);
                        intent.putExtra("Fragment","Fav");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_left,R.anim.no_anim);
                    }
                });

                holder.postImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(),PostDetailsActivity.class);
                        intent.putExtra("BlogId",key);
                        intent.putExtra("Fragment","Fav");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_left,R.anim.no_anim);
                    }
                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_blog_post,parent,false);
                return new PostViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private TextView description;
        private ImageView postImg;
        private TextView date;

        private CircleImageView profileImg;
        private TextView userName;
        private TextView userAbout;
        private TextView followTxt;

        private ImageView likeImg;
        private TextView likeTxt;

        private ImageView commentImg;
        private TextView commentTxt;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.single_post_title);
            description = itemView.findViewById(R.id.single_post_desc);
            date = itemView.findViewById(R.id.single_post_date);
            postImg = itemView.findViewById(R.id.single_post_image);

            profileImg = itemView.findViewById(R.id.single_pro_img);
            userName = itemView.findViewById(R.id.single_com_user_name);
            userAbout = itemView.findViewById(R.id.single_status);
            followTxt = itemView.findViewById(R.id.single_follow);
            followTxt.setVisibility(View.INVISIBLE);

            likeImg = itemView.findViewById(R.id.like_image);
            likeTxt = itemView.findViewById(R.id.like_num);

            commentImg = itemView.findViewById(R.id.commentImage);
            commentTxt = itemView.findViewById(R.id.commentNum);
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
            likeImg.getDrawable().setTint(getResources().getColor(R.color.colorLike,null));
            likeTxt.setText(like);
        }
    }
}