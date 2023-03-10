package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.EditorViewActivity;
import com.bookapp.noribook.Activities.HomeActivity;
import com.bookapp.noribook.Activities.SplashActivity;
import com.bookapp.noribook.Model.ModelSub;
import com.bookapp.noribook.Activities.TextViewActivity;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.RowPdfSubBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterSub extends RecyclerView.Adapter<AdapterSub.HolderSub> {

    private Context context;

    private ArrayList<ModelSub> subArrayList;

    private RowPdfSubBinding binding;

    private FirebaseAuth firebaseAuth;

    public AdapterSub(Context context, ArrayList<ModelSub> subArrayList) {
        this.context = context;
        this.subArrayList = subArrayList;
    }

    @NonNull
    @Override
    public HolderSub onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfSubBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderSub(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderSub holder, int position) {

        firebaseAuth = FirebaseAuth.getInstance();
        ModelSub model = subArrayList.get(position);
        String uid = model.getUid();
        String subNumber =model.getSubNumber();
        String subTitle = model.getSubTitle();
        String date = model.getDate();
        String bookTitle = model.getBookTitle();
        String url = model.getUrl();
        long viewCount = model.getViewCount();
        long recommend = model.getRecommend();
        String subId = model.getSubId();

        holder.subNumberTv.setText(subNumber);
        holder.subTitleTv.setText(subTitle);
        holder.dateTv.setText(date);
        holder.viewCountTv.setText(""+viewCount);
        holder.recommendTv.setText(""+recommend);

        commentCount(bookTitle, subNumber, holder);


        if (!isUserLoggedIn()) { // ???????????? ??????????????? ?????? ??????
            if (holder.getAdapterPosition() < 20) {
                holder.unlockIv.setVisibility(View.GONE);
            } else {
                holder.unlockIv.setVisibility(View.VISIBLE);
            }
        } else { // ???????????? ???????????? ??????
            if (holder.getAdapterPosition() < 40) { // ?????? ????????? 40??? ????????? ??????
                holder.unlockIv.setVisibility(View.GONE);
            } else { // ?????? ????????? 40??? ????????? ??????
                // Firebase Database?????? 'Users' > firebaseAuth.getUid() > 'buyBooks' > bookTitle > subNumber ???????????? ???????????? ???????????????.
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("buyBooks").child(bookTitle).child(subNumber);
                // 'subNumber' ?????? ??????????????? ???????????????.
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) { // 'subNumber' ?????? ???????????? ??????
                            holder.unlockIv.setVisibility(View.GONE);
                        } else { // 'subNumber' ?????? ???????????? ?????? ??????
                            holder.unlockIv.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        long subCheckNumber= Long.parseLong(subNumber);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    if(subCheckNumber>20){
                        Toast.makeText(context, "????????? ????????? 40????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(context, TextViewActivity.class);
                        intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                        intent.putExtra("subNumber", subNumber);
                        context.startActivity(intent);
                    }

                }else {
                    reference.child(firebaseAuth.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String userType = ""+snapshot.child("userType").getValue();
                                    if (userType.equals("editor")){
                                        Intent intent = new Intent(context, EditorViewActivity.class);
                                        intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                        intent.putExtra("subNumber",subNumber);
                                        context.startActivity(intent);
                                    }else {
                                        if(subCheckNumber <= 20){
                                            Intent intent = new Intent(context, TextViewActivity.class);
                                            intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                            intent.putExtra("subNumber",subNumber);
                                            context.startActivity(intent);
                                        }
                                        else if( subCheckNumber > 40){
                                            if(firebaseAuth.getCurrentUser()==null){
                                                Toast.makeText(context, "????????? ????????? 40????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                            // 40??? ??????, ????????? ??? ?????? -> ??????????????? ???????????? ???????????? ???????????? ?????? ??????(Alert), ??????????????? ??? ??? ?????????
                                            else {
                                                MyApplication.subBuyCheck(context, bookTitle, subNumber);

                                            }

                                        }
                                        else if( subCheckNumber >20) {

                                                Intent intent = new Intent(context, TextViewActivity.class);
                                                intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                                intent.putExtra("subNumber", subNumber);
                                                context.startActivity(intent);

                                        }
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }


            }
        });

    }

    private void commentCount(String bookTitle, String subNumber, HolderSub holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks");
        reference.child(bookTitle).child(subNumber).child("Comment").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String commentCount = ""+snapshot.getChildrenCount();
                    holder.commentTv.setText(commentCount);
                }else {
                    holder.commentTv.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//    private void subBuyCheck(String subId, String bookTitle, String subNumber) {
//        firebaseAuth = FirebaseAuth.getInstance();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getUid()).child("SubBooks").child(subId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Boolean isBuySub = snapshot.exists();
//                        if (isBuySub){ //?????? ?????? ??????
//                            Intent intent = new Intent(context, TextViewActivity.class);
//                            intent.putExtra("bookTitle", bookTitle);
////                intent.putExtra("subTitle", subTitle);
//                            intent.putExtra("subNumber",subNumber);
//                            context.startActivity(intent);
//                        }else{ //???????????? ??????
//                            askBuySub(bookTitle, subNumber);
//                        }
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });
//    }
//
//    private void askBuySub(String bookTitle, String subNumber) {
//
//        Toast.makeText(context, "40??? ?????? ?????? alert ????????? ???", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public int getItemCount() {
        return subArrayList.size();
    }

    class HolderSub extends RecyclerView.ViewHolder{

        TextView subNumberTv, subTitleTv, dateTv, viewCountTv, recommendTv, commentTv;

        ImageView unlockIv;

        public HolderSub(@NonNull View itemView) {
            super(itemView);

            commentTv = binding.commentTv;
            unlockIv = binding.unlockIv;
            viewCountTv = binding.viewCountTv;
            recommendTv = binding.recommendTv;
            dateTv = binding.dateTv;
            subTitleTv = binding.subTitleTv;
            subNumberTv = binding.subNumberTv;

        }
    }

    private boolean isUserLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null);
    }
}
