package com.bookapp.noribook.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bookapp.noribook.Adapter.AdapterSub;
import com.bookapp.noribook.Model.ModelSub;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityPdfDetailBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TextDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    //pdf id , get from intent

    String bookId, bookTitle, bookUrl, subNumber;

    Context context;

    private ArrayList<ModelSub> subArrayList;

    private AdapterSub adapterSub;

    private FirebaseAuth firebaseAuth ;

    boolean isInMyFavorite = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        // get data from adapterpdfUser
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadBookDetails();
        // 이 페이지 시작시마다 increase view count 늘리기
        MyApplication.incrementBookViewCount(bookTitle);
        MyApplication.decrementBookViewCount(bookTitle);

        //adapter(sub books)
        loadSubBooks();

        //go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null ){
                    Toast.makeText(TextDetailActivity.this, "로그인되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    if (isInMyFavorite){
                        MyApplication.removeFromFavorite(TextDetailActivity.this, bookId, bookTitle);

                    }else{
                        MyApplication.addFavorite(TextDetailActivity.this, bookId, bookTitle);
                    }
                }

            }
        });

//        페이지 이동 버튼 클릭
//        binding.shiftBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pageShift();
//            }
//        });
//
//    }

//    private void pageShift() {
//        String subNumber = ""+binding.pageEt.getText().toString().trim();
//        if(TextUtils.isEmpty(subNumber)){
////            Toast.makeText(context, "페이지가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
//        }else{
//            Intent intent = new Intent(context, TextViewActivity.class);
//            intent.putExtra("bookTitle", bookTitle);
////                intent.putExtra("subTitle", subTitle);
//            intent.putExtra("subNumber",subNumber);
//            context.startActivity(intent);
//        }

    }

    private void loadSubBooks() {
        subArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subArrayList.clear();

                for (DataSnapshot ds:snapshot.getChildren()) {
                    ModelSub model = ds.getValue(ModelSub.class);
                    subArrayList.add(model);
                }
                adapterSub = new AdapterSub(TextDetailActivity.this, subArrayList);
                binding.pdfSubRv.setAdapter(adapterSub);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                     String title = ""+snapshot.child("title").getValue();
                     String categoryTitle = ""+snapshot.child("categoryTitle").getValue();
                     String date = ""+snapshot.child("date").getValue();
                     String description = ""+snapshot.child("description").getValue();
                     String viewCount = ""+snapshot.child("viewCount").getValue();
                     String url = ""+snapshot.child("url").getValue();
                     String recommendCount = ""+snapshot.child("recommendCount").getValue();

                        Glide.with(TextDetailActivity.this)
                                .load(url)
                                .into(binding.bookIv);


                     // set data
                        binding.bookTitleTv.setText(title);
                        binding.descriptionTv.setText(description);
                        binding.categoryTv.setText(categoryTitle);
                        binding.favoriteCountTv.setText(recommendCount);
                        binding.viewsTv.setText(viewCount.replace("null","n/a"));

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists(); //존재하면 true, 존재하지 않으면 false
                        if (isInMyFavorite){
                            // 존재
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
                            binding.favoriteBtn.setText("선호작 제거");
                        }else{
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_border_white, 0, 0, 0);
                            binding.favoriteBtn.setText("선호작 추가");

                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}