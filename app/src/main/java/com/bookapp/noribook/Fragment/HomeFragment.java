package com.bookapp.noribook.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bookapp.noribook.Activities.LoginActivity;
import com.bookapp.noribook.Activities.ProfileActivity;
import com.bookapp.noribook.Adapter.AdapterHomeBook;
import com.bookapp.noribook.Model.ModelBook;
import com.bookapp.noribook.databinding.FragmentHomeBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private FirebaseAuth firebaseAuth;

    private Context mConText;

    private ArrayList<ModelBook> pdfArrayList, pdfArrayList2, pdfArrayList3, reversePdf2 ;

    private AdapterHomeBook adapterCount;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mConText = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // home 조회수 베스트
        loadBestCountBook("viewCount");

        // home 추천 베스트
        loadBestRecommendBook("recommendCount");

        // 오너 추천 (featured =true) 가져오기
        loadFeaturedBook("featured");

        // 이미지 넣기
        advImageSet();

        nameBinding();


        // 프로필 button
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }else{
                    startActivity(new Intent(getActivity(), ProfileActivity.class));
                }
            }
        });

        //  로그아웃
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
//                finish();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private void advImageSet() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdvImage");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imgUrl = ""+snapshot.child("url").getValue();
                Glide.with(getActivity())
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
                            ModelBook model = ds.getValue(ModelBook.class);
                            pdfArrayList.add(model);
                        }
                        adapterCount = new AdapterHomeBook(getActivity(), pdfArrayList);
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
                        adapterCount = new AdapterHomeBook(getActivity(), pdfArrayList2);
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
                        adapterCount = new AdapterHomeBook(getActivity(), pdfArrayList3);
                        binding.countBestRv.setAdapter(adapterCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}