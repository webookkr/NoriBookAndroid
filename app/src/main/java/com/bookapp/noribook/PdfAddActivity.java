package com.bookapp.noribook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.databinding.ActivityPdfAddBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth ;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 1. attach
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }
    // 1-2 attach : Intent 컨텐츠 가져오기
    private void pdfPickIntent() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
    }

    String title = "", description = "" ;
    private void validateData() {
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "제목이 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)) {
            Toast.makeText(this, "설명이 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        else {
         updateData();
        }
    }

    private void updateData() {
        String uid = firebaseAuth.getUid();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("description", description);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");


    }
}