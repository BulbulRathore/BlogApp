package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MakePost extends AppCompatActivity implements View.OnClickListener {

    private CardView imageCard;
    private TextInputLayout postInputTitle,postInputDesc;
    private ImageView postImageView;

    //firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;
    private Button postBtn;

    private String userId;
    private String uniqueName;

    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);

        //element id's
        imageCard = findViewById(R.id.post_card_view);
        postInputTitle = findViewById(R.id.post_input_title);
        postInputDesc = findViewById(R.id.post_input_desc);
        postBtn = findViewById(R.id.post_btn);
        postImageView = findViewById(R.id.post_img_view);

        //onclickListener
        imageCard.setOnClickListener(this);
        postBtn.setOnClickListener(this);

        //firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userId = currentUser.getUid();
        uniqueName = UUID.randomUUID().toString();
        mDialog = new Dialog(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post_card_view:
                //select the image
                getPostImage();
                break;

            case R.id.post_btn:
               addDataToDatabase();
                break;
        }

    }

    private void addDataToDatabase() {


        mDialog.setTitle("Post is Uploading..");
        mDialog.setCancelable(false);
        mDialog.show();
        mStorageRef.child("post_images").child(uniqueName+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String download_link = uri.toString();
                final String postTitle = postInputTitle.getEditText().getText() + "";
                final String postDesc = postInputDesc.getEditText().getText() + "";
                HashMap<String, Object> blogData = new HashMap<>();
                blogData.put("post_image",download_link);
                blogData.put("post_title",postTitle);
                blogData.put("post_desc",postDesc);
                blogData.put("date", ServerValue.TIMESTAMP);

                final String pushId = mDatabaseRef.child("user_blog").child(userId).push().getKey();
                Log.i("pushId",pushId);
                mDatabaseRef.child("user_blog").child(userId).child(pushId).setValue(blogData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(MakePost.this, "Data is added to database", Toast.LENGTH_SHORT).show();

                            HashMap<String, Object> allBlog = new HashMap<>();
                            allBlog.put("user_id",userId);
                            allBlog.put("blog_title",postTitle);
                            allBlog.put("blog_desc",postDesc);
                            allBlog.put("blog_image",download_link);
                            allBlog.put("date",ServerValue.TIMESTAMP);
                            allBlog.put("blog_id",pushId);
                            mDatabaseRef.child("all_blog").push().setValue(allBlog).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(MakePost.this, "All blogs are added here", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            mDatabaseRef.child("likes").child(pushId).child("like").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.i("like","added 1 like of user");
                                    }
                                }
                            });

                            mDatabaseRef.child("liked").child(currentUser.getUid()).child(pushId).child("like").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.i("postLiked","post is liked by yourself");
                                }
                            });
                            mDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    private void getPostImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"select image for post"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){
            assert data != null;
            final Uri uri = data.getData();
            final StorageReference imageRef = mStorageRef.child("post_images").child(uniqueName+".jpg");
            imageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        //file is uploaded on storage
                        //now upload on database
                        Picasso.get().load(uri).into(postImageView);
                    }
                }
            });

        }
    }
}