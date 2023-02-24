package com.bookapp.noribook.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bookapp.noribook.Activities.DashboardUserActivity;
import com.bookapp.noribook.Activities.MainActivity;
import com.bookapp.noribook.Activities.ProfileActivity;
import com.bookapp.noribook.BookUserFragment;
import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.FragmentLibraryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class LibraryFragment extends Fragment {

    private Context mContext;

    private FragmentLibraryBinding binding;

    private FirebaseAuth firebaseAuth;

    // categories to show in taps
    public ArrayList<ModelCategory> categoryArrayList;

    // 1. viewPagerAdapter 변수 생성
    public ViewPagerAdapter viewPagerAdapter;

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);

        binding.tabLayout.setupWithViewPager(binding.viewPager);

//  logoutBtn
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });


        // open profile button
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(getActivity(), "로그인 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(getActivity(), ProfileActivity.class));
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
//        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
//
//        categoryArrayList = new ArrayList<>();
//
//        categoryArrayList.clear();
//        // load categories - Static all, most viewed
//        // all data to model
//        ModelCategory modelAll = new ModelCategory("01","All","","");
//        ModelCategory modelMostViewed = new ModelCategory("02","Most Viewed","","");
//
//        // add models to list
//        categoryArrayList.add(modelAll);
//        categoryArrayList.add(modelMostViewed);
//        // add data to view pager adapter
//        viewPagerAdapter.addFragment(BookUserFragment.newInstance(
//                ""+modelAll.getId(),
//                ""+modelAll.getCategory(),
//                ""+modelAll.getUid()
//                ,""+modelAll.getDate()
//        ), modelAll.getCategory());
//        viewPagerAdapter.addFragment(BookUserFragment.newInstance(
//                ""+modelMostViewed.getId(),
//                ""+modelMostViewed.getCategory(),
//                ""+modelMostViewed.getUid(),
//                ""+modelMostViewed.getDate()
//        ),modelMostViewed.getCategory());
//        //refrash list
//        viewPagerAdapter.notifyDataSetChanged();
//
//        //load categories from firebase
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds: snapshot.getChildren()){
//                    ModelCategory model = ds.getValue(ModelCategory.class);
//                    categoryArrayList.add(model);
//                    // add data to viewPagerAdapter
//                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
//                            ""+model.getId(),
//                            ""+model.getCategory(),
//                            ""+model.getUid()
//                            ,""+model.getDate()
//                    ), model.getCategory());
//                    viewPagerAdapter.notifyDataSetChanged();
//                }
//
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        //set adapter to view pager
//        viewPager.setAdapter(viewPagerAdapter);

    }

    // 1-2 adapter
    public class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        // 1-4 fragment
        private void addFragment(BookUserFragment fragment, String title){
            // add fragment passed as parameter in fragmentList
            fragmentList.add(fragment);
            // add title passed as parameter in fragmentTitleList
            fragmentTitleList.add(title);
        }

        // 1-5 getPageTitle
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }





    //  logoutBtn
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            binding.subTitleTv.setText("로그 아웃 상태");

        }
        else {
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);

        }

    }
}