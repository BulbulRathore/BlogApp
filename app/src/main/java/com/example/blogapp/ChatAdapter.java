package com.example.blogapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatListViewHolder> {


    private DatabaseReference reference;
    private FirebaseUser currentUser;

    public ChatAdapter(){
        reference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chat_item,parent,false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        reference.child("Chat").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("data",snapshot.getRef().getKey()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView image;
        private TextView userName;
        private TextView msg;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.single_image_chat);
            userName = itemView.findViewById(R.id.single_user_chat);
            msg = itemView.findViewById(R.id.single_msg_chat);
        }
    }

}
