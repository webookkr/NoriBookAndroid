package com.bookapp.noribook.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bookapp.noribook.Adapter.AdapterHomeBook;
import com.bookapp.noribook.Model.ModelBook;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityHomeBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private ActivityHomeBinding binding;

    private ArrayList<ModelBook> pdfArrayList, pdfArrayList2, pdfArrayList3, reversePdf2 ;

    private AdapterHomeBook adapterCount;

    private Handler mHandler = new Handler();

    private ViewStub splashViewStub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        SharedPreferences sharedPreferences = getSharedPreferences("NoriBookAndroid", MODE_PRIVATE);
        boolean isSharedDisplayed = sharedPreferences.getBoolean("is_shared_displayed",false);
        Log.d("MyTag", "is_shared_displayed: " + isSharedDisplayed); // Log로 값 확인

        if (!isSharedDisplayed){

            binding.splashVs.setVisibility(View.VISIBLE);
            checkUser();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.splashVs.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_shared_displayed", true);
                    editor.apply();
                }
            },2000);
        }


        firebaseAuth = FirebaseAuth.getInstance();

        // home 조회수 베스트
        loadBestCountBook("viewCountMinus");

        // home 추천 베스트
        loadBestRecommendBook("recommendCountMinus");

        // 오너 추천 (featured =true) 가져오기
        loadFeaturedBook("featured");

        // 이미지 넣기
        advImageSet();

        nameBinding();

        MyApplication.noriCoinCheck(binding.noriGoldTv);




        binding.noriGoldIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.askGoldBuy(HomeActivity.this);
            }
        });


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
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.favorite:
                    if(firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(this, "로그인 하세요", Toast.LENGTH_SHORT).show();
                        break;
                    }else {
                        Intent intent1 = new Intent(this, FavoriteActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                    }
                    return true;
                case R.id.info:
                    Intent intent2 = new Intent(HomeActivity.this, infoActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent2);
                    return true;
            }
            return true;
        });

    }

    String email = "";
    private void nameBinding() {
        String uid = firebaseAuth.getUid();
        if(firebaseAuth.getCurrentUser() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    email = ""+snapshot.child("email").getValue();
                    binding.subTitleTv.setText(email);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

//    private void advImageSet() {
//
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdvImage");
//        reference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String imgUrl = ""+snapshot.child("url").getValue();
//                Glide.with(HomeActivity.this)
//                        .load(imgUrl)
//                        .into(binding.adIv);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//    }

    private void advImageSet() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdvImage");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> imageUrls = new ArrayList<>(); // url 리스트 생성
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String url = childSnapshot.getValue(String.class);
                    imageUrls.add(url);
                }
                if (!imageUrls.isEmpty()) {
                    setAdvImage(imageUrls, 0); // 0번째 이미지부터 시작
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setAdvImage(List<String> imageUrls, int index) {
        if (isDestroyed() || isFinishing()) {
            return;
        }

        Glide.with(this)
                .load(imageUrls.get(index))
                .into(binding.adIv);

        new Handler().postDelayed(new Runnable() { // 일정 시간 후에 다음 이미지로 변경
            @Override
            public void run() {
                int nextIndex = (index + 1) % imageUrls.size();
                setAdvImage(imageUrls, nextIndex);
            }
        }, 6000); // 5초 후에 다음 이미지로 변경
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
                            ModelBook model = ds.getValue(ModelBook.class);
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
        reversePdf2 = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList2.clear();

                        for (DataSnapshot ds:snapshot.getChildren()) {
                            ModelBook model = ds.getValue(ModelBook.class);
                            pdfArrayList2.add(model);
                        }
                        adapterCount = new AdapterHomeBook(HomeActivity.this, pdfArrayList2);
//                        GridLayoutManager layoutManager = new GridLayoutManager(HomeActivity.this, 2, RecyclerView.HORIZONTAL,true);
//                        binding.favoriteRv.setLayoutManager(layoutManager);
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
                    ModelBook model = ds.getValue(ModelBook.class);
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

    @Override
    protected void onRestart() {
        super.onRestart();

        SharedPreferences sharedPreferences = getSharedPreferences("NoriBookAndroid", MODE_PRIVATE);
        boolean isSharedDisplayed = sharedPreferences.getBoolean("is_shared_displayed",false);

        if (!isSharedDisplayed){
            binding.splashVs.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUser();
                    binding.splashVs.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_shared_displayed", true);
                    editor.apply();
                }
            },2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences = getSharedPreferences("NoriBookAndroid", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_shared_displayed", false);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void checkUser() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // 로그인 되어 있지 않으면
//            startActivity(new Intent(H.this, HomeActivity.class));
//            finish();
            return;
        }
        else { // 로그인 되어 있으면 유저와 관리자 구분해서 대쉬보드로
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userType = ""+snapshot.child("userType").getValue();
                            if (userType.equals("user")){
                                // user는 userDash
//                                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
//                                finish();
                            }
                            else if (userType.equals("admin")){
                                startActivity(new Intent(HomeActivity.this, DashboardAdminActivity.class));
                                finish();
                            }
                            else if (userType.equals("editor")){
//                                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
//                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }
    }

}