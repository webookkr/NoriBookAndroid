package com.bookapp.noribook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bookapp.noribook.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    //pdf id , get from intent

    String bookId, bookTitle;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");

        loadBookDetails();
        // 이 페이지 시작시마다 increase view count 늘리기
        MyApplication.incrementBookViewCount(bookTitle);


        //go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // handle click, read pdf : open to view pdf
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                intent1.putExtra("bookTitle",bookTitle);
                startActivity(intent1);
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

                     MyApplication.loadPdfFromUrl(context, ""+url, binding.pdfView, binding.progressBar);
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
}