package com.example.blogapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private CircleImageView profileImage, profileChooser;
    private Button logOutBtn;
    private View name_view, about_view;

    private ImageView userImage,userPen,aboutImage,aboutPen;
    private TextView userTxt,aboutTxt;
    private TextInputLayout edtUserName, edtAbout;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private StorageReference mStorageRef;

    private ProgressDialog mDialog;

    //variables
    String userId;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.acc_profile_img);
        profileChooser = view.findViewById(R.id.acc_pro_img_chooser);
        logOutBtn = view.findViewById(R.id.log_out_btn);
        name_view = view.findViewById(R.id.name_view);
        about_view = view.findViewById(R.id.about_view);

        userImage = view.findViewById(R.id.user_image);
        userPen = view.findViewById(R.id.user_pen_edt);
        aboutImage = view.findViewById(R.id.about_info);
        aboutPen = view.findViewById(R.id.about_change);
        userTxt = view.findViewById(R.id.user_name_txt);
        aboutTxt = view.findViewById(R.id.about_txt);
        edtUserName = view.findViewById(R.id.acc_name_edit);
        edtAbout = view.findViewById(R.id.acc_about_edit);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();
        showDataOnAcc();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        profileChooser.setOnClickListener(this);
        name_view.setOnClickListener(this);
        about_view.setOnClickListener(this);
        logOutBtn.setOnClickListener(this);
        userPen.setOnClickListener(this);
        aboutPen.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.acc_pro_img_chooser:
                addImage();
                break;

            case R.id.log_out_btn:
                mAuth.signOut();
                Toast.makeText(getContext(),"Log Out Successfully",Toast.LENGTH_LONG).show();
                moveToMainActivity();
                break;
            case R.id.name_view:
                Toast.makeText(getContext(), "Name is clicked", Toast.LENGTH_SHORT).show();
                editUserName();
                break;
            case R.id.about_view:
                Toast.makeText(getContext(), "about is clicked", Toast.LENGTH_SHORT).show();
                editAbout();
                break;

            case R.id.user_pen_edt:
                //user editing
                saveUserName();
                break;
            case R.id.about_change:
                //about editing
                saveAbout();
                break;
        }
    }

    private void addImage() {
        //access gallery
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"select Image"),1);
    }

    private void editAbout() {
        aboutImage.setVisibility(View.INVISIBLE);
        aboutTxt.setVisibility(View.INVISIBLE);
        edtAbout.setVisibility(View.VISIBLE);
        aboutPen.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
    }

    private void editUserName() {
        userImage.setVisibility(View.INVISIBLE);
        userTxt.setVisibility(View.INVISIBLE);
        edtUserName.setVisibility(View.VISIBLE);
        userPen.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
    }

    private void saveUserName() {
        if(userPen.getDrawable() == ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_edit_24,null)){

        } else{
            final String userName = edtUserName.getEditText().getText() + "";
            if (!TextUtils.isEmpty(userName)){
                //save the user name to database
                reference.child("users").child(userId).child("name").setValue(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                           userImage.setVisibility(View.VISIBLE);
                           userTxt.setText(userName);
                           userTxt.setVisibility(View.VISIBLE);
                           edtUserName.setVisibility(View.INVISIBLE);
                           userPen.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_edit_24,null));
                        }
                    }
                });
            }
        }
    }

    private void saveAbout() {
        if(userPen.getDrawable() == ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_edit_24,null)){

        } else{
            final String about = edtAbout.getEditText().getText() + "";
            if(!TextUtils.isEmpty(about)){
                //save about to status to database
                String userId = currentUser.getUid();
                reference.child("users").child(userId).child("status").setValue(about).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            aboutImage.setVisibility(View.VISIBLE);
                            aboutTxt.setVisibility(View.VISIBLE);
                            aboutTxt.setText(about);
                            aboutPen.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_edit_24,null));
                            edtAbout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }
    }

    private void showDataOnAcc() {
        String userId = currentUser.getUid();
        reference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Log.i("userName & about",user.getName() + " " + user.getStatus());
                edtUserName.getEditText().setText(user.getName());
                edtAbout.getEditText().setText(user.getStatus());
                userTxt.setText(user.getName());
                aboutTxt.setText(user.getStatus());
                String userImg = user.getImage();
                if(!userImg.equals("default")){
                    Picasso.get().load(user.getImage()).into(profileImage);
                } else{
                   // Picasso.get().load(R.drawable.ic_baseline_account_circle_24).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(getActivity(),MainActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        getActivity().finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            assert data != null;
            Uri uri = data.getData();
            CropImage.activity(uri).setAspectRatio(1,1).start(Objects.requireNonNull(getContext()),this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == Activity.RESULT_OK){

                mDialog = new ProgressDialog(getContext());
                mDialog.setTitle("Uploading image...");
                mDialog.setMessage("Wait until image is uploading...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                assert result != null;
                Uri resultUri = result.getUri();
                Bitmap thumb_bitmap = null;

                File file = new File(resultUri.getPath());
                try {
                    thumb_bitmap = new Compressor(getContext()).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_array = baos.toByteArray();

                StorageReference filePath = mStorageRef.child("profile_images").child(userId + ".jpg");
                final StorageReference thumbPath = mStorageRef.child("thumb_images").child(userId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            StorageReference storageReference = mStorageRef.child("profile_images").child(userId+".jpg");
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String download_link = uri.toString();

                                    UploadTask uploadTask = thumbPath.putBytes(thumb_array);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                StorageReference thumbRef = mStorageRef.child("thumb_images").child(userId+".jpg");
                                                thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String link = uri.toString();
                                                        reference.child("users").child(userId).child("thumb_image").setValue(link).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(getContext(),"thumb_image is uploaded",Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });

                                    reference.child("users").child(userId).child("image").setValue(download_link).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(getContext(),"image is uploaded",Toast.LENGTH_LONG).show();
                                                mDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

            }
        }
    }

//    void createDialog(String data, int value){
//
//        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext())
//                .setTitle(data)
//                .setView(R.layout.dialog_view)
//                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                }).setCancelable(false).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.dialog_bg,null));
//
//        if(value == 1){
//
//            dialogBuilder.setPositiveButton("save", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//
//                }
//            });
//        } else{
//            dialogBuilder.setPositiveButton("save", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//
//                }
//            });
//        }
//        dialogBuilder.show();
//    }
}