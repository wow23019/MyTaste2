package com.example.mytaste2;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpProfile extends AppCompatActivity {

    private static final int PHOTO_REQUEST_GALLERY = 15;
    private CircleImageView circleImageView;
    private EditText mName;
    private Button mSave;
    private Uri mImageUri = null;
    private boolean isChanged = false;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storage;
    private String Uid;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);

        Toolbar mToolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        progressBar = findViewById(R.id.setUpProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        circleImageView = findViewById(R.id.circleImageView);
        mName = findViewById(R.id.enter_name);
        mSave = findViewById(R.id.save_profile);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        Uid = auth.getCurrentUser().getUid();

        firestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String profile = task.getResult().getString("image");

                        mImageUri = Uri.parse(profile);
                        mName.setText(name);
                        Log.d(profile, "onComplete: ");
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.profile);
                        Glide.with(SetUpProfile.this).load(profile).into(circleImageView);
                    }
                }
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(SetUpProfile.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(SetUpProfile.this , new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
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

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String name = mName.getText().toString();
                if (!name.isEmpty() && mImageUri != null){
                    StorageReference imageRef = storage.child("Profile_pics").child(Uid + ".jpg");
                    if (isChanged){
                        imageRef.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri){
                                            saveToFireStore(task , name ,uri);
                                        }
                                    });

                                }else{
                                    Toast.makeText(SetUpProfile.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        saveToFireStore(null , name , mImageUri);
                    }
                }else{
                    Toast.makeText(SetUpProfile.this, "Please Select Image and write your name", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name, Uri downloadUri) {

        HashMap<String , Object> map = new HashMap<>();
        map.put("name" , name);
        map.put("image" , downloadUri.toString());
        firestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpProfile.this, "profile Settings Saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetUpProfile.this , MainActivity.class));
                    finish();
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpProfile.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                mImageUri = result.getUri();
//                circleImageView.setImageURI(mImageUri);
//                isChanged = true;
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//                Toast.makeText(this, error.getMessage() , Toast.LENGTH_SHORT).show();
//            }
//        }

        if (resultCode == RESULT_OK && requestCode == PHOTO_REQUEST_GALLERY) {
            mImageUri=data.getData();
            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),mImageUri);
                circleImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}