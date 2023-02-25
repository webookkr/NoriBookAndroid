package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.ActivitySubAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SubAddActivity extends AppCompatActivity {

    String bookId, bookTitle;

    ActivitySubAddBinding binding;

    private ProgressDialog progressDialog;

    private Uri pdfUri = null;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.titleTv.setText(bookTitle);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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

    String subNumber = ""+0 ;
    String subTitle = "";
    private void validateData() {
        subNumber = binding.subNumEt.getText().toString().trim();
        subTitle = binding.subTitleEt.getText().toString().trim();
        if (TextUtils.isEmpty(subNumber)){
            Toast.makeText(this, "No가 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(subTitle)){
            Toast.makeText(this, "제목이 비었습니다.", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri == null){
            Toast.makeText(this, "pdf가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
        updateData();
    }

    private void updateData() {
        progressDialog.setMessage("pdf 업로딩 중..");
        progressDialog.show();
        
        long timestamp = System.currentTimeMillis();
        
        String filePathAndName = "SubBooks/"+bookTitle+"/"+ subNumber;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();
                        uploadedPdfInfoToDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SubAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show(); 
                    }
                });
        
        
    }

    private void uploadedPdfInfoToDb(String uploadedPdfUrl, long timestamp) {

        String date = MyApplication.formatTimestamp(timestamp);

        progressDialog.setMessage("데이터베이스에 저장 중");
        progressDialog.show();

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("subNumber", subNumber);
        hashMap.put("subTitle",subTitle);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("date",""+date);
        hashMap.put("viewCount",0);
        hashMap.put("recommend",0);
        hashMap.put("bookTitle",""+bookTitle);
        hashMap.put("subId",""+timestamp);


        String filePathAndName = "SubBooks/"+bookTitle+"/";
        String subFilePathAndName = subNumber;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(filePathAndName);
        ref.child(subFilePathAndName).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(SubAddActivity.this, "pdf 업로드 성공", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SubAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pdfPickIntent() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), 1004);
        Toast.makeText(this, "pdf를 선택하였습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            pdfUri = data.getData();
        }
        else{
            Toast.makeText(this, "pdf 선택 취소", Toast.LENGTH_SHORT).show();
        }
    }
}