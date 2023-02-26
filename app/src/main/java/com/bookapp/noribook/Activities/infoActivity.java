package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityInfoBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class infoActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    ActivityInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        information();

        MyApplication.noriCoinCheck(binding.noriGoldTv);

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(infoActivity.this, LoginActivity.class));
                }else{
                    startActivity(new Intent(infoActivity.this, ProfileActivity.class));
                }
            }
        });

        //  로그아웃
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(infoActivity.this, LoginActivity.class));
                finish();
            }
        });


        binding.bottomNav.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.home:
                    Intent intent = new Intent(infoActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return  true;
                case R.id.library:
                    Intent intent2 = new Intent(infoActivity.this, DashboardUserActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.favorite:
                    if(firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(this, "로그인 하세요", Toast.LENGTH_SHORT).show();
                        break;
                    }else {
                        Intent intent1 = new Intent(this, FavoriteActivity.class);
                        startActivity(intent1);
                    }
                    return true;
                case R.id.info:
                    break;
            }
            return true;
        });

        binding.noriGoldIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.askGoldBuy(infoActivity.this);
            }
        });


    }


    private void information() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Info");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imgUrl = ""+snapshot.child("url").getValue();
                Glide.with(infoActivity.this)
                        .load(imgUrl)
                        .into(binding.visionIv);

                String boardUrl = ""+snapshot.child("boardUrl").getValue();

                String visionUrl = ""+snapshot.child("visionUrl").getValue();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<String> addressList = getTextFromWeb(boardUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb = new StringBuilder();
                                for(String s : addressList){
                                    sb.append(s);
                                    sb.append("\n");
                                }
                                binding.boardTv.setText(sb.toString());
                                binding.boardTv.setMovementMethod(new ScrollingMovementMethod());
                            }
                        });

                    }
                }).start();



                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<String> addressList = getTextFromWeb(visionUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb = new StringBuilder();
                                for(String s : addressList){
                                    sb.append(s);
                                    sb.append("\n");
                                }
                                binding.visionTv.setText(sb.toString());
                                binding.visionTv.setMovementMethod(new ScrollingMovementMethod());
                            }
                        });

                    }
                }).start();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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
    }

    private List<String> getTextFromWeb(String urlString) {
        URLConnection feedUrl;
        List<String> placeAddress = new ArrayList<>();

        try {
            feedUrl = new URL(urlString).openConnection();
            InputStream is = feedUrl.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;

            while ((line = reader.readLine()) != null)
            {
                placeAddress.add(line);
            }
            is.close();
            return  placeAddress;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }
}