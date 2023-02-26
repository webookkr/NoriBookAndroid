package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.Adapter.AdapterFavorite;
import com.bookapp.noribook.Model.ModelBook;
import com.bookapp.noribook.Model.ModelFavorite;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityFavoriteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    private ActivityFavoriteBinding  binding;

    private FirebaseAuth firebaseAuth;


    private ArrayList<ModelBook> bookArrayList;

    private ArrayList<ModelFavorite> favArrayList;

    private AdapterFavorite adapterFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserinfo();

        loadFavoriteBook();

        MyApplication.noriCoinCheck(binding.noriGoldTv);

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(FavoriteActivity.this, "로그인 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(FavoriteActivity.this, ProfileActivity.class));
                }
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                finish();
            }
        });


        binding.bottomNav.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.home:
                    Intent intent = new Intent(FavoriteActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return  true;
                case R.id.library:
                    Intent intent2 = new Intent(FavoriteActivity.this, DashboardUserActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.favorite:
                    break;
                case R.id.info:
                    Intent intent3 = new Intent(FavoriteActivity.this, infoActivity.class);
                    startActivity(intent3);
                    return true;
            }
            return true;
        });

        binding.noriGoldIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.askGoldBuy(FavoriteActivity.this);
            }
        });


    }


    private void loadFavoriteBook() {
        //init
        bookArrayList = new ArrayList<>();

        //loadfavorite book : Users -> userId -> Favorite
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorite")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){
                            String bookTitle = ""+ds.child("bookTitle").getValue();

                            ModelBook modelBook = new ModelBook();
                            modelBook.setTitle(bookTitle); // profile Detail 에서 bookTitle 받아와서 set하고 adapter에서 get해서 이어서 "Uses"에서 가져오기
                            bookArrayList.add(modelBook);
                        }
                        adapterFavorite = new AdapterFavorite(FavoriteActivity.this, bookArrayList);
                        binding.booksRv.setAdapter(adapterFavorite);

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

                        binding.nameTv.setText(name);
                        binding.subTitleTv.setText(email);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}