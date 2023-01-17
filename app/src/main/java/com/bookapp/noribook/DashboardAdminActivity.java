package com.bookapp.noribook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.bookapp.noribook.Adapter.AdapterCategory;
import com.bookapp.noribook.Filter.FilterCategory;
import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.databinding.ActivityDashboardAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding ;

    private FirebaseAuth firebaseAuth ;

    // 1-1. 카테고리 데이터 변수
    private ArrayList<ModelCategory> categoryArrayList; // <modelcategry>데이터 형식을 선언한 arraylist
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        // 1- 2.  category load - 어뎁터 만든 후에
        loadCategories();

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