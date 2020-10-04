package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.TimeFormat;
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

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private CircleImageView imageView;
    private TextView userName;
    private TextView status;
    private RecyclerView recyclerView;
    private TextInputLayout edt_msg;
    private ImageView send_img;

    //firebase
    private FirebaseUser currentUser;
    private DatabaseReference mDatabaseRef;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        toolbar = findViewById(R.id.messageToolbar);
        userName = findViewById(R.id.message_user_name);
        status = findViewById(R.id.message_status);
        imageView = findViewById(R.id.message_img_view);
        recyclerView = findViewById(R.id.msg_recycler_view);
        edt_msg = findViewById(R.id.msg_txt_input_layout);
        send_img = findViewById(R.id.msg_send_img);

        setSupportActionBar(toolbar);

       userId = getIntent().getStringExtra("userId");

       //firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        setDataToToolBar();

        send_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessageSaveToDatabase();
            }
        });

        showDataToRecyclerView();
    }

    private void showDataToRecyclerView() {

        Query query = mDatabaseRef.child("Chat").child(currentUser.getUid()).child(userId).orderByChild("date");
        FirebaseRecyclerOptions<ChatModel> options = new FirebaseRecyclerOptions.Builder<ChatModel>().setQuery(query,ChatModel.class).build();
        FirebaseRecyclerAdapter<ChatModel,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull ChatModel model) {

                if (model.getType().equals("sent")){
                    holder.msgView.setText(model.getMessage());
                    long date = model.getDate();

                    TimeAgo timeAgo = new TimeAgo();
                    holder.dateView.setText(timeAgo.getTime(date));
                    holder.cardView.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.chat_bg,null));
                    holder.layout.setHorizontalGravity(Gravity.START);
                } else {
                    holder.msgView.setText(model.getMessage());
                    TimeAgo timeAgo = new TimeAgo();
                     String time = timeAgo.getTime(model.getDate());
                    holder.dateView.setText(time);
                    holder.cardView.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.chat_bg_2,null));
                    holder.layout.setHorizontalGravity(Gravity.END);
                }

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_msg_layout,parent,false);
                return new ChatViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void getMessageSaveToDatabase() {
        final String msg = edt_msg.getEditText().getText().toString();
        if (!TextUtils.isEmpty(msg)){
            final HashMap<String,Object> chatHashMap = new HashMap<>();
            chatHashMap.put("message",msg);
            chatHashMap.put("date", ServerValue.TIMESTAMP);
            chatHashMap.put("type","sent");
            mDatabaseRef.child("Chat").child(currentUser.getUid()).child(userId).push().setValue(chatHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        HashMap<String,Object> chatMap = new HashMap<>();
                        chatMap.put("message",msg);
                        chatMap.put("date",ServerValue.TIMESTAMP);
                        chatMap.put("type","receive");
                        mDatabaseRef.child("Chat").child(userId).child(currentUser.getUid()).push().setValue(chatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.i("chat","chat is added to database");
                                    edt_msg.getEditText().setText("");
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void setDataToToolBar() {
        mDatabaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userName.setText(user.getName());
                status.setText(user.getStatus());
                Picasso.get().load(user.getThumb_image()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        private TextView msgView;
        private TextView dateView;
        private CardView cardView;
        private LinearLayout layout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            msgView = itemView.findViewById(R.id.single_chat_msg);
            dateView = itemView.findViewById(R.id.single_chat_date);
            cardView = itemView.findViewById(R.id.single_card_view);
            layout = itemView.findViewById(R.id.single_chat_layout);
        }
    }
}