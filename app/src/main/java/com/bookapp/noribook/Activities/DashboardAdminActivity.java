package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Adapter.AdapterCategory;
import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.databinding.ActivityDashboardAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class DashboardAdminActivity extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding ;

    private FirebaseAuth firebaseAuth ;

    // 1-1. 카테고리 데이터 변수
    private ArrayList<ModelCategory> categoryArrayList; // <modelcategry>데이터 형식을 선언한 arraylist
    private AdapterCategory adapterCategory;

    private ProgressDialog progressDialog;

    private Uri imgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        // 1- 2.  category load - 어뎁터 만든 후에
        loadCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("이미지 선택");
        progressDialog.setCanceledOnTouchOutside(false);

        // 2. search list
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 타입할때마다 반응
                try {
                    adapterCategory.getFilter().filter(charSequence);
                }
                catch (Exception e){

                };
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //logout btn
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        // 광고 추가 (광고 업로드)
        binding.addAdvFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickIntent();

            }
        });



        //category add btn
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });

        //pdf add btn
        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });
    }

    private void imageUpload(Uri imgUri) {
        progressDialog.setMessage("이미지 업로드 중");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("AdvImage/advImg");
        storageReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();
                // storage에 저장한 pdf를 db에 넣기
                uploadPdfInfoToDb(uploadedPdfUrl);
            }
        });

    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl) {
        progressDialog.setMessage("데이터베이스에 저장 중");
        progressDialog.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", 1);
        hashMap.put("url",""+uploadedPdfUrl);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AdvImage");
        ref.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(DashboardAdminActivity.this, "Pdf 업로드 성공", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(DashboardAdminActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void imagePickIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "광고 이미지 선택"),500);
    }

    // 1-3 받은 결과값이 실행되면
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if(requestCode == 500){
                imgUri = data.getData();
                imageUpload(imgUri);
            }
        }else{
            Toast.makeText(this, "이미지 선택 취소", Toast.LENGTH_SHORT).show();
        }
    }

    //  1- 3. 카테고리 로드 (어뎁터 만든 후에 모델과 연결)
    private void loadCategories() {
        //init array
        categoryArrayList = new ArrayList<>();

        // firebase 카테고리 읽기
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             // 일단 자료 비우기
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    // add to array
                    categoryArrayList.add(model);
                }
                //setup adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);
                //set adapter to recyclerview
                binding.categoryRv.setAdapter(adapterCategory);


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // profileBtn
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, ProfileActivity.class));
            }
        });


    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // 로그인 안되어 있으면 메인화면으로
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else {
            //로그인 되어 있으면 유저 정보 대쉬보드에
            String email = firebaseUser.getEmail();
            // 텍스트 표시
            binding.subTitleTv.setText(email);
        }
    }
}