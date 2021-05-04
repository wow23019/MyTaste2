package com.example.mytaste2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mytaste2.Adapter.CommentAdapter;
import com.example.mytaste2.Model.Comment;
import com.example.mytaste2.Model.Post;
import com.example.mytaste2.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private RecyclerView commentRecyclerView;
    private EditText commentEdit;
    private Button addCommentBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private CommentAdapter adapter;
    private List<Comment> list;
    private Query firstQuery;
    private ListenerRegistration listenerRegistration;
    private String postId;
    private String currentUserId;
    private List<Users> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

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


        commentEdit = findViewById(R.id.comment_Text);
        commentRecyclerView= findViewById(R.id.comment_recyclerview);
        addCommentBtn = findViewById(R.id.comment_add_btn);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        list = new ArrayList<>();
        usersList = new ArrayList<>();
        adapter = new CommentAdapter(list ,this , usersList , postId);
        commentRecyclerView.setHasFixedSize(true);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setAdapter(adapter);

        currentUserId = auth.getCurrentUser().getUid();
        postId = getIntent().getStringExtra("postid");

        firestore.collection("Posts/" + postId + "/comment").addSnapshotListener(CommentsActivity.this , new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
              if (error == null) {
                  for (DocumentChange change : value.getDocumentChanges()) {
                      if (change.getType() == DocumentChange.Type.ADDED) {
                          String commentId = change.getDocument().getId();
                          Comment commentModel = change.getDocument().toObject(Comment.class).withId(commentId);
                          String userId = change.getDocument().getString("user");

                          firestore.collection("Users").document(userId).get()
                                  .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                      @Override
                                      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                          if (task.isSuccessful()) {
                                              Users users = task.getResult().toObject(Users.class);
                                              usersList.add(users);
                                              list.add(commentModel);
                                              adapter.notifyDataSetChanged();
                                          }
                                      }
                                  });
                      } else {
                          adapter.notifyDataSetChanged();
                      }
                  }
              }
            }
        });
        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentEdit.getText().toString();

                if (!comment.isEmpty()){

                    HashMap<String , Object> commentMap = new HashMap<>();
                    commentMap.put("comment" , comment);
                    commentMap.put("time" , FieldValue.serverTimestamp());
                    commentMap.put("user" ,auth.getCurrentUser().getUid());
                    firestore.collection("Posts/" + postId + "/comment").document(postId+System.currentTimeMillis()).set(commentMap)
                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful())
                                       Toast.makeText(CommentsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                                   else
                                       Toast.makeText(CommentsActivity.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                               }
                           });
                }else{
                    Toast.makeText(CommentsActivity.this, "Please Write Your Comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}