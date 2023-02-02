package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bookapp.noribook.Adapter.AdapterHomeBook;
import com.bookapp.noribook.Adapter.AdapterSub;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.Model.ModelSub;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityHomeBinding;
import com.bookapp.noribook.fragment.FavoriteFragment;
import com.bookapp.noribook.fragment.HomeFragment;
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

    private ArrayList<ModelPdf> pdfArrayList;

    private AdapterHomeBook adapterCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        loadBestCountBook("viewCount");

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

    private void loadBestCountBook(String orderBy) {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();

                for (DataSnapshot ds:snapshot.getChildren()) {
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                adapterCount = new AdapterHomeBook(HomeActivity.this, pdfArrayList);
                binding.countBestRv.setAdapter(adapterCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}