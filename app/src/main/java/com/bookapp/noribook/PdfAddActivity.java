package com.bookapp.noribook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth ;

    private ProgressDialog progressDialog;

    private Uri pdfUri;

    // 2-2 category 담을 어레이
    private ArrayList<ModelCategory> categoryArrayList;

    int PDF_PICK_CODE = 1000;

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


        // 2-1 category 선택
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    // 2-2 category 선택
    private void categoryPickDialog() {
        categoryArrayList = new ArrayList<>();

        // firebase db에서 category 데이터 가져와 categoryArrayList에 담기
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // String 카테ㅔ고리array를 arraylist에서 가져오기
        String[] categoriesArray = new String[categoryArrayList.size()];
        for(int i = 0; i < categoryArrayList.size(); i++) {
            categoriesArray[i] = categoryArrayList.get(i).getCategory();
        }

        //alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카테고리 선택")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String category = categoriesArray[i];
                        binding.categoryTv.setText(category);
                    }
                }).show();
    }

    // 3-2 submit
    private void validateData() {
        String title = binding.titleEt.getText().toString().trim();
        String description = binding.descriptionEt.getText().toString().trim();
        String category = binding.categoryTv.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "제목이 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)) {
            Toast.makeText(this, "설명이 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(category)) {
            Toast.makeText(this, "카테고리가 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri == null) {
            Toast.makeText(this, "pdf가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
        else {
         updateData();
        }
    }

   //  1-2 attach : Intent 컨텐츠 가져오기
    private void pdfPickIntent() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"),PDF_PICK_CODE);
    }

    // 1-3 받은 결과값이 실행되면
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
            pdfUri = data.getData();
            }
        }else{
            Toast.makeText(this, "pdf 선택 취소", Toast.LENGTH_SHORT).show();
        }
    }

    //3-3. submit 1-3 받은 결과값 pdfUri를 fireStorage에 넣기
    private void updateData() {

        progressDialog.setMessage("pdf 업로딩중...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Books/"+timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();
                        progressDialog.dismiss();
                        // storage에 저장한 pdf를 db에 넣기
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //3-4 firestorage 정보를 fireDatabase Db에 넣기
    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {

        String title = binding.titleEt.getText().toString().trim();
        String description = binding.descriptionEt.getText().toString().trim();
        String category = binding.categoryTv.getText().toString().trim();
        String date = MyApplication.formatTimestamp(timestamp);

        progressDialog.setMessage("데이터베이스에 저장 중");
        progressDialog.show();

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("category", ""+category);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("date", ""+date);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+title)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfAddActivity.this, "Pdf 업로드 성공", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                   progressDialog.dismiss();
                        Toast.makeText(PdfAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}