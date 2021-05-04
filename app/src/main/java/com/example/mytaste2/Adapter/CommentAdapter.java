package com.example.mytaste2.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytaste2.Model.Comment;
import com.example.mytaste2.Model.Users;
import com.example.mytaste2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.Date;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final String TAG = CommentAdapter.class.getSimpleName();
    List<Comment> commentList;
    List<Users> usersList;
    Activity context;
    String postId;
    private FirebaseFirestore firestore;
    public CommentAdapter(List<Comment> commentList, Activity context, List<Users> usersList , String postId){
        this.commentList = commentList;
        this.context = context;
        this.usersList = usersList;
        this.postId = postId;
    }
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.each_comment , parent , false);
       firestore = FirebaseFirestore.getInstance();
       return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.setComment_text(comment.getComment());
        String profile = usersList.get(position).getImage();
        long milliseconds = comment.getTime().getTime();
        String date = DateFormat.format("MM/dd/yyyy" , new Date(milliseconds)).toString();
        holder.setComment_date(date);
        holder.setProfile_pic(profile);
        Log.d(TAG, "onBindViewHolder: "+profile);

        String commentId = comment.CommentId;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        holder.setComment_text(comment.getComment()+commentId);
        holder.setComment_text(comment.getComment());
//        if (currentUserId.equals(comment.getUser())){
//            holder.deleteBtn.setVisibility(View.VISIBLE);
//            holder.deleteBtn.setClickable(true);
//            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//
//                     firestore.collection("Posts/" + postId + "/comment").document(commentId).delete()
//                              .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                  @Override
//                                  public void onComplete(@NonNull Task<Void> task) {
//                                     if (task.isSuccessful()){
//                                         Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
//                                     }else{
//                                         Toast.makeText(context, "error" , Toast.LENGTH_SHORT).show();
//                                     }
//                                  }
//                              });
//                }
//            });
//        }

        if (currentUserId.equals(comment.getUser())) {
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
                                    firestore.collection("Posts/" + postId + "/comment").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        for (QueryDocumentSnapshot snapshot : task.getResult()){
                                                            firestore.collection("Posts/" + postId + "/comment")
                                                                    .document(commentId).delete();
                                                        }
                                                    }
                                                }
                                            });
                                    commentList.remove(position);
//                                    firestore.collection("Posts").document(postId)
//                                            .collection("comment").document(commentId).delete();
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No" , null);
                    alertDialog.show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView profile_pic;
        TextView comment_text,comment_date;
        ImageButton deleteBtn;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            deleteBtn = mView.findViewById(R.id.imageButton2);
        }

        public void setProfile_pic(String url){
            profile_pic = mView.findViewById(R.id.profile_pic_comment);
            Glide.with(context).load(url).into(profile_pic);
        }
        public void setComment_text(String text){
            comment_text = mView.findViewById(R.id.comment_textview);
            comment_text.setText(text);
        }

        public void setComment_date(String date){
            comment_date = mView.findViewById(R.id.comment_tv_date);
            comment_date.setText(date);
        }
    }
}
