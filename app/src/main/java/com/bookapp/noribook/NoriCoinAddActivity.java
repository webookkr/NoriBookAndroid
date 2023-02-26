package com.bookapp.noribook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bookapp.noribook.Activities.ProfileActivity;
import com.bookapp.noribook.Activities.ProfileEditActivity;
import com.bookapp.noribook.databinding.ActivityNoriCoinAddBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoriCoinAddActivity extends AppCompatActivity {

    ActivityNoriCoinAddBinding binding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoriCoinAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        loadUserData();

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NoriCoinAddActivity.this, ProfileEditActivity.class));
            }
        });

        binding.renewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        
    }

    private void loadUserData() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = ""+snapshot.child("name").getValue();
                String profileUrl = ""+snapshot.child("profileImage").getValue();
                String noriCoin = ""+snapshot.child("noriCoin").getValue();

                binding.nameTv.setText(name);
                binding.coinTv.setText(noriCoin+"원");

                Glide.with(NoriCoinAddActivity.this)
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}