package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Constants;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.ActivityPdfView2Binding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity2 extends AppCompatActivity {

    String bookId;
    String bookTitle;
    String subTitle, subNumber;

    ActivityPdfView2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfView2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("bookTitle");
        subTitle = intent.getStringExtra("subTitle");
        subNumber = intent.getStringExtra("subNumber");

        binding.titleTv.setText(subNumber+". "+subTitle);

        loadBookDetails();
        MyApplication.incrementSubBookViewCount(bookTitle, subNumber);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadBookDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
        ref.child(subNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                     String pdfUrl = ""+snapshot.child("url").getValue();
                     loadPdfFromUrl(pdfUrl);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPdfFromUrl(String pdfUrl) {
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        binding.pdfView.fromBytes(bytes)
                                .enableSwipe(true).swipeHorizontal(false)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set current and total pages int oolbar subtitle
                                        int currentPage = page + 1;
                                        binding.subTitleTv.setText(currentPage + "/" + pageCount);
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}