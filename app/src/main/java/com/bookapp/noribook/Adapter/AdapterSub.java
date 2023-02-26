package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Model.ModelSub;
import com.bookapp.noribook.Activities.TextViewActivity;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.RowPdfSubBinding;
import com.google.firebase.auth.FirebaseAuth;
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
        long recommendCount = model.getRecommendCount();
        String subId = model.getSubId();

        holder.subNumberTv.setText(subNumber);
        holder.subTitleTv.setText(subTitle);
        holder.dateTv.setText(date);
        holder.viewCountTv.setText(""+viewCount);
        holder.recommendTv.setText(""+recommendCount);


        if (firebaseAuth.getCurrentUser() != null){
            firebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("buyBooks").child(bookTitle).child(subNumber)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean isBuySub= snapshot.exists();
                            if(isBuySub = false){
                                holder.unlockIv.setImageResource(R.drawable.ic_lock_gray);
                                notifyDataSetChanged();


                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long subCheckNumber= Long.parseLong(subNumber);
                if(subCheckNumber <= 5){
                    Intent intent = new Intent(context, TextViewActivity.class);
                    intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                    intent.putExtra("subNumber",subNumber);
                    context.startActivity(intent);
                }
                else if( subCheckNumber > 10){
                    if(firebaseAuth.getCurrentUser()==null){
                        Toast.makeText(context, "로그인 하시면 40편까지 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                    // 40편 초과, 로그인 된 경우 -> 구매했는지 체크하여 구매하지 않았으면 구매 권유(Alert), 구매했으면 볼 수 있도록
                    else {
                        MyApplication.subBuyCheck(context, bookTitle, subNumber);

                    }

                }
                else if( subCheckNumber >5) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(context, "로그인 하시면 40편까지 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(context, TextViewActivity.class);
                        intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                        intent.putExtra("subNumber", subNumber);
                        context.startActivity(intent);

                    }
                }
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
//                        if (isBuySub){ //구매 내역 존재
//                            Intent intent = new Intent(context, TextViewActivity.class);
//                            intent.putExtra("bookTitle", bookTitle);
////                intent.putExtra("subTitle", subTitle);
//                            intent.putExtra("subNumber",subNumber);
//                            context.startActivity(intent);
//                        }else{ //구매내역 없음
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
//        Toast.makeText(context, "40편 이상 구매 alert 테스트 중", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public int getItemCount() {
        return subArrayList.size();
    }

    class HolderSub extends RecyclerView.ViewHolder{

        TextView subNumberTv, subTitleTv, dateTv, viewCountTv, recommendTv;

        ImageView unlockIv;

        public HolderSub(@NonNull View itemView) {
            super(itemView);

            unlockIv = binding.unlockIv;
            viewCountTv = binding.viewCountTv;
            recommendTv = binding.recommendTv;
            dateTv = binding.dateTv;
            subTitleTv = binding.subTitleTv;
            subNumberTv = binding.subNumberTv;

        }
    }
}
