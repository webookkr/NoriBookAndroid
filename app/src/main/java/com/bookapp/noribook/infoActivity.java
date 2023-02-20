package com.bookapp.noribook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Activities.DashboardUserActivity;
import com.bookapp.noribook.Activities.HomeActivity;
import com.bookapp.noribook.Activities.LoginActivity;
import com.bookapp.noribook.Activities.ProfileActivity;
import com.bookapp.noribook.databinding.ActivityInfoBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                        Intent intent1 = new Intent(this, ProfileActivity.class);
                        startActivity(intent1);
                    }
                    return true;
                case R.id.info:
                    break;
            }
            return true;
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
                        .into(binding.infoIv);
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
}