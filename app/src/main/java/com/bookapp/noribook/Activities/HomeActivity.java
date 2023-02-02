package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Adapter.AdapterHomeBook;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityHomeBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private ActivityHomeBinding binding;

    private ArrayList<ModelPdf> pdfArrayList, pdfArrayList2, pdfArrayList3, pdfArrayList4 ;

    private AdapterHomeBook adapterCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        // home 조회수 베스트
        loadBestCountBook("viewCount");

        // home 추천 베스트
        loadBestRecommendBook("recommendCount");

        // 오너 추천 (featured =true) 가져오기
        loadFeaturedBook("featured");

        // 이미지 넣기
        advImageSet();


        // 프로필 button
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                }else{
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                }
            }
        });

        //  로그아웃
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
            }
        });


// 네비
        binding.bottomNav.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {

                case R.id.home:
                    break;
                case R.id.library:
                    Intent intent = new Intent(HomeActivity.this, DashboardUserActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.favorite:
                    if(firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(this, "로그인 하세요", Toast.LENGTH_SHORT).show();
                        break;
                    }else {
                        Intent intent1 = new Intent(this, ProfileActivity.class);
                        startActivity(intent1);
                    }
                    return true;
            }
            return true;
        });

    }

    private void advImageSet() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdvImage");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imgUrl = ""+snapshot.child("url").getValue();
                Glide.with(HomeActivity.this)
                        .load(imgUrl)
                        .into(binding.adIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadFeaturedBook(String orderBy) {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).equalTo(true).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();

                        for (DataSnapshot ds:snapshot.getChildren()) {
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                        }
                        adapterCount = new AdapterHomeBook(HomeActivity.this, pdfArrayList);
                        binding.featuredBestRv.setAdapter(adapterCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadBestRecommendBook(String orderBy) {
        pdfArrayList2 = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList2.clear();

                        for (DataSnapshot ds:snapshot.getChildren()) {
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            pdfArrayList2.add(model);
                        }
                        adapterCount = new AdapterHomeBook(HomeActivity.this, pdfArrayList2);
                        binding.favoriteRv.setAdapter(adapterCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBestCountBook(String orderBy) {
        pdfArrayList3 = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList3.clear();

                for (DataSnapshot ds:snapshot.getChildren()) {
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    pdfArrayList3.add(model);
                }
                adapterCount = new AdapterHomeBook(HomeActivity.this, pdfArrayList3);
                binding.countBestRv.setAdapter(adapterCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}