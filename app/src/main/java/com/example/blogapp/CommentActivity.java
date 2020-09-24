package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private String blogImg;
    private String userId;
    private String blogId;

    private TextInputLayout comment;
    private Button commentBtn;
    private ImageView commentImage;

    private RecyclerView commentRecyclerView;

    private DatabaseReference reference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        blogImg = getIntent().getStringExtra("imageUrl");
        userId = getIntent().getStringExtra("userId");
        blogId = getIntent().getStringExtra("blogId");

        comment = findViewById(R.id.comment_txt);
        commentBtn = findViewById(R.id.comment_btn);
        commentImage = findViewById(R.id.comment_img);

        reference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        commentRecyclerView = findViewById(R.id.commentRecyclerView);

        Log.i("imageUrl",blogImg);
        Picasso.get().load(blogImg).into(commentImage);

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentData = comment.getEditText().getText().toString();
                HashMap<String, Object> commentsHashMap = new HashMap<>();
                commentsHashMap.put("userId",currentUser.getUid());
                commentsHashMap.put("comment",commentData);
                commentsHashMap.put("date", ServerValue.TIMESTAMP);

                if(!TextUtils.isEmpty(commentData)){
                    reference.child("comments").child(blogId).push().setValue(commentsHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CommentActivity.this, "comment is added", Toast.LENGTH_SHORT).show();
                                comment.getEditText().setText(null);

                            }
                        }
                    });
                }
            }
        });

        addCommentToView();
    }

    private void addCommentToView() {

        Query query = reference.child("comments").child(blogId).orderByChild("date");
        FirebaseRecyclerOptions<CommentModel> options = new FirebaseRecyclerOptions.Builder<CommentModel>().setQuery(query,CommentModel.class).build();
        FirebaseRecyclerAdapter<CommentModel,CommentViewHolder> adapter = new FirebaseRecyclerAdapter<CommentModel, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder holder, int position, @NonNull CommentModel model) {
                holder.comment.setText(model.getComment());
                holder.userName.setText(model.getUserId());

                reference.child("users").child(model.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User userData = snapshot.getValue(User.class);
                        assert userData != null;
                        holder.userName.setText(userData.getName());
                        Picasso.get().load(userData.getThumb_image()).into(holder.imageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment,parent,false);
                return new CommentViewHolder(view);
            }
        };

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();
        commentRecyclerView.setAdapter(adapter);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageView;
        private TextView userName;
        private TextView comment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.single_comment_img);
            userName = itemView.findViewById(R.id.single_com_user_name);
            comment = itemView.findViewById(R.id.single_com_comment);
        }
    }
}