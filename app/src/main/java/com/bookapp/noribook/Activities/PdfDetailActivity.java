package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Adapter.AdapterSub;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.Model.ModelSub;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityPdfDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    //pdf id , get from intent

    String bookId, bookTitle, bookUrl;

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

        //adapter(sub books)
        loadSubBooks();

        //go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
//
//
//        // handle click, read pdf : open to view pdf
//        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
//                intent1.putExtra("bookId", bookId);
//                intent1.putExtra("bookTitle",bookTitle);
//                startActivity(intent1);
//            }
//        });
//
//        binding.readBookBtn2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent2 = new Intent(PdfDetailActivity.this, PdfViewActivity2.class);
//                intent2.putExtra("bookId", bookId);
//                intent2.putExtra("bookTitle",bookTitle);
//                startActivity(intent2);
//            }
//        });
        // add remove favorite 클릭
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() ==null ){
                    Toast.makeText(context, "로그인되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    if (isInMyFavorite){
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);

                    }else{
                        MyApplication.addFavorite(PdfDetailActivity.this, bookId, bookTitle);
                    }
                }

            }
        });

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
                adapterSub = new AdapterSub(PdfDetailActivity.this, subArrayList);
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

                     MyApplication.loadPdfFromUrl(""+url, binding.pdfView, binding.progressBar);
                     MyApplication.loadPdfSize(""+url,binding.sizeTv);

                     // set data
                        binding.bookTitleTv.setText(title);
                        binding.dateTv.setText(date);
                        binding.descriptionTv.setText(description);
                        binding.categoryTv.setText(categoryTitle);
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