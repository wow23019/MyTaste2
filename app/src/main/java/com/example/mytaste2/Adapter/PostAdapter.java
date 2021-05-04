package com.example.mytaste2.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytaste2.CommentsActivity;
import com.example.mytaste2.Model.Comment;
import com.example.mytaste2.Model.Post;
import com.example.mytaste2.Model.Users;
import com.example.mytaste2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private static final String TAG =PostAdapter.class.getSimpleName() ;
    private List<Post> mList;
    private List<Users> userList;
    private Activity context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public PostAdapter(Activity context , List<Post> mList , List<Users> usersList){
        this.context = context;
        this.mList = mList;
        this.userList = usersList;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_post , parent , false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        Post post = mList.get(position);
        holder.setmPostImage(post.getImage());
        holder.setmCaption(post.getCaption()+ userList.get(position).getImage());

        long milliseconds = post.getTime().getTime();
        String date = DateFormat.format("MM/dd/yyyy" , new Date(milliseconds)).toString();
        holder.setmDate(date);

        String username = userList.get(position).getName();
        String profile = userList.get(position).getImage();
        holder.setmUserName(username);
        holder.setmProfilePic(profile);
        Log.d(TAG, "onBindViewHolder: "+profile);

        //like btn
        String blogPostId = post.PostId;

        String currentUserId = auth.getCurrentUser().getUid();
        holder.deleteBtn.setVisibility(View.INVISIBLE);
        holder.deleteBtn.setClickable(false);

        if (currentUserId.equals(post.getUser())) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setEnabled(true);
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Delete")
                               .setMessage("Are You Sure ?")
                               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       firestore.collection("Posts/" + blogPostId + "/comment").get()
                                               .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                     if (task.isSuccessful()){
                                                         for (QueryDocumentSnapshot snapshot : task.getResult()){
                                                             firestore.collection("Posts/" + blogPostId + "/comment")
                                                                     .document(snapshot.getId()).delete();
                                                         }
                                                     }
                                                   }
                                               });
                                       firestore.collection("Posts/" + blogPostId + "/Likes").get()
                                               .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                       if (task.isSuccessful()){
                                                           for (QueryDocumentSnapshot snapshot : task.getResult()){
                                                               firestore.collection("Posts/" + blogPostId + "/Likes")
                                                                       .document(snapshot.getId()).delete();
                                                           }
                                                       }
                                                   }
                                               });
                                       firestore.collection("Posts").document(blogPostId).delete();
                                       mList.remove(position);
                                       notifyDataSetChanged();
                                       Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                   }
                               })
                            .setNegativeButton("No" , null);
                   alertDialog.show();
                }
            });
        }

        holder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()){
                            Map<String , Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp" , FieldValue.serverTimestamp());
                            firestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                        }else{
                            firestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });
     firestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error == null) {
                        if (value.exists()) {
                            holder.mLikeBtn.setImageDrawable(context.getDrawable(R.drawable.after_liked));
                        } else {
                            holder.mLikeBtn.setImageDrawable(context.getDrawable(R.drawable.before_liked));
                        }
                    }
            }
        });
        //like count
     firestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
             @Override
             public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                 if (error == null) {
                     if (!value.isEmpty()) {
                         int count = value.size();
                         holder.setmLikeCount(count);
                     } else {
                         holder.setmLikeCount(0);
                     }
                 }
             }
         });

     //comment count
        firestore.collection("Posts/" + blogPostId + "/comment").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!value.isEmpty()) {
                        int count = value.size();
                        holder.setmCommentCount(count);
                    } else {
                        holder.setmCommentCount(0);
                    }
                }
            }
        });

         holder.setmCommentBtn(blogPostId);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView mPostImage , mProfilePic , mLikeBtn , mCommentBtn;
        TextView mUserName , mDate , mCaption , mLikeCount,mCommentCount;
        ImageButton deleteBtn;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = mView.findViewById(R.id.like_image);
            deleteBtn = mView.findViewById(R.id.imageButton);
        }

        public void setmCommentBtn(String postId){
            mCommentBtn = mView.findViewById(R.id.comment_btn);
            mCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context , CommentsActivity.class);
                    intent.putExtra("postid" , postId);
                    context.startActivity(intent);
                }
            });
        }

        public void setmCommentCount(int count){
            mCommentCount = mView.findViewById(R.id.comment_text);
            mCommentCount.setText(count + " Comments");
        }

        public void setmLikeCount(int count){
            mLikeCount = mView.findViewById(R.id.like_text);
            mLikeCount.setText(count + " Likes");
        }
        public void setmUserName(String userName){
            mUserName = mView.findViewById(R.id.text_username);
            mUserName.setText(userName);
        }
        public void setmDate(String date){
            mDate = mView.findViewById(R.id.text_date);
            mDate.setText(date);
        }
        public void setmPostImage(String image){
            mPostImage = mView.findViewById(R.id.post_image);
            Glide.with(context).load(image).into(mPostImage);
        }
        public void setmProfilePic(String profile){
            mProfilePic = mView.findViewById(R.id.profile_pic);
            Glide.with(context).load(profile).into(mProfilePic);
        }
        public void setmCaption(String caption){
            mCaption = mView.findViewById(R.id.caption_text);
            mCaption.setText(caption);
        }
    }
}
