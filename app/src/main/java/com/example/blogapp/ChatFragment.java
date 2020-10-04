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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private DatabaseReference reference;
    private String user_id;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        reference = FirebaseDatabase.getInstance().getReference();
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getData();

    }

    public void getData(){

        final Query query = reference.child("Chat").child(user_id);

        FirebaseRecyclerOptions<ChatModel> options = new FirebaseRecyclerOptions.Builder<ChatModel>().setQuery(query,ChatModel.class).build();
        FirebaseRecyclerAdapter<ChatModel,ChatListViewHolder> adapter = new FirebaseRecyclerAdapter<ChatModel, ChatListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatListViewHolder holder, int position, @NonNull ChatModel model) {
                getRef(position).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i("data",snapshot.getKey()+"");
                        final String id = snapshot.getKey();
                        reference.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user != null){
                                    Log.i("userName",user.getName());
                                    holder.userName.setText(user.getName());
                                    Picasso.get().load(user.getThumb_image()).into(holder.image);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        Query query1 = reference.child("Chat").child(user_id).child(id).limitToLast(1);
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.i("valuedata",snapshot.getChildren().iterator().next().getValue(ChatModel.class).getMessage()+"");
                                holder.msg.setText(snapshot.getChildren().iterator().next().getValue(ChatModel.class).getMessage());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(),MessageActivity.class);
                                intent.putExtra("userId",id);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chat_item,parent,false);
                return new ChatListViewHolder(view);
            }
        };

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.startListening();
        chatRecyclerView.setAdapter(adapter);
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