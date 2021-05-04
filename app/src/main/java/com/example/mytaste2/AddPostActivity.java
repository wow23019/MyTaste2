package com.example.mytaste2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddPostActivity extends AppCompatActivity{

    private static final int PHOTO_REQUEST_GALLERY =15 ;
    private EditText mCaption;
    private Button addPostBtn;
    private ImageView postImage;
    private Uri postImageUri = null;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private ProgressBar progressBar;
    private String currentUserId;
    private FirebaseAuth auth;
    private String random = FieldValue.serverTimestamp().toString();

    //image position now
    int position =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        Toolbar mToolbar = findViewById(R.id.addpost_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Post");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        mCaption = findViewById(R.id.caption_edit);
        addPostBtn = findViewById(R.id.add_post);
        postImage = findViewById(R.id.addpost_image);

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(3,2)
//                        .setMinCropResultSize(512,512)
//                        .start(AddPostActivity.this);
//                Intent intent;

//                if (Build.VERSION.SDK_INT < 19) {
//                    intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.setType("*/*");
//                    startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
//                } else {
//                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
//                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(AddPostActivity.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(AddPostActivity.this , new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
                    }
                    else{
/*
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetUpProfile.this);
*/

                        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                        gallery.setType("image/*");
                        startActivityForResult(gallery,PHOTO_REQUEST_GALLERY);

                    }
                }

            }
        });

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caption = mCaption.getText().toString();

//                if (!caption.isEmpty() && postImageUri !=null){
                if (!caption.isEmpty() && postImageUri !=null){
                    progressBar.setVisibility(View.VISIBLE);

                    StorageReference filePath = storageReference.child("post_images")
                            .child(random + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){

                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String , Object> dataMap = new HashMap<>();
                                        dataMap.put("image" , uri.toString());
                                        dataMap.put("caption" , caption);
                                        dataMap.put("user" , currentUserId);
                                        dataMap.put("time" , FieldValue.serverTimestamp());

                                        firestore.collection("Posts").add(dataMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){
                                                    progressBar.setVisibility(View.VISIBLE);
                                                    Toast.makeText(AddPostActivity.this, "Post Added", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(AddPostActivity.this , MainActivity.class));
                                                    finish();
                                                }else{
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddPostActivity.this, task.getException().toString() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(AddPostActivity.this, "Please Select Image and Write Caption ...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PHOTO_REQUEST_GALLERY) {
            postImageUri=data.getData();
            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),postImageUri);
                postImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
