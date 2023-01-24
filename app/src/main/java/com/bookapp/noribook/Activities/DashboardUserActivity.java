package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bookapp.noribook.BookUserFragment;
import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    private ActivityDashboardUserBinding binding ;

    private FirebaseAuth firebaseAuth;
    // categories to show in taps
    private ArrayList<ModelCategory> categoryArrayList;

    // 1. viewPagerAdapter 변수 생성
    public ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);

        binding.tabLayout.setupWithViewPager(binding.viewPager);

//  logoutBtn
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
            }
        });
    }

    // 1- 3.
    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        categoryArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                // load categories - Static all, most viewed
                // all data to model
                ModelCategory modelAll = new ModelCategory("01","All","","");
                ModelCategory modelMostViewed = new ModelCategory("02","Most Viewed","","");

                // add models to list
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                // add data to view pager adapter
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(""+modelAll.getId(),""+modelAll.getCategory(),""+modelAll.getUid()),modelAll.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(""+modelMostViewed.getId(),""+modelMostViewed.getCategory(),""+modelMostViewed.getUid()),modelMostViewed.getCategory());
                //refrash list
                viewPagerAdapter.notifyDataSetChanged();
                // load from fire base
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);
                    // add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            ""+model.getId(),""+model.getCategory(),""+model.getUid()), model.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);

    }

    // 1-2 adapter
    public class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
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

        }
        else {
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);

        }

    }
}