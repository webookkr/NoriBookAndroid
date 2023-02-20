package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Model.ModelComment;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.RowCommentBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment>{

    Context context;
    private ArrayList<ModelComment> commentArrayList;

    private RowCommentBinding binding;

    private FirebaseAuth firebaseAuth;

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {

        // get data
        ModelComment modelComment = commentArrayList.get(position);
        String comment = modelComment.getComment();
        String date = modelComment.getDate();
        String subNumber = modelComment.getSubNumber();
        String bookTitle = modelComment.getBookTitle();
        String uid = modelComment.getUid();
        String id = modelComment.getId();


        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

//        나머지는 uid에서 가져오기
        loadUserDetail(modelComment, holder);

//        delete
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user 체크
                if(firebaseAuth != null && uid.equals(firebaseAuth.getUid())){
                    deleteComment(modelComment, holder);
                }
                else {
                    Toast.makeText(context, "본인이 아닐시 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

//    댓글 삭제
    private void deleteComment(ModelComment modelComment, HolderComment holder) {
        String subNumber = modelComment.getSubNumber();
        String bookTitle = modelComment.getBookTitle();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("댓글 삭제")
                .setMessage("댓글을 삭제 하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        댓글 삭제 버튼 클릭
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks");
                        ref.child(bookTitle).child(subNumber).child("Comments").child(modelComment.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "댓글 삭제", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "댓글 삭제 실패 :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Users > uid > 프로필 이미지 등 가져오기
    private void loadUserDetail(ModelComment modelComment, HolderComment holder) {
        String uid = modelComment.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileUrl = ""+snapshot.child("profileImage").getValue();
                        String name = ""+snapshot.child("name").getValue();

                        holder.nameTv.setText(name);

                        try{
                            Glide.with(context)
                                    .load(profileUrl)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(holder.profileIv);
                        }
                        catch (Exception e){
                            holder.profileIv.setImageResource(R.drawable.ic_person_gray);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    class HolderComment extends RecyclerView.ViewHolder{

        TextView nameTv, dateTv, commentTv;
        ShapeableImageView profileIv;

        ImageButton deleteBtn;

        public HolderComment(View itemView){
            super(itemView);

            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;
            profileIv = binding.profileIv;
            deleteBtn = binding.deleteBtn;

        }
    }
}
