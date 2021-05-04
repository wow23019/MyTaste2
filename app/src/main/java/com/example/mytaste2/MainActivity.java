package com.example.mytaste2;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytaste2.Adapter.PostAdapter;
import com.example.mytaste2.Model.Post;
import com.example.mytaste2.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private String currentUserId;
    private FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private PostAdapter adapter;
    private List<Post> mList;
    private Query firstQuery;
    private List<Users> usersList;
    private ListenerRegistration listenerRegistration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int hasPermisson = checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if(hasPermisson !=
                PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }


        Toolbar mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyTaste2");

        mRecyclerView = findViewById(R.id.recyclerview);
        mList = new ArrayList<>();
        usersList = new ArrayList<>();
        adapter = new PostAdapter(MainActivity.this , mList ,usersList);

        fab = findViewById(R.id.fab_btn);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this , AddPostActivity.class));
            }
        });

        if (mAuth.getCurrentUser() != null){

           mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
               @Override
               public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                   super.onScrolled(recyclerView, dx, dy);
                   Boolean isReached = !mRecyclerView.canScrollVertically(1);
                   if (isReached)
                       Toast.makeText(MainActivity.this, "Reached Bottom", Toast.LENGTH_SHORT).show();
               }
           });
           firstQuery = firestore.collection("Posts").orderBy("time" , Query.Direction.DESCENDING);
           listenerRegistration = firstQuery.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
               @Override
               public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                     for (DocumentChange documentChange : value.getDocumentChanges()){
                         if (documentChange.getType() == DocumentChange.Type.ADDED){
                            String postId =  documentChange.getDocument().getId();
                            String postuserId = documentChange.getDocument().getString("user");
                            Post model = documentChange.getDocument().toObject(Post.class).withId(postId);

                            firestore.collection("Users").document(postuserId).get()
                                     .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                         @Override
                                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                             Users users = task.getResult().toObject(Users.class);
                                             usersList.add(users);
                                             mList.add(model);
                                             adapter.notifyDataSetChanged();
                                         }
                                     });
                         }else{
                             adapter.notifyDataSetChanged();
                         }
                     }
                     listenerRegistration.remove();
               }
           });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(MainActivity.this , LoginActivity.class));
            finish();
        }else{
            currentUserId = mAuth.getCurrentUser().getUid();
            firestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if (task.isSuccessful()){
                       if (!task.getResult().exists()){
                           startActivity(new Intent(MainActivity.this , SetUpProfile.class));
                           finish();
                       }
                   }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.my_profile){
            startActivity(new Intent(MainActivity.this , SetUpProfile.class));
        }
        else if (item.getItemId() == R.id.sign_out){
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this , LoginActivity.class));
            finish();
        }
        return true;
    }
}