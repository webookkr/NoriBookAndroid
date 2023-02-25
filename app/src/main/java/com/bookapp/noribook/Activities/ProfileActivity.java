package com.bookapp.noribook.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bookapp.noribook.Adapter.AdapterFavorite;
import com.bookapp.noribook.Model.ModelBook;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityProfileBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;


    private ArrayList<ModelBook> pdfArrayList;

    private AdapterFavorite adapterFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserinfo();

        loadFavoriteBook();

        MyApplication.noriCoinCheck(binding.noriGoldTv);

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    // adapter(pdfFavorite)
    private void loadFavoriteBook() {
        //init
        pdfArrayList = new ArrayList<>();

        //loadfavorite book : Users -> userId -> Favorite
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorite")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){
                            String bookTitle = ""+ds.child("bookTitle").getValue();

                            ModelBook modelBook = new ModelBook();
                            modelBook.setTitle(bookTitle); // profile Detail 에서 bookTitle 받아와서 set하고 adapter에서 get해서 이어서 "Uses"에서 가져오기
                            pdfArrayList.add(modelBook);
                        }
                        adapterFavorite = new AdapterFavorite(ProfileActivity.this, pdfArrayList);
                        binding.booksRv.setAdapter(adapterFavorite);

                        String favoriteCount ="" +snapshot.getChildrenCount();
                        binding.favoriteBookCount.setText(favoriteCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadUserinfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                     String email = ""+snapshot.child("email").getValue();
                     String userType = ""+snapshot.child("userType").getValue();
                     String name = ""+snapshot.child("name").getValue();
                     String date = ""+snapshot.child("date").getValue();
                     String uid = ""+snapshot.child("uid").getValue();
                     String profileImage = ""+snapshot.child("profileImage").getValue();

                     binding.emailTv.setText(email);
                     binding.nameTv.setText(name);
                     binding.memberDateTv.setText(date);

                     // set Image , using glide -> github

                    Glide.with(ProfileActivity.this)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}